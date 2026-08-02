# ¿Mantener el Swing? — análisis de fondo

**Fecha:** 2026-08-02 · **Pregunta:** si la visión original era migrar al POS
React, ¿tiene sentido seguir invirtiendo en el Swing?

## 1. Respuesta corta

**Sí a corto plazo, pero con política de CONGELAMIENTO, no de mantenimiento.**
El Swing no puede morir hoy — es el dueño de las migraciones y cubre módulos
que el POS aún no tiene — pero cada línea nueva que se le escribe es deuda
sobre un activo en retiro. La inversión debe invertirse: hoy ~70% del
esfuerzo de features cae en rieles duplicados; la meta es que el 100% de lo
nuevo nazca en POS/API y el Swing solo reciba correcciones críticas y
migraciones.

La evidencia de que el reemplazo funciona ya existe: **venecia opera en
producción con el POS** desde junio, y dulce lo validó en hardware real
(táctil, TM-T20, iPad/iPhone).

## 2. El costo real del doble riel (datos, no impresiones)

**Esta misma semana:** US-125, US-126, US-127 y US-128 fueron íntegramente
trabajo Swing. El epic de stock reservado tocó BD + API + Swing + POS — cada
regla de negocio se implementó y verificó por triplicado (guard de sobreventa:
en el SP, en la API y en el DAO del Swing).

**La fragilidad del Swing es medible, no anecdótica** (todo de esta semana):
- Editar un usuario reventaba por un `SET usuario = NULL` latente desde abril
  (US-126); la pantalla de usuarios creaba vendedores rotos por SUS DOS
  caminos (US-128, costó 2 días de un vendedor).
- Renderers que leen columnas ocultas por índice fijo (US-120), DAOs con
  estado parcial, NPEs que rompen pantallas en silencio.
- Cada arranque aplica migraciones contra lo que apunte el `.dat` — así se
  produjo la deriva V42.

**El stack está congelado por fuera:** Java 8 + Gradle 4.10 + Flyway 6.5.7
(subir Flyway arrastra Java 17 + Gradle 8, estimado 1–2 semanas — diferido).
No hay HiDPI en pantallas modernas, no hay tests de UI posibles, y el pool
de gente que quiere mantener Swing en 2026 se achica cada año.

## 3. Mapa de paridad (medido sobre el código, 2026-08-02)

Pantallas del menú Swing vs cobertura POS/API real (líneas de código como
proxy de sustancia, no rutas vacías):

| Módulo Swing | POS | Estado |
|---|---|---|
| Facturar | `/facturacion` (6.793 loc, táctil+móvil) | ✅ **EN PROD (venecia)** |
| Cierres de caja | `/` cierre completo + docs de caja US-108 | ✅ EN PROD |
| Clientes + CxC (pagos, estado de cuenta, cartera) | `/customers` (US-097) | ✅ validado |
| Artículos / Categorías | `/products`, `/categories` (barcodes, jerárquicas) | ✅ validado |
| Buscar facturas (+ anulación, reimpresión) | `/facturas` (US-041/099) | ✅ validado |
| Cajas + Datos facturación (CAI) | `/cajas` (US-101) | ✅ validado |
| Inventario (toma física, valoración, kardex) | `/inventario`, `/inventory` | ✅ validado |
| Entradas/Salidas caja | movimientos del cierre | ✅ |
| Órdenes de venta | US-084 + app de pedidos | ✅ |
| Gestión compras / Proveedores | `/compras` (1.108 loc), `/proveedores` | 🟡 construido, **sin validar en prod** |
| Usuarios + Config usuarios | `/users` (1.119 loc), `/settings` | 🟡 construido, sin validar |
| Cotizaciones | stub con TODOs | 🔴 pendiente |
| Reportes (SAR ventas/compras, comisiones, ventas x usuario/vendedor) | `/reports/daily` (54 loc) | 🔴 **la brecha grande** |
| Empleados (alta/edición) | — | 🔴 sin cubrir |
| Rutas de entrega/cobro | — | 🔴 sin cubrir |
| Cuentas por pagar / Bancos | — | 🔴 sin cubrir |
| Interés a facturas vencidas | — | 🔴 sin cubrir |

