# Refactor diferido — Transaccionalidad de `FacturaDao.registrar` (Swing)

> Documento de plan para retomar cuando el proyecto principal de US lo
> permita. Diferido voluntariamente el 2026-05-28 para no atrasar entregas.

## TL;DR

`FacturaDao.registrar()` no es transaccional: cada INSERT (encabezado +
N detalles + cuenta_factura) usa una conexión distinta del pool con
`autoCommit=true`. Si cualquier paso falla después del encabezado, queda
factura cobrada sin líneas (huérfana). **El bug está latente pero no se
manifiesta hoy** porque V27 desactivó el único punto conocido de falla
intermedia. Plan documentado abajo para arreglarlo cuando haya tiempo.

## Contexto y por qué se difirió

- Bug descubierto al investigar el reporte de "Stock insuficiente" en
  Cliente A (2026-05-28). Tras aplicar **V27** (default `COALESCE` →
  1 = permitir), el SIGNAL ya no salta desde cajas y las facturas pasan
  completas. El bug estructural sigue latente pero no se reproduce en
  el flujo normal.
- El refactor es trabajo de ~2 horas + pruebas, en código Swing legacy
  que el proyecto está migrando gradualmente al API. Decisión 2026-05-28:
  no atrasar el roadmap de US del API por esto.
- Riesgo aceptado: cualquier OTRA causa de falla en mitad del flujo
  (BD caída durante el INSERT, deadlock, error inesperado de FK, futura
  V28 que reactive validación V19 vía firma con `p_usuario`) volverá a
  producir huérfanos hasta que se haga este refactor.

## El bug en una frase

```java
FacturaDao.registrar() {
    conn1 = pool.getConnection();  // ← una conn
    INSERT encabezado_factura       // autoCommit=true → commit inmediato

    for (detalle : detalles) {
        detallesDao.agregarDetalle(detalle, id);  // ← abre conn2 NUEVA
        // INSERT detalle_factura     autoCommit=true → commit inmediato
        // trigger detalle_factura_b_insert → CALL crear_venta_kardex
        //   ← si falla aquí (SIGNAL, FK, deadlock), conn1 ya committeada
    }

    if (credito) cuentaFacturaDao.registrar();   // ← abre conn3 NUEVA
}
```

3 conexiones distintas del pool, 3 transacciones independientes. Si el
detalle 2 de 5 falla, queda el encabezado + detalle 1 ya commiteados.

## Lo que YA funciona (NO necesita arreglarse hoy)

- Facturación normal en Swing legacy → pasa OK desde V27.
- Sale Returns en API (INV-8 final) → ya transaccional con `tenantTM` +
  `commonTM` + compensación (`InvoiceService.createFromOrder`).
- Compras/Devoluciones de compra/Requisiciones en API → ya transaccionales
  con `@Transactional` Spring.
- El pool DBCP2 sigue funcionando normalmente sin cambios.

## Opciones evaluadas

| Opción | Trabajo | Resuelve | Estado |
|---|---|---|---|
| **A**. Documentar como deuda, no tocar ahora | 0 | Nada — solo registro | ✅ **Esta decisión** |
| **B**. Pre-validación Java antes de tocar BD | ~30 LOC | 80% (casos comunes), no race conditions ni fallos BD | Atajo viable si reaparece |
| **C**. Refactor transaccional completo | ~150 LOC + pruebas | 100% — todos los casos | **Plan abajo** para cuando se active |

## Plan al retomar (Opción C)

### Paso 0 — Defensa a nivel pool (1 línea, primero, aislado)

En `ConexionStatic.getPoolConexion()` (el método que configura el
`BasicDataSource`), agregar:

```java
ds.setUrl(url);
ds.setUsername(login);
// ... existing ...
ds.setInitialSize(3);
ds.setMaxIdle(3);
ds.setMinIdle(3);
ds.setDefaultAutoCommit(true);   // ← NUEVO: pool resetea autoCommit al prestar
ds.setMinEvictableIdleTimeMillis(1000 * 60 * 15);
```

**Por qué primero**: DBCP2 NO resetea `autoCommit` al `close()` por
defecto. Esta línea hace que el POOL fuerce `autoCommit=true` al PRESTAR
cualquier conexión, así aunque algún `finally` se olvide, no contamina
la siguiente operación. Es una red de seguridad que NO depende de los
cambios posteriores. Conviene aplicarla incluso si nunca se hace el resto
del refactor.

**Commit aislado**: `feat: setDefaultAutoCommit(true) en pool DBCP2`.

### Paso 1 — Sobrecargas en DAOs del flujo

Tocar estos archivos (verificar lista completa en el momento; el flujo
de facturación puede llamar más de los listados):

- `src/.../dao/DetalleFacturaDao.java` — método `agregarDetalle`.
- `src/.../dao/CuentaFacturaDao.java` — método `registrar`.
- `src/.../dao/CuentaXCobrarFacturaDao.java` — método `reguistrarCredito`,
  `reguistrarDebitoYaProcesado` (si están en critical path).
