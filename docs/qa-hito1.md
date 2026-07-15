# QA del Hito 1 — Fase 5 (pasada de prueba integral)

> Estado: **ejecutada en local 2026-07-14, sin push.** Objetivo de la fase:
> convertir "código terminado" (Fases 0-4) en "sistema probado". Todo se validó
> contra `localhost/admin_tools` (esquema V38) y la API en `:8099`; la instancia
> de IntelliJ en `:8080` no se tocó.

## Resumen

| Frente | Resultado |
|--------|-----------|
| Tests automatizados (API) | ✅ **136 tests, 0 fallos** (98 previos + 38 nuevos de lógica de Fases 2-4) |
| E2E de escenarios de negocio | ✅ **17 checks, 0 fallos** (venta→ticket→cierre→reporte→anulación→CxC→seguridad) |
| Regresión decimal | ✅ Esquema común con cero float **base** (2 restantes son vistas con `SUM`, benignas) |
| Matriz jar-viejo / BD-nueva | ✅ Verificado a nivel SQL (aditividad V38 + DECIMAL JDBC-safe). Falta 1 corrida real del Swing (smoke del usuario) |
| Smoke por navegador (POS) | ⬜ Pendiente del usuario (ver checklist al final) |

---

## 1. Tests automatizados (API)

`./gradlew test` → **136 tests, 0 fallos, 0 errores.** Se agregaron 38 tests
(commit `bd78b8e`) sobre la lógica nueva que no tenía cobertura, todos unitarios
puros (sin MySQL):

- **`LoginAttemptService`** (6) — throttle: <10 no bloquea, 10 bloquea, éxito
  limpia, claves usuario+IP independientes, null-safe.
- **`InvoiceQrTokenService`** (7) — HMAC determinístico; acepta el token correcto,
  rechaza alterado/otra-caja/otro-número; secreto vacío = feature apagada.
- **`TabularFileParser`** (9) — CSV `,`/`;` autodetectado, BOM, filas vacías,
  numeración estilo Excel (header=1), tope de filas, extensión no soportada, xlsx.
- **`ProductImportService` / `CustomerImportService`** (10) — validación por fila
  (nombre dup, precio/impuesto inválido, categoría inexistente, RTN, crédito sin
  límite) y **todo-o-nada** (con errores y `dryRun=false` NO se persiste nada).
- **`DailyReportService`** (1) — consolidación cross-caja de las 8 columnas.
- **`CategoryService`** (5) — árbol padre→hijo→nieto, huérfano = raíz, ciclo/
  self-parent → 409, parent inexistente → 400.

Además se adaptaron `AuthCtlTest` y `OrderCtlTest` al contrato endurecido en
Fase 4 (mensaje genérico de login, 429 por throttle, `getOrder` por principal +
test de IDOR con param ajeno ignorado).

**Huecos anotados** (no bloquean, endurecer a futuro): validación de duplicados
contra-BD en los imports (nombre/RTN ya existentes) se probó por fila pero no con
resultados no-vacíos del `NamedParameterJdbcTemplate` mockeado.

---

## 2. E2E de escenarios de negocio

Script reproducible `scratchpad/e2e_hito1.sh` contra la API viva. Corre el flujo
completo y **reversa lo que muta** (anula la venta de prueba). **17 PASS / 0 FAIL.**

1. **Venta de contado** (cajero `tecnico`, caja 1): `POST /invoices` contado de
   2×L20 → factura #738, total **40.00**, **kardex descontó 2 uds** (10→8).
2. **Ticket fiscal**: la respuesta trae el bloque fiscal real de la caja
   (`numeroFiscal 000-001-01-00000738`, CAI `PRUEBA`, rango, `restantes 62`).
3. **Cierre de caja**: `GET /cierre-caja/resumen` del cajero refleja la venta en
   `ventaEfectivo`.
4. **Reporte diario admin**: `GET /reports/daily` ve la venta del día y su total
   **cuadra con `/invoices/admin/summary`** (US-099) — 40.00 = 40.00.
5. **Anulación total** (admin, clave supervisor): `POST /sale-returns/annul/738`
   → 200, **kardex repuesto** (8→10), factura queda `NULA`.