Lectura honesta: **el corazón operativo (vender, cobrar, cerrar caja,
inventario, clientes) ya está cubierto y probado en producción.** Lo que
falta es back-office (reportería legal, compras en prod, empleados) y
módulos cuyo uso real hay que auditar antes de portar.

## 4. Lo que ata al Swing más allá de las pantallas

1. **Es el dueño de las migraciones** (repair+migrate en cada boot; la API
   solo valida). Retirar el Swing de una terminal sin resolver esto deja al
   cliente sin mecanismo de evolución de esquema. **Es EL proyecto
   estructural del retiro**: mover la propiedad a la API (que ya conoce los
   tenants por caja) o a un migrador CLI pequeño que corra en el servidor
   del cliente. Beneficio colateral enorme: desaparece la clase de
   accidentes "una terminal migró la base" (deriva V42, fallo E3).
2. **Impresión**: mitigado — el POS ya imprime ticket fiscal 80mm
   (browser-print US-040) y directo por WebUSB (US-107). Queda verificar los
   formatos carta/crédito de Jasper si algún cliente los usa.
3. **Plantillas Excel de carga** (productos con precio de costo): portable a
   la API cuando toque compras en prod.
4. **Costumbre de los operadores**: los cajeros de Sharon facturan en Swing
   hace años. El retiro es por cliente y con acompañamiento, no por decreto.

## 5. Opciones

| Opción | Veredicto |
|---|---|
| **A. Congelar Swing + retiro por módulo/cliente** | **Recomendada** — abajo |
| B. Statu quo (doble riel indefinido) | La más cara: cada feature ×2-3, cada bug ×2 superficies, y el gap nunca se cierra porque el esfuerzo se drena en mantener el viejo |
| C. Big-bang (apagar Swing en una fecha) | Inaceptable: reportería SAR sin cubrir = riesgo fiscal del cliente; y las migraciones no tienen dueño alternativo aún |

## 6. Recomendación: política de congelamiento + plan de retiro

**Política inmediata (no cuesta nada, ahorra desde hoy):**
- El Swing NO recibe features nuevas. Solo: (a) fixes críticos de operación,
  (b) migraciones de esquema (mientras siga siendo su dueño), (c) paridad
  mínima cuando un bug lo exija. Todo lo demás nace en POS/API.
- Autocrítica que valida la regla: US-127 (transferir cartera) se construyó
  esta semana EN SWING; bajo esta política habría sido una pantalla del POS
  sobre `/customers`. Es el ejemplo perfecto del reflejo a corregir.
- El canal de actualización (estrategia aparte) se mantiene con alcance de
  **herramienta de transición**: abarata distribuir los fixes del período
  dual; no es excusa para alargarlo.

**Plan de retiro por fases** (criterio de salida por módulo: cubierto E2E en
producción en ≥1 cliente):

1. **Fase R1 — validar lo ya construido**: `/compras`, `/proveedores`,
   `/users` en producción de venecia (el cliente POS-first). Con eso, el
   Swing de venecia queda solo para back-office residual.
2. **Fase R2 — el proyecto estructural**: mover las migraciones Flyway del
   Swing a la API (o migrador CLI). Hito: "primera terminal formateada sin
   Swing" en venecia.
3. **Fase R3 — reportería**: SAR ventas/compras, comisiones, ventas por
   vendedor en el POS (la base ya existe: reportes dulce morena +
   `/invoices/admin/summary`). Es la brecha que hoy impide a un contador
   soltar el Swing.
4. **Fase R4 — auditar antes de portar**: bancos, CxP, rutas, interés,
   cotizaciones. Medir uso real en los datos de cada cliente (como hicimos
   con MARIOH: 701 órdenes pero ninguna en 7 meses). Lo que nadie usa no se
   porta — se retira.
5. **Fase R5 — retiro por cliente**: venecia primero (ya vive en POS),
   dulce, Sharon al final (la operación más Swing-dependiente).

**Métrica de avance** honesta y simple: % de features nuevas del trimestre
que NO tocaron el repo Swing, y nº de terminales sin Swing instalado.

## 7. Qué NO estamos decidiendo hoy

Fechas de apagado. El plan es de dirección, no de calendario: cada fase
tiene criterio de salida verificable, y el ritmo lo marcan la validación en
prod y la capacidad. Lo único que sí empieza hoy, si se aprueba, es la
política de congelamiento — que es gratis.
