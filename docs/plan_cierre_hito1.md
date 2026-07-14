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
| 1 | Hardening BD: float→DECIMAL (US-070..073) | ✅ Validado en local (US-070/071/072/073; esquema común con **cero float**). Sin push ni deploy — pendiente OK del usuario |
| 2 | Completar features admin/POS (US-081/043/044/047/100) | ✅ Full-stack validado en local 2026-07-13, sin push (US-079 ✅ merged · US-101 ✅). Pendiente smoke de navegador (imports, print del reporte, flujo QR) |
| 3 | Hardening app de pedidos (US-074..077) | ✅ Hecho en local 2026-07-13 (sin push) |
| 4 | Seguridad — auditoría OWASP (US-049) | ✅ Auditada + arreglos en local 2026-07-14 (sin push). Código limpio en Fases 1-3; deuda heredada arreglada. 2 críticas operativas del deploy (rotar secretos) documentadas |
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
  - US-071: `CuentaPorCobrar`, `CuentaPorCobrarFactura`, `ReciboPago` ✅ (hecho)
  - US-072: `Articulo.precioVenta`, `ArticuloMaster.precioArticulo` ✅ (hecho) — **caso inverso:** eran `Double` sin anotación (mapeaban DOUBLE); hay que **AGREGAR** `@JdbcTypeCode(SqlTypes.DECIMAL)`, no quitar REAL (mismo patrón que `existencia`). `precios_programados` no está mapeada.
  - US-073: `CierreCaja`, `EntradaCaja`, `SalidaCaja`
  - (verificar además otras entidades al tocar cada tabla)
- **TODO EL TRABAJO DE FASE 1 SE VALIDA EN LOCAL** (`localhost/admin_tools`); no tocar clientes hasta validar todo y OK del usuario (regla 2026-07-08).

- [x] **US-070** — kardex/stock float→DECIMAL(15,2) *(medio · 5 SP)* — **✅ validado en local 2026-07-08** (V34 común: kardex, movimiento_kardex, articulo_kardex, articulo_bodega). `detalle_movimiento_kardex` no tenía floats; `existencia_articulo_bodega` ya era DECIMAL. Fix API: `ArticuloKardex` sin `@JdbcTypeCode(REAL)`. Datos preservados, vistas/SPs OK, API valida + `/inventory/*` responde. Ramas `feature/us-070-kardex-decimal` (Swing + API), **sin push ni deploy**.
- [x] **US-071** — CxC float→DECIMAL(15,2) *(medio · 5 SP)* — **✅ validado en local 2026-07-09** (V35 común: `cliente.saldo`, `cuentas_por_cobrar`, `cuentas_por_cobrar_facturas`, `pagos_creditos`, `recibo_pago` — OJO typo legacy `saldo_anterio`). Fix API: `CuentaPorCobrar`/`CuentaPorCobrarFactura`/`ReciboPago` sin `@JdbcTypeCode(REAL)` (`cliente.saldo` es `@Transient`, `pagos_creditos` no mapeada). Datos preservados (checksums iguales), API valida + `/accounts-receivable` balance/statement/delinquent OK. Rama `feature/fase1-decimal` (Swing + API; renombrada desde `us-070`), **sin push ni deploy**.
- [x] **US-072** — precio de artículo float→DECIMAL(15,2) *(alto · 8 SP)* — **✅ validado en local 2026-07-09** (V36 común: `articulo.precio_articulo` double(10,2)→DECIMAL, `precios_programados.nuevo_precio` float→DECIMAL). **Riesgo real menor de lo estimado:** el precio de venta real ya vive en `precios_articulos` (DECIMAL(38,2), Sprint 4.5); `articulo.precio_articulo` es fallback legacy. En vez del refactor `Double→BigDecimal` en call-sites, se añadió `@JdbcTypeCode(DECIMAL)` a los 2 campos `Double` (patrón `existencia`). Vistas heredan DECIMAL. Datos preservados, API valida, lectura + round-trip de escritura de precio exactos. Rama `feature/fase1-decimal`, **sin push ni deploy**.
- [x] **US-073** — barrido final float→DECIMAL(15,2) del esquema común *(medio · 5 SP)* — **✅ validado en local 2026-07-09** (V37 común: cierre_caja(17), cuentas_bancos, cuentas_por_pagar, detalle_cotizacion, encabezado_cotizacion, detalle_factura, detalle_factura_temp, entradas/salidas_caja, insumos, movimientos_bancos, recibo_pago_proveedores). **Tras V37 el esquema COMÚN tiene CERO float/double base.** Fix API: `CierreCaja`(−17 REAL), `EntradaCaja`/`SalidaCaja`(−REAL), `DetalleOrden.precio`(+DECIMAL); `DetalleFactura` tenant ya BigDecimal. 11 checksums idénticos, API valida, `/cierre-caja/actual` + `/orders` OK, round-trip escritura entrada 12.34 exacto. Rama `feature/fase1-decimal`, **sin push ni deploy**.
  - **Residual (fuera de Fase 1, esquema CAJA):** `detalle_factura.cantidad` en las BDs por-caja sigue `float(11,2)` — `caja/V7` la dejó así explícitamente por "no ser monetaria". Si se quisiera cero-float también en caja: `caja/V10` (ALTER MODIFY, espejada a la API) + retest por cada caja_N. No bloquea el DoD ("cero float **monetario**"; `cantidad` es cantidad, no monto).