6. **CxC**: `GET /accounts-receivable/{id}/balance`, `/{id}/statement`,
   `/delinquent` → 200.
7. **Seguridad (Fase 4) activa**: headers `X-Frame-Options: DENY` + CSP presentes,
   login inválido → "Credenciales inválidas" (genérico), endpoint público del QR
   recorta el login del cajero (`usuario: null`).

---

## 3. Regresión decimal (Fase 1)

- `information_schema`: **cero columnas float/double base** en el esquema común.
  Las 2 que aparecen son de **vistas** (`v_clientes.saldo`, `v_proveedores.saldo`),
  donde MySQL tipa como `double` el resultado de un `SUM()`; las columnas base
  (`cliente.saldo`, etc.) son `decimal`. No hay pérdida de precisión (se computa
  al vuelo desde bases DECIMAL). **Benigno, esperado.**
- `schema_version` al día: última = **V38**, `success=1`.
- El smoke del Flyway del Swing (Fase 1) ya probó que V33–V37 aplican desde un
  estado cliente V32/float sin descuadres.

## 4. Matriz jar-viejo / BD-nueva

Verificación a nivel SQL simulando las operaciones del DAO viejo del Swing contra
el esquema V38 (todas OK):

- **INSERT por columnas explícitas omitiendo `parent_id`** (el jar viejo no conoce
  la columna V38) → funciona, `parent_id` queda NULL (aditiva, nullable).
- **UPDATE por columna** (no toca `parent_id`) → funciona.
- **`SELECT *` sobre `marcas`** con la columna extra → las columnas que el DAO
  viejo lee por nombre (`descripcion`/`observacion`) no se ven afectadas.
- **`setDouble` sobre DECIMAL**: un double "sucio" (889.10003…) cae limpio en
  `DECIMAL(15,2)` → 889.10, sin error (JDBC get/setDouble sobre DECIMAL es seguro).

Sumado a que las migraciones son **solo aditivas o retype** (nunca rename/drop) y
a `ignoreFutureMigrations`, un Swing con jar viejo coexiste con la BD nueva. **Lo
único que falta es una corrida real del Swing** (GUI JDBC) contra la BD migrada —
queda para el smoke del usuario, es la validación "en vivo" que no se puede
reproducir headless.

---

## 5. Checklist de smoke MANUAL (pendiente del usuario)

Lo que necesita ojo humano / navegador / la GUI y no se puede automatizar acá:

**POS (React)**
- [ ] Categorías: crear jerarquía padre→hijo, mover una rama, borrar con hijos
      (debe avisar 409), y ver el árbol expandir/colapsar.
- [ ] Import de productos y clientes: descargar plantilla, subir un archivo con
      errores (ver el reporte por fila) y uno válido (confirmar → importa).
- [ ] Reporte diario: abrir `/reports/daily`, imprimir (verificar que el override
      de `@page` no queda pegado al 80mm del ticket) y exportar CSV.
- [ ] Flujo QR: hacer una venta, ver el QR en el ticket, escanearlo con el
      celular → abre `/f/:caja/:numero` sin sesión, muestra la factura con
      membrete, "Imprimir copia" funciona, y el token desaparece de la URL.

**Swing (JavaFX)**
- [ ] Arrancar un jar reciente contra la BD local V38 y facturar/cerrar caja una
      vez (confirma la matriz jar/BD en vivo y que Flyway `repair()`+`migrate()`
      no reprocesa V33-V38).

**Deploy (operativo, no código)**
- [ ] Rotar `APP_JWT_SECRET` por cliente (crítico OWASP C1) y setear
      `APP_PUBLIC_INVOICE_SECRET` si se usa el QR.
- [ ] Configurar CAI/rango por caja en los clientes que emitan fiscal.

---

## DoD Fase 5

✅ Documento de pruebas con casos críticos cubiertos y resultados. ✅ Automatizado
verde (136). ✅ E2E de negocio verde (17). ✅ Regresión decimal y matriz jar/BD
verificadas a nivel SQL. ⬜ Smoke manual del usuario (navegador + 1 corrida Swing)
y rotación de secretos en el deploy.