- `src/.../dao/CuentaPorCobrarDao.java` — `reguistrarCredito`,
  `getSaldoCliente`.

Patrón a aplicar en cada uno:

```java
// === SOBRECARGA NUEVA — recibe conexión externa ===
public boolean agregarDetalle(DetalleFactura detalle, int idFactura, Connection conn) throws SQLException {
    // Usa la conn que recibe. NO la cierra. NO la toca para autoCommit.
    try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
        // ... bind params ...
        ps.executeUpdate();
        return true;
    }
}

// === WRAPPER OLD — mantiene firma vieja para compatibilidad ===
public boolean agregarDetalle(DetalleFactura detalle, int idFactura) {
    try (Connection conn = ConexionStatic.getPoolConexion().getConnection()) {
        return agregarDetalle(detalle, idFactura, conn);
    } catch (SQLException e) {
        log.error("agregarDetalle fallo", e);
        return false;
    }
}
```

**Importante**:
- La sobrecarga nueva **propaga `SQLException`** — el caller decide rollback.
- El wrapper viejo **traga la excepción** (comportamiento histórico),
  para no romper callers existentes.
- NO cerrar la conexión en la sobrecarga nueva — la maneja el caller.

### Paso 2 — Refactor `FacturaDao.registrar`

```java
public boolean registrar(Object c) {
    Factura myFactura = (Factura) c;
    Connection conn = null;
    boolean success = false;

    super.DbName = ConexionStatic.getUsuarioLogin().getCajaActiva().getNombreBd();
    String sql = super.getQueryInsert() + " (...) VALUES (...)";

    try {
        // si el cliente es escrito por el usuario (fuera de la transacción,
        // porque vive en admin_tools no en la caja)
        if (myFactura.getCliente().getId() < 0) {
            myClienteDao.registrarClienteContado(myFactura.getCliente());
            myFactura.getCliente().setId(myClienteDao.getIdClienteRegistrado());
        }

        conn = ConexionStatic.getPoolConexion().getConnection();
        conn.setAutoCommit(false);   // ← abre transacción

        // ---- 1) INSERT encabezado ----
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // ... bind params igual que hoy ...
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idFacturaGuardada = rs.getInt(1);
                    myFactura.setIdFactura(idFacturaGuardada);
                }
            }
        }

        // ---- 2) INSERT detalles (reusan conn) ----
        for (int x = 0; x < myFactura.getDetalles().size(); x++) {
            DetalleFactura detalle = myFactura.getDetalles().get(x);
            if (detalle.getArticulo().getId() != -1) {
                detallesDao.agregarDetalle(detalle, idFacturaGuardada, conn);
            }
        }

        // ---- 3) Si crédito, registrar CXP (reusa conn) ----
        if (myFactura.getTipoFactura() == 2) {
            CuentaPorCobrar ultima = myCuentaCobrarDao.getSaldoCliente(myFactura.getCliente(), conn);
            myCuentaCobrarDao.reguistrarCredito(myFactura, conn);

            CuentaFactura unaCuentaFactura = new CuentaFactura();
            unaCuentaFactura.setCaja(ConexionStatic.getUsuarioLogin().getCajaActiva());
            myFactura.setCodigoCaja(ConexionStatic.getUsuarioLogin().getCajaActiva().getCodigo());
            unaCuentaFactura.setCliente(myFactura.getCliente());
            unaCuentaFactura.setFactura(myFactura);

            cuentaFacturaDao.registrar(unaCuentaFactura, conn);
            cuentaXCobrarFacturaDao.reguistrarCredito(unaCuentaFactura, conn);

            if (ultima.getSaldo().doubleValue() < 0) {
                // misma lógica de hoy, pero pasando conn a cuentaXCobrarFacturaDao
                // ...
            }
        }

        conn.commit();   // ← TODO o NADA
        success = true;
        return true;

    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); }
            catch (SQLException ex) { log.error("Rollback fallo", ex); }
        }
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "Error al facturar — la operación fue revertida: " + e.getMessage(),
            "Error en la base de datos", JOptionPane.ERROR_MESSAGE);
        return false;

    } finally {
        super.DbName = DbNameBase;
        if (conn != null) {
            try { conn.setAutoCommit(true); }    // ← defensa local (Paso 0 lo hace a nivel pool, pero defensa explícita igual)
            catch (SQLException ex) { log.warn("Reset autoCommit fallo", ex); }
            try { conn.close(); }                 // devuelve al pool
            catch (SQLException ex) { /* log */ }
        }
    }
}
```

**Sutilezas a respetar**:
- `super.DbName = ...` se hace ANTES de pedir la conexión (el pool toma
  el DbName del momento del `getConnection`).
- `super.DbName = DbNameBase` en el `finally` (no cambiar el patrón
  histórico).
