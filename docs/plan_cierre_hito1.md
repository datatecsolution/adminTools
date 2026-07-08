# Plan de cierre técnico — Hito 1

> **Meta:** dejar el sistema completo (Swing + admintools-core + API + POS + app-pedidos)
> con todas las features de Hito 1 terminadas y **verificado de punta a punta**.
>
> **Fuera de alcance** (decisión 2026-06-30): todo lo del despliegue a Urbina →
> US-050 (infra VPS/SSL/backups), US-052 (deploy a producción), US-053 (capacitación),
> US-054 (manuales/videos/acta + cobro). También fuera: la config operativa de CAI/rango
> por caja (es per-cliente, va con el deploy).
>
> Backlog fuente: [`historias_urbina_ola1.csv`](./historias_urbina_ola1.csv). Marcar cada
> US como `Terminado` ahí cuando se cierre, además del checkbox de este archivo.

---

## Tablero de avance

| Fase | Descripción | Estado |
|------|-------------|--------|
| 0 | Reconciliar backlog (verificar ya-hechas) | ✅ Hecho (2026-07-05: las 4 cubiertas, −23 SP) |
| 1 | Hardening BD: float→DECIMAL (US-070..073) | 🟡 En curso (US-070 ✅ validado local, sin deploy) |
| 2 | Completar features admin/POS (US-081/043/044/047/100) | 🟡 En curso (US-079 ✅ merged 2026-07-08 · US-101 ✅) |
| 3 | Hardening app de pedidos (US-074..077) | ⬜ Pendiente |
| 4 | Seguridad — auditoría OWASP (US-049) | ⬜ Pendiente |
| 5 | Pasada de prueba integral (objetivo "probado") | ⬜ Pendiente |

Leyenda: ⬜ Pendiente · 🟡 En curso · ✅ Hecho

**Tamaño estimado:** ~~72 SP~~ → **49 SP de build + QA** (Fase 0 confirmó las 4
sospechosas el 2026-07-05: −23 SP; residuales menores anotados en el CSV, no
bloquean). Además se completó la US-101 (8 SP, agregada fuera del estimado).

**Secuencia recomendada:**
```
Fase 0  →  Fase 1 (con backups)  →  Fase 2 + Fase 3 (en paralelo)  →  Fase 4  →  Fase 5
```

---

## Fase 0 — Reconciliar el backlog
Antes de construir, confirmar qué ya está hecho pero figura `Backlog` en el CSV.
Sospechosas de estar cubiertas por rediseños posteriores:

- [x] **US-046** (gestión CxC) — ✅ cubierta por US-097 + US-092/093 (cartera, abonos a cliente y por factura, estado de cuenta). Residual: estado de cuenta **imprimible** no existe.
- [x] **US-080** (barcode en ProductForm) — ✅ cubierta por Rediseño Productos: input agrega con Enter (lector USB HID OK), duplicados local+409, búsqueda por código (searchByNameCodeBarcode). Sin residuales.
- [x] **US-082** (vista de saldos) — ✅ cubierta por US-097 (Saldo/Límite/Disponible + sobregiro + cartera con KPIs). Residual: **aging buckets 0-30/31-60/61+** no existen (solo # vencidas).
- [x] **US-083** (histórico de facturas del cliente) — ✅ cubierta entre AccountDrawer (pendientes + detalle) y /facturas (histórico completo, búsqueda por cliente server-side, anuladas marcadas). Residual: atajo cliente→/facturas prefiltrado (~1 SP).

**DoD:** ✅ CSV actualizado con la realidad (2026-07-05). Pendiente de build: 23 US → **19 US / 49 SP**.
Residuales anotados (imprimir estado de cuenta · aging buckets · atajo histórico): decidir en Fase 5 si entran o se descartan.

---

## Fase 1 — Hardening de BD: float → DECIMAL
Lo más invasivo (toca Swing + core + API). Hacerlo primero para que el resto se pruebe
sobre el esquema final. Serán migraciones **V34+** (común) / **V10+** (caja) —
V33/V9 ya las consumió el fix de sobreventa (investigación Ronal cat. 107,
mergeado 2026-07-03). REGLA US-101: toda migración de caja nueva se copia al
espejo de la API (`admintools-api/src/main/resources/db/migration/caja`).

**Reglas (de la investigación de Flyway 6.5.7):**
- Migraciones **solo retype** (`MODIFY ... DECIMAL`), **sin renombrar ni dropear** columnas → un Swing viejo sigue arrancando (retype es JDBC-safe; `ignoreFutureMigrations=true` por default).
- Auditoría `information_schema` pre-migración en cada lote (sin valores fuera de rango).
- Verificar que ningún DAO viejo haga `INSERT` posicional en las tablas tocadas.
- Backup completo antes de cada lote.
- **Precisión estándar: `DECIMAL(15,2)`** (rango ±9.999.999.999.999,99; caja V7 usó lo mismo).
- **Regla de espejo API (¡crítica, descubierta en US-070!):** las entidades JPA de la API tienen `@JdbcTypeCode(SqlTypes.REAL)` en las columnas que aún eran float, para que `ddl-auto=validate` no exija DECIMAL. **Al migrar cada lote hay que QUITAR esa anotación** de las entidades correspondientes o la API no arranca ("found decimal, expecting real"). Mapeo entidad→lote:
  - US-070: `ArticuloKardex` ✅ (hecho)
  - US-071: `CuentaPorCobrar`, `CuentaPorCobrarFactura`, `ReciboPago`
  - US-073: `CierreCaja`, `EntradaCaja`, `SalidaCaja`
  - (verificar además otras entidades al tocar cada tabla)
- **TODO EL TRABAJO DE FASE 1 SE VALIDA EN LOCAL** (`localhost/admin_tools`); no tocar clientes hasta validar todo y OK del usuario (regla 2026-07-08).

- [x] **US-070** — kardex/stock float→DECIMAL(15,2) *(medio · 5 SP)* — **✅ validado en local 2026-07-08** (V34 común: kardex, movimiento_kardex, articulo_kardex, articulo_bodega). `detalle_movimiento_kardex` no tenía floats; `existencia_articulo_bodega` ya era DECIMAL. Fix API: `ArticuloKardex` sin `@JdbcTypeCode(REAL)`. Datos preservados, vistas/SPs OK, API valida + `/inventory/*` responde. Ramas `feature/us-070-kardex-decimal` (Swing + API), **sin push ni deploy**.
- [ ] **US-071** — CxC, cuenta_factura, recibo_pago, cobro_factura, pagos_creditos → DECIMAL *(medio · 5 SP)*
- [ ] **US-072** — tabla `articulo` + **entidad `Articulo` a BigDecimal** (muchos call-sites: DAOs, controllers, aritmética) *(**alto** · 8 SP)*
- [ ] **US-073** — cierre_caja, entradas/salidas_caja, cuentas/movimientos_bancos, datos_factura + barrido global *(medio · 5 SP)*

**DoD:** cero columnas float monetarias en el esquema; facturación/cierre/CxC sin
descuadres; jar viejo arranca OK contra el esquema nuevo.

---

## Fase 2 — Completar features del panel admin / POS

- [x] **US-079** — Imagen de producto *(5 SP)* — **MERGEADO 2026-07-08** (api#25 → main, pos#32 → master). Desvío aprobado por simpleza de BD: en vez de columna `imageUrl` + `/upload`, se **reusa la tabla legacy `articulo_imagen`** (blob en BD, sin migración, sobrevive redeploys vía mysqldump). API `/products/{id}/image` (POST ADMIN → `{imageVersion}`; GET público con ETag/immutable/304; DELETE; `?size=thumb` → miniatura ~160px al vuelo, 5.8× menos bytes para conexión lenta) + `ProductResponse.imageVersion`; POS con cuadro de imagen 40×40 inline en el modal (clic/drag&drop), miniatura en la tabla, en el grid táctil y **en el catálogo + líneas del ticket de facturación**. Colateral: se arregló un blocker preexistente de Zod v4 que impedía guardar cualquier producto (`prices.record`). E2E por navegador (Playwright). NO desplegado en clientes aún.

  > **Nota — análisis de eficiencia del blob-en-BD (medido contra Ronal, 2026-07-05).** Ronal: 3.408 productos (2.883 activos), `articulo_imagen` en 0 filas, BD común `admin_tools` = 324,6 MB, suma de todas las BD = 650,9 MB, **buffer pool en 128 MB** (default sin tunear). Proyección con JPEG 600 px ≈ 60 KB: 30% de activos con foto = ~52 MB; 60% = ~104 MB; **100% de activos = ~173 MB** (común pasaría a ~498 MB, +27% del backup total). Para alcanzar el umbral de "separar backups" (~2 GB de imágenes) harían falta ~34.000 fotos = 10× el catálogo → **no aplica**. Clave: las imágenes están **acotadas por el catálogo** (crece lento), no por las ventas (`movimiento_kardex` ya tiene 1,34 M filas). **Veredicto: el blob-en-BD es la opción correcta para este perfil de cliente; no hay caso para filesystem ni object storage.**
  >
  > **Acciones al desplegar US-079 en clientes:** (1) excluir `articulo_imagen` del `mysqldump` diario (`--ignore-table=admin_tools.articulo_imagen`) y respaldarla aparte ~1×/semana; (2) subir el `innodb_buffer_pool_size` a 512 MB–1 GB (pendiente preexistente por el tamaño transaccional, no por las fotos); (3) a futuro, evaluar backups físicos (XtraBackup) que evitan el inflado hex de los blobs. *(Colateral detectado en Ronal, ajeno a imágenes: `detalle_factura_temp` = 544 k filas / 104 MB y `encabezado_factura_temp` = 90 k / 32 MB — órdenes temporales sin limpiar que ya inflan todos los backups.)*
- [ ] **US-081** — Categorías jerárquicas padre-hijo (Flyway `parent_id` self-FK en `marcas`; `GET /categories/tree`; UI árbol; selector con path "Padre > Hijo"; sin ciclos; borrar con hijos → 409) *(8 SP)*
- [ ] **US-043** — Importación masiva de productos Excel/CSV (plantilla descargable, validación previa, reporte de errores, hasta 5000 filas, rollback) *(5 SP)*
- [ ] **US-044** — Importación masiva de clientes Excel/CSV (plantilla con validación RTN, hasta 2000) *(3 SP)*
- [ ] **US-047** — Reporte diario consolidado (ventas, desglose por método de pago, descuentos, anulaciones; export PDF/Excel) *(5 SP)*
- [ ] **US-100** — Reimpresión de factura por QR (autoservicio) *(5 SP)*
- [x] **US-101** — Cajas + Datos de facturación CAI/rangos: endpoints backend réplica del Swing (provisioning de BD de caja + fiscal-ranges con ALTER AUTO_INCREMENT) *(8 SP · agregada 2026-07-02)* — **Terminado 2026-07-02** (api#22 → main 6bafe37, E2E verificado; pantalla POS queda como seguimiento)

**DoD:** cada una full-stack, mergeada vía PR, criterios de aceptación cumplidos y
verificada local.

---

## Fase 3 — Hardening de la app de pedidos (`at-ordenes-ventas`)

- [ ] **US-074** — **Lock pesimista anti-sobreventa** en `OrderService.save` (`SELECT articulo ... FOR UPDATE` dentro de `@Transactional`; conflicto → 409 con `[{productId, nombre, pedida, disponible}]`; React maneja 409). **API.** *(5 SP)*
- [ ] **US-075** — `authFetch → apiClient` robusto (validar `response.ok`, throw tipado, parsing solo si JSON; `AbortController` en búsquedas; retry de `/auth/refresh` con backoff 1s/3s/9s) *(5 SP)*
- [ ] **US-076** — Defensivo: helper `getUserSafe()` con try/catch (5 sitios de `JSON.parse(localStorage.user)`) + guard/UI de orden ya facturada (estado > 2) *(2 SP)*
- [ ] **US-077** — Sanitizar `console.log` en prod (helper `logger` gateado por `NODE_ENV`; bundle de prod sin logs sensibles) *(1 SP)*

**DoD:** test concurrente de US-074 (dos threads → uno OK, uno 409); jest del happy path
+ 1-2 paths de error; consola limpia en build de prod.

---

## Fase 4 — Seguridad (US-049 · 5 SP)
Auditoría OWASP top 10 del sistema (API + POS + pedidos).

- [ ] Headers de seguridad configurados
- [ ] Authorization por endpoint revisada (`@PreAuthorize`)
- [ ] Validación de inputs / manejo de JWT
- [ ] Log de auditoría activo
- [ ] Vulnerabilidades críticas resueltas

> Herramienta: correr `/security-review` sobre el diff de cada fase + una pasada global al final.

**DoD:** checklist OWASP completo; críticas resueltas.

---

## Fase 5 — Pasada de prueba integral (objetivo "probado")
Lo que convierte "código terminado" en "sistema listo".

- [ ] **Tests automatizados** donde hay huecos — agente `test-doc-generator` por módulo (FacturacionService, CierreCaja, CxC, inventario, conversión decimal).
- [ ] **E2E manual por escenario de negocio**, contra el stack **pruebas-dulce** (datos reales) y/o MySQL local:
      venta contado/crédito → ticket fiscal → cierre de caja → CxC → anulación total/parcial → reporte diario → pedido desde la app.
- [ ] **Regresión del Swing** (repetir tras el lote decimal de la Fase 1).
- [ ] **Matriz de versiones**: confirmar en vivo que un Swing viejo coexiste con la BD nueva (analizado teóricamente; falta probarlo una vez).

**DoD:** documento de pruebas con casos críticos cubiertos y bugs resueltos.

---

## Registro de decisiones / notas
- 2026-07-05 — US-101 TERMINADA full-stack (API + pantalla /cajas + regla de
  sobrepaso de rangos). Cierra el pendiente operativo CAI de US-040.
- 2026-07-03 — Fuera de plan: fix sobreventa kardex V33 común + V9 caja + guard
  compras-inactivos (investigación stock cat. 107 Ronal). Mergeado; falta
  desplegar a Ronal + limpieza de datos con el cliente.
- 2026-06-30 — Plan creado. Urbina-deploy explícitamente fuera de alcance.
- US-040 (ticket fiscal) ya cerrado y desplegado en venecia + pruebas-dulce (2026-06-29).
- Recordatorio Flyway: las migraciones deben ser **aditivas o con vista de retrocompat**
  para que terminales con jar viejo y nuevo coexistan sobre la común compartida.