**DoD:** cero columnas float monetarias en el esquema; facturación/cierre/CxC sin
descuadres; jar viejo arranca OK contra el esquema nuevo.

**Estado DoD (2026-07-12):** ✅ esquema COMÚN con cero float/double base (V34–V37);
✅ datos preservados (checksums idénticos por lote); ✅ API arranca con `validate`
limpio contra V37.

**✅ Smoke Flyway del Swing (2026-07-12):** se reprodujo el `runFlyway()` real del
app (repair()+migrate(), Flyway 6.5.7 del classpath, `outOfOrder`, location common)
contra un **clon en estado cliente real V32/float** (`admin_tools_smoke`, revertido con
los backups pre_v34..37). Resultado: V33 (SP sobreventa) + V34–V37 aplicadas
`success=1`, cero float base después. Los warnings `1265 Data truncated` de la
conversión float→DECIMAL se **cuantificaron benignos**: delta 0.00 a 2 decimales
(join por PK real en `movimiento_kardex`, 474 filas) — solo se descartó ruido binario
sub-centavo. Además el resultado de Flyway == la migración manual (diff vacío). La BD
local viva quedó en el estado post-deploy (schema_version V37). *Nota:* el smoke cubrió
la **capa de migración/DB** vía el Flyway del propio app; NO se manejó la GUI JavaFX
(facturación/cierre por pantalla) — el Swing usa JDBC `get/setDouble`, seguro sobre
DECIMAL, y la misma lectura/escritura se validó por la API.

⬜ *pendiente antes de deploy:* **OK explícito del usuario** para push + PR + deploy a
clientes (opcional: click-through manual de facturación/cierre en la GUI del Swing).

---

## Fase 2 — Completar features del panel admin / POS