- Si `registrarClienteContado` se queda fuera de la tx (como en el
  ejemplo), eso es intencional: el cliente vive en `admin_tools`, no en
  la caja, y se permite que persista aunque la factura falle (el cliente
  no genera dato erróneo, solo queda "creado pero sin uso"). Si se
  prefiere atómico, requiere conn cross-schema explícita — complicación
  adicional.

### Paso 3 — Pruebas

Tres escenarios obligatorios en local con caja1/4321:

| # | Escenario | Setup | Esperado con refactor |
|---|---|---|---|
| 1 | Happy path | Factura con 2 ítems, stock alcanza, contado | Encabezado + 2 detalles + kardex movido + UI sin error |
| 2 | Fallo de detalle | Factura donde un ítem tiene `codigo_articulo` inválido (FK rota) | **0 filas** en `encabezado_factura`, **0** en `detalle_factura`, kardex sin cambios, mensaje de error al cajero |
| 3 | Fallo de cuenta_factura | Factura crédito (`tipo_factura=2`) donde `cuentaFacturaDao.registrar` falla por algo | **0 filas** en `encabezado_factura`, **0** en `detalle_factura`, **0** en `cuenta_factura`, kardex sin cambios |

Si en ese momento existe la V28 (validación V19 reactivada con `p_usuario`),
agregar:

| 4 | Sobreventa con flag=0 | Stock=5, vender 10 con usuario flag=0 | SIGNAL salta, rollback completo, BD limpia |

### Paso 4 — Commit + push

Rama: `feature/factura-transaccional` en repo Swing.
Commits separados:
- `feat(pool): setDefaultAutoCommit(true) en ConexionStatic` (paso 0 aislado, mergeable solo)
- `refactor(dao): sobrecarga con Connection en DAOs del critical path` (paso 1)
- `feat(facturacion): FacturaDao.registrar transaccional` (paso 2)

Merge a `master`, push.

## Estimación de esfuerzo

| Paso | Esfuerzo |
|---|---|
| 0 — `setDefaultAutoCommit(true)` | 5 min |
| 1 — sobrecargas en DAOs | 45 min (depende cuántos DAOs en critical path) |
| 2 — refactor `FacturaDao.registrar` | 45 min |
| 3 — pruebas locales con 3 escenarios | 30 min |
| 4 — commit + push | 10 min |
| **Total** | **~2 horas + 1 hora buffer** |

## Gatillos que activan el refactor

Ejecutar el plan cuando ocurra **cualquiera** de:

1. **Aparece otro huérfano en producción**. El bug es estructural — V27
   solo desactivó el detonante conocido.
2. **Se quiere reactivar la validación V19** (V28 + V9 caja con
   `p_usuario` como parámetro). La validación volverá a tirar SIGNAL en
   sobreventa; sin el refactor C, otra vez aparecen huérfanos.
3. **Se hace deploy a un cliente con políticas estrictas de auditoría**
   (p. ej. cliente con SAT, DGI o equivalente) donde una factura cobrada
   sin líneas es problema legal serio.
4. **Cualquier reporte de inconsistencia kardex/facturación** en
   producción que no se explique por otro motivo.

## Riesgos del refactor (cuando se haga)

| Riesgo | Mitigación |
|---|---|
| Romper otros callers de `agregarDetalle` / `cuentaFacturaDao.registrar` | Mantener firma vieja como wrapper compatible. El refactor solo ADICIONA sobrecargas, no quita métodos. |
| Olvidar `setAutoCommit(true)` en `finally` | Paso 0 (`setDefaultAutoCommit(true)` a nivel pool) cubre el olvido. |
| Conexión queda "colgada" en transacción si el código lanza una excepción no manejada | El `try/catch/finally` cubre `SQLException`. Si llega un `RuntimeException`, el `finally` igual cierra/devuelve. |
| Performance peor por aguantar más tiempo la conexión | NO — antes se tomaban 2+N conexiones, ahora 1. Menos contención del pool. |
| Bug nuevo si las pruebas no cubren un caso real | Hacer pruebas 2 y 3 (escenarios de fallo) sí o sí antes de mergear. Si hay caso de uso raro (p. ej. facturación con descuento por porcentaje y cliente al crédito con saldo a favor), agregar escenario. |

## Referencias

- Causa raíz del descubrimiento: `~/Desktop/admintools/docs/inv-epic-retrospective.md` §6.9
- Commit V27 (que evita la manifestación actual): `a6f85f9` (master Swing)
- Plan V28 + V9 caja (validación V19 real, también diferido): documentado en
  `inv-epic-retrospective.md` §6.9 ("Para validación real desde cajas...")
- Pool: `ConexionStatic.getPoolConexion()` — DBCP2, `initialSize=3, maxIdle=3, minIdle=3`.

---

Documento vivo. Cuando se ejecute el plan, actualizar este archivo con
"CERRADO YYYY-MM-DD + commit hash" o eliminarlo y dejar solo referencia
en la retrospectiva.