- [x] **US-079** — Imagen de producto *(5 SP)* — **MERGEADO 2026-07-08** (api#25 → main, pos#32 → master). Desvío aprobado por simpleza de BD: en vez de columna `imageUrl` + `/upload`, se **reusa la tabla legacy `articulo_imagen`** (blob en BD, sin migración, sobrevive redeploys vía mysqldump). API `/products/{id}/image` (POST ADMIN → `{imageVersion}`; GET público con ETag/immutable/304; DELETE; `?size=thumb` → miniatura ~160px al vuelo, 5.8× menos bytes para conexión lenta) + `ProductResponse.imageVersion`; POS con cuadro de imagen 40×40 inline en el modal (clic/drag&drop), miniatura en la tabla, en el grid táctil y **en el catálogo + líneas del ticket de facturación**. Colateral: se arregló un blocker preexistente de Zod v4 que impedía guardar cualquier producto (`prices.record`). E2E por navegador (Playwright). NO desplegado en clientes aún.

  > **Nota — análisis de eficiencia del blob-en-BD (medido contra Ronal, 2026-07-05).** Ronal: 3.408 productos (2.883 activos), `articulo_imagen` en 0 filas, BD común `admin_tools` = 324,6 MB, suma de todas las BD = 650,9 MB, **buffer pool en 128 MB** (default sin tunear). Proyección con JPEG 600 px ≈ 60 KB: 30% de activos con foto = ~52 MB; 60% = ~104 MB; **100% de activos = ~173 MB** (común pasaría a ~498 MB, +27% del backup total). Para alcanzar el umbral de "separar backups" (~2 GB de imágenes) harían falta ~34.000 fotos = 10× el catálogo → **no aplica**. Clave: las imágenes están **acotadas por el catálogo** (crece lento), no por las ventas (`movimiento_kardex` ya tiene 1,34 M filas). **Veredicto: el blob-en-BD es la opción correcta para este perfil de cliente; no hay caso para filesystem ni object storage.**
  >
  > **Acciones al desplegar US-079 en clientes:** (1) excluir `articulo_imagen` del `mysqldump` diario (`--ignore-table=admin_tools.articulo_imagen`) y respaldarla aparte ~1×/semana; (2) subir el `innodb_buffer_pool_size` a 512 MB–1 GB (pendiente preexistente por el tamaño transaccional, no por las fotos); (3) a futuro, evaluar backups físicos (XtraBackup) que evitan el inflado hex de los blobs. *(Colateral detectado en Ronal, ajeno a imágenes: `detalle_factura_temp` = 544 k filas / 104 MB y `encabezado_factura_temp` = 90 k / 32 MB — órdenes temporales sin limpiar que ya inflan todos los backups.)*
- [x] **US-081** — Categorías jerárquicas padre-hijo *(8 SP)* — **✅ validado en local 2026-07-13.** **V38** común (`marcas.parent_id` INT UNSIGNED NULL + self-FK; aditiva — `CategoriaDao` usa columnas explícitas, jar viejo OK; el FK protege el DELETE del Swing). API (commit `e983b46`): `GET /categories/tree` (huérfanos = raíces), `parentId` en DTOs, ciclos/self-parent → 409, parent inexistente → 400, DELETE con hijos → 409. E2E completo por curl. POS (commit `083f9fc`, rama `feature/us-081-categorias`): página de categorías en árbol (expandir/colapsar, indentación), selector de padre con path "Padre > Hijo" excluyendo la propia rama, selects de productos con path jerárquico.
- [x] **US-043** — Importación masiva de productos Excel/CSV *(5 SP)* — **✅ backend validado en local 2026-07-13** (commit `a4d80c6`, + POI/commons-csv). `GET /products/import/template` (CSV BOM) + `POST /products/import?dryRun=` (ADMIN, máx 5000, .csv/.xlsx): valida nombre (dup en archivo y BD — anti stock-fantasma), precio, categoría por nombre, impuesto 15/18/0/EXENTO, barcodes múltiples `|` con unicidad global; reporte `{row,column,message}`; **todo-o-nada** (400 con reporte y 0 importado si hay errores). Reusa `ProductMasterService.create`. E2E: 6 tipos de error, count intacto tras 400, import 2 filas OK, re-import detecta dups, .xlsx OK. POS: botón Importar + `ImportDialog` reutilizable (dry-run → reporte → confirmar), commit `ee05301` en `feature/fase2-pos`.
- [x] **US-044** — Importación masiva de clientes *(3 SP)* — **✅ backend validado en local 2026-07-13** (mismo commit): `POST /customers/import?dryRun=` (máx 2000), RTN 'CF'/14 dígitos con unicidad (archivo y BD), tipo CONTADO/CREDITO con las reglas del alta manual (crédito exige tel+dirección+límite>0). Reusa `CustomerService.create`. POS: comparte el `ImportDialog` (mismo commit `ee05301`).
- [x] **US-047** — Reporte diario consolidado *(5 SP)* — **✅ backend validado en local 2026-07-13** (commit `1494184`): `GET /reports/daily?date=&caja=` (ADMIN/INVENTORY) — ventas, efectivo/tarjeta/crédito (fórmulas del cuadre del cierre), desglose por tasa, descuentos y anulaciones, por caja + consolidado; corte por día calendario en `app.timezone`. Cuadrado contra SQL directo y `/invoices/admin/summary` (2026-06-09: 15/11.281,00). **Export = print-to-PDF + CSV client-side (decisión 2026-07-13, sin libs server-side).** POS: página `/reports/daily` (KPIs + tabla por caja + ISV, imprimir con override de `@page` sobre el CSS del ticket, export CSV client-side), commit `2c8b4f2`.
- [x] **US-100** — Reimpresión de factura por QR *(5 SP)* — **✅ backend validado en local 2026-07-13** (commit `357458c`). Sin migración: token HMAC-SHA256(caja|numero, `app.public-invoice.secret`) truncado a 128 bits; `GET /public/invoices/{caja}/{numero}?t=` público (404 con token inválido/ajeno/feature apagada); `qrToken`/`qrCaja` en los DTOs de factura para que el POS arme la URL. **Operativo por cliente: setear `APP_PUBLIC_INVOICE_SECRET` al desplegar** (vacío = QR apagado). POS: QR en Ticket80 (lib `qrcode`, data-URL precargado antes del flushSync) + página pública `/f/:caja/:numero` sin auth con membrete (la respuesta pública es `{invoice, empresa}` — `/company` NO se abrió al público), commits `520524b` + `c630a05`.
- [x] **US-101** — Cajas + Datos de facturación CAI/rangos: endpoints backend réplica del Swing (provisioning de BD de caja + fiscal-ranges con ALTER AUTO_INCREMENT) *(8 SP · agregada 2026-07-02)* — **Terminado 2026-07-02** (api#22 → main 6bafe37, E2E verificado; pantalla POS queda como seguimiento)

**DoD:** cada una full-stack, mergeada vía PR, criterios de aceptación cumplidos y
verificada local.

---

## Fase 3 — Hardening de la app de pedidos (`at-ordenes-ventas`)

- [x] **US-074** — **Lock pesimista anti-sobreventa** en `OrderService.save` *(5 SP)* — **✅ validado en local 2026-07-12** (API commit `6fc1439`, rama `feature/fase3-us074-lock-sobreventa`). Bloqueo **opt-in por usuario** con la misma semántica que el SP `crear_venta_kardex_v2` (V33): `facturar_sin_inventario=0` → exige stock; sin fila o =1 → histórico (cero impacto). `FOR UPDATE` sobre `articulo` (ids ascendentes) + `@Transactional(READ_COMMITTED)` — **gotcha real**: con REPEATABLE READ el read-view se crea en la primera lectura de la tx y el guard no veía el commit del competidor (los dos pasaban). Disponible = `f_existencia_y_ordenes` (kardex − pendientes, la cifra que ve el vendedor) + add-back de la propia orden en updates; solo artículos con kardex. 409 con `conflicts:[{productId,nombre,pedida,disponible}]`. **DoD verificado**: dos saves concurrentes → uno 201 y uno 409 (disponible 3.00); límite exacto pasa, +1 falla. React (app pedidos) maneja el 409 con toast por producto (commit `6ea21ff`).
- [x] **US-075** — `authFetch → apiClient` robusto *(5 SP)* — **✅ 2026-07-13** (commit `6870516`, rama `feature/fase3-hardening` de at-ordenes-ventas): `ApiError{status,body}`, parsing JSON solo si corresponde, `AbortController` en búsquedas de clientes/productos, refresh 401 con backoff 1s/3s/9s (solo fallos de red) y dedupe; `authFetch.js` eliminado.
- [x] **US-076** — Defensivo *(2 SP)* — **✅ 2026-07-13** (commit `8bc60a1`): `getUserSafe()` en los 6 sitios de `JSON.parse(localStorage.user)`; guard de orden procesada (estado > 2) al seleccionar y al re-guardar.
- [x] **US-077** — Sanitizar `console.log` en prod *(1 SP)* — **✅ 2026-07-13** (commit `37daed2`): `utils/logger` gateado por NODE_ENV, 17 sitios migrados, `console.log(username)` eliminado.

**DoD:** ✅ test concurrente de US-074 (dos requests simultáneas → 201 + 409); ✅ 22
tests verdes en 4 suites (apiClient, getUserSafe, logger, orderErrors); ✅ build de
prod OK con emisión de logs gateada. **Pendiente: push + PR (regla solo-local).**

---

## Fase 4 — Seguridad (US-049 · 5 SP) — ✅ auditoría hecha + arreglos en local 2026-07-14 (sin push)
Auditoría OWASP top 10 del sistema (API + POS + pedidos) vía **3 auditores en
paralelo** (API/auth/inyección, frontends, config). **Veredicto: el código nuevo
de Fases 1-3 está limpio** — sin inyección SQL (todas las queries cross-DB validan
el nombre de BD antes de concatenar; imports con binding), RBAC correcto en lo
nuevo, QR público bien diseñado (HMAC constant-time, 404 indistinguible). La deuda
real era **heredada**.

- [x] **Headers de seguridad** — CSP `frame-ancestors 'none'`, Referrer-Policy
  `no-referrer`, HSTS agregados en `SecurityConfig` (X-Frame-Options/nosniff ya
  venían); tres headers seguros en los nginx del POS y pedidos (CSP estricta queda
  como plantilla comentada, a validar contra el build de Vita).
- [x] **Authorization por endpoint** — cerrado IDOR en `GET /orders/{id}` (usaba
  `?user=` del atacante → ahora el JWT); `@PreAuthorize(ADMIN)` en
  `/products/save` y `/products/delete` legacy que estaban sin gate.
- [x] **Validación de inputs / JWT** — throttle anti-fuerza-bruta
  (`LoginAttemptService`, 10 fallos/5min por usuario+IP) en `/auth/login` y
  `/authorization/verify-admin`; login con mensaje genérico; `OrderCtl.save` deja
  de filtrar `e.getMessage()`. El filtro JWT ya fallaba cerrado (HS256 con firma
  verificada); Swagger apagado en `pdn`; `@CrossOrigin` hardcodeados removidos.
- [x] **Exposición de datos** — endpoint público del QR recorta el login del
  cajero y las facturas restantes del rango (`toPublic()`); `PublicInvoicePage`
  saca el token del historial (`history.replaceState`); CSV del reporte diario
  escapa contra formula-injection.
- [ ] **Log de auditoría activo** — DIFERIDO a Fase 5 (no había logging de
  auditoría; agregarlo es feature nueva, no fix de vulnerabilidad). El
  `GlobalExceptionHandler` ya loguea errores server-side sin filtrar internals.
- [x] **Vulnerabilidades críticas resueltas (código)** — todas las accionables en
  código, arregladas y verificadas E2E en local.

**Operativo del deploy (NO código, gate del usuario):**
- **C1 (crítica)**: **rotar `APP_JWT_SECRET` por cliente** — el default histórico
  es público en el repo; con él se forja un JWT de admin contra el host de internet
  (`pedidos.distribuidorasharon.com`). `DEPLOY.md` + `.env.example` ya corregidos
  para exigir `openssl rand -base64 48` único. **Verificar/rotar en Ronal.**
- **A2**: setear `APP_PUBLIC_INVOICE_SECRET` fuerte por cliente si se usa el QR.
- CORS de `pdn`: `CORS_ALLOWED_ORIGINS` solo con orígenes vivos y HTTPS.

Ramas: API `feature/us-049-owasp-hardening` (commits `81007f8` + `0e21d08`
docs), POS `feature/us-049-owasp-frontend`, pedidos `feature/us-049-owasp`.

**DoD:** ✅ auditoría OWASP completa; críticas de código resueltas y verificadas;
las 2 críticas restantes son operativas del deploy (secretos), documentadas.

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
- 2026-07-13 — Fases 2 y 3 construidas y validadas EN LOCAL (sin push). Ramas
  apiladas: API `feature/fase1-decimal` → `fase3-us074-lock-sobreventa` →
  `us-081-categorias` → `us-043-044-import` → `us-047-reporte-diario` →
  `us-100-qr-reimpresion` (apiladas porque `main` no arranca con validate
  contra la BD local V37+). POS: `feature/us-081-categorias` → `fase2-pos`.
  Pedidos: `feature/fase3-hardening`. Al aprobar el push, mergear en ese orden
  (o un PR por repo con todo).
- 2026-07-13 — US-047: export = imprimir→PDF del navegador + CSV client-side
  (sin JasperReports/POI server-side). US-043/044: acepta .csv y .xlsx (POI).
  US-100: URL pública firmada con HMAC (sin migración); requiere
  APP_PUBLIC_INVOICE_SECRET por cliente al desplegar.
- 2026-07-05 — US-101 TERMINADA full-stack (API + pantalla /cajas + regla de
  sobrepaso de rangos). Cierra el pendiente operativo CAI de US-040.
- 2026-07-03 — Fuera de plan: fix sobreventa kardex V33 común + V9 caja + guard
  compras-inactivos (investigación stock cat. 107 Ronal). Mergeado; falta
  desplegar a Ronal + limpieza de datos con el cliente.
- 2026-06-30 — Plan creado. Urbina-deploy explícitamente fuera de alcance.
- US-040 (ticket fiscal) ya cerrado y desplegado en venecia + pruebas-dulce (2026-06-29).
- Recordatorio Flyway: las migraciones deben ser **aditivas o con vista de retrocompat**
  para que terminales con jar viejo y nuevo coexistan sobre la común compartida.
