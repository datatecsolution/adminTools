# Runbook — Sharon (servidor de Ronal): V48 → V57 + jar del 5° precio

> 2026-10-03. BD de producción de **Distribuidora Sharon** en el MySQL nativo del servidor de Ronal (10.10.0.1:3306, fuera de Docker). La comparten las 4 terminales Swing (192.168.88.242, .243, .246 y .248), la API `admin-tools-api-v2` y la app de pedidos (`pedidos.distribuidorasharon.com`).

## 1. Qué se despliega

- **Jar del Swing de `master` `ae9cc82`**:
  - 5° precio (#126): flechas, «Seleccionar precio», Compras y formulario de artículo.
  - Atajos alternativos para macOS.
  - Fecha de Compras y Datos de facturación (#127).
- **Migraciones V49 a V57** de la BD común. El Swing las aplica solo al arrancar, así que se aplican antes, de forma controlada:

| Versión | Qué hace | Riesgo |
|---|---|---|
| V49 | Tablas `report_schedules` y `report_send_log` | Ninguno (nuevas) |
| V50 | Tabla `articulo_galeria` | Ninguno (nueva) |
| V51–V52 | Columna `config_user_facturacion.catalogo_parrilla` (por defecto 0) | Bloqueo breve de la tabla |
| V53 | Vista `v_existencia_alert` (alerta de inventario, US-176) | Solo la vista |
| V54–V55 | Tablas de consolidados de pedidos y su relleno (vacío en Sharon) | Ninguno |
| V56 | Columna e índice `usuario.codigo_cliente` | Bloqueo breve de la tabla |
| V57 | Columna `config_user_facturacion.app_permite_descuento` (por defecto 1) | Bloqueo breve de la tabla |

Las cajas 1–7 ya están en V9, la última, así que no tienen migraciones pendientes.

> La API y la app de pedidos de Sharon están en la versión del 5-sep (US-150). Este despliegue **no las actualiza** (opción A). Actualizarlas, la API sobre todo (83 commits atrás), es una ventana aparte con su propio ensayo.

**No se tocan la API ni la app de pedidos.** La app de pedidos tiene restricción de despliegue. La API (`sharon-481b4b0`) se probó contra el esquema V57 en el ensayo.

## 2. Estado de partida (2026-10-03 14:20, lectura)

| | |
|---|---|
| Común | V48 (2026-09-05), 0 fallidas |
| Cajas 1–7 | V9, 0 fallidas |
| MySQL | 8.0.42, `log_bin=1`, `log_bin_trust_function_creators=1` |
| API | `admintools-api:sharon-481b4b0` (US-150, 5-sep) |
| Tamaño | ~755 MB en total (común 426 MB, caja 2 284 MB) |

## 3. Ensayo local (hecho el 2026-10-03)

1. Volcado de solo lectura de las 8 BDs: 11 s, 79 MB comprimido, sha256 `24a0d283…`, «Dump completed», 8 `CREATE DATABASE`.
2. MySQL 8.0 aparte en la Mac (puerto 3307, nombres exactos). Carga en 45 s, sin errores.
3. Huella: 99 tablas, 6.271.209 filas.
4. Runner real (`EnsayoMigrate` + jar `ae9cc82`, que trae hasta V57): **9 migraciones en 0,13 s**. Cajas: «up to date». 0 fallidas.
5. Después:
   - ninguna tabla existente cambió de conteo;
   - `schema_version` pasó de 48 a 57;
   - 6 tablas nuevas, vacías;
   - `v_existencia_alert` devuelve 2.265 filas y es `INVOKER`.
6. **API de producción `481b4b0`**, construida en el commit exacto y con perfil `pdn` (`ddl-auto=validate`), contra la copia en V57: **Started en 4,4 s**, sin errores de validación. `/inventory` → 401 (pide sesión).
7. **Contrato de la app de pedidos.** Se repitieron las rutas que la app usa en producción, sacadas de los logs del proxy host 3 (~3 semanas): `products/description` (47.167 llamadas), `customers` (4.241), `orders/save` (1.504), `orders/today` (722), `auth/refresh` y `auth/login`, `orders/delete` (12).
   - Se corrieron contra la API `481b4b0` sobre una copia en **V48** y otra en **V57**, con un vendedor real (clave de prueba solo en las copias).
   - Se buscaron 10 términos, se listaron clientes y pedidos de hoy, se clonó, guardó y borró un pedido real.
   - Resultado: **19/19 pasos idénticos** (status y contenido).
   - La app es un front estático (nginx) que solo habla con `admin-tools-api-v2`; no se conecta a la BD.

## 4. Ventana

**Cuándo:** con las terminales **sin operar**: fuera de horario, o confirmando con el cliente que nadie está facturando. La migración dura menos de un segundo. Pero los `ALTER` de `usuario` y `config_user_facturacion` necesitan un bloqueo de metadatos, y una terminal con una transacción abierta los haría esperar y frenaría a las demás. Las terminales pueden quedar abiertas, sin uso.

| Paso | Dónde | Comando | Criterio |
|---|---|---|---|
| 1. Preflight | servidor | `bash ~/deploy-sharon/v57/v57.sh preflight` | V48, 0 fallidas, **0 transacciones abiertas** |
| 2. Respaldo | servidor | `bash ~/deploy-sharon/v57/v57.sh backup` | gzip íntegro, «Dump completed», 8 BDs, sha256 y huella guardados |
| 3. Migrar | Mac | `bash docs/deploy-sharon/v57/migrar-v57.sh <jar ae9cc82> <runner>` | `Successfully applied 9 migrations`, rc=0, sin esperas por bloqueo |
| 4. Verificar | servidor | `bash ~/deploy-sharon/v57/v57.sh verificar` | V57, 0 fallidas, conteos iguales, 6 tablas nuevas, API sin errores, dominio 200 |
| 5. Jar | terminales | copiar `AdminTools-1.0_sharon_ae9cc82.jar` (en `~/deploy-sharon/`) a las 4 terminales, **guardando el jar anterior** | Abre sin el aviso de migraciones; **Ctrl+↑** y Ctrl+D abren «Seleccionar precio» |
| 6. 5° precio | POS/BD | crear el tipo de precio **después** de que las 4 terminales tengan el jar nuevo | El cliente carga los valores (a mano o con el importador) |

**Vigilancia:**
- Durante la migración, `migrar-v57.sh` registra cada segundo las consultas que esperan un bloqueo de metadatos.
- Después, revisar los logs de la API a los 15 minutos y a las 2 horas, y barrer los 4xx/5xx del proxy (proxy host 3).
- La API no se reinicia, así que no hace falta pausar el vigilante de Docker.

## 5. Vuelta atrás

| Problema | Qué hacer | Pérdida de datos |
|---|---|---|
| Falla el Swing nuevo en una terminal | Volver a poner el jar anterior en esa terminal. Con la BD en V57 abre igual: muestra el aviso «No se pudieron aplicar las migraciones» y sigue. Las migraciones son aditivas | Ninguna |
| Falla la migración a la mitad | El runner es reanudable (`repair()` + `migrate()`). Corregir y volver a correr `migrar-v57.sh` | Ninguna |
| La BD quedó dañada | `bash ~/deploy-sharon/v57/v57.sh rollback`: verifica el sha256, detiene la API, restaura las 8 BDs y levanta la API. **Terminales sin operar** | Todo lo registrado después del respaldo |

El restore completo es el último recurso. Las migraciones solo agregan, y ni la API ni el jar anterior dependen de ellas.

## 6. Avisar al cliente

- Ya no se puede guardar ni actualizar un artículo **sin precio Público General**.
- En Compras, escribir en P/venta 2 o P/venta 3 **crea** el precio si el artículo no lo tenía.
- En Datos de facturación, al modificar solo se cambian CAI, código, cantidad y fecha límite.
- Los atajos no cambian. Solo se agregaron alternativas: Ctrl+D = Ctrl+↑ y Ctrl+número = tecla F.

## 7. Registro de la ejecución

### Ejecución del 2026-10-03 (BD: hecha · jar: pendiente)

| Hora (local) | Paso | Resultado |
|---|---|---|
| 14:39:31 | Preflight | V48 / cajas V9, 0 fallidas, **0 transacciones abiertas**; 3 terminales y la API conectadas (las terminales operando) |
| 14:39:36–14:40:00 | Respaldo | `~/deploy-sharon/backup/sharon_pre_v57_20261003_143936.sql.gz`: 76 MB, gzip íntegro, «Dump completed», 8 BDs, sha256 guardado. Huella: 99 tablas, 6.271.254 filas |
| 14:40:08–14:40:29 | Migración (Mac → túnel → runner `ae9cc82`) | **9 migraciones en 6,2 s**, rc=0, cajas «up to date», **sin esperas por bloqueo** |
| 14:40:36 | Verificación | Común **V57**, 0 fallidas; **ninguna tabla existente cambió de conteo**; 6 tablas nuevas; alerta 2.265 filas `INVOKER`; API sin errores; dominio 200 |
| 14:41 | Vigilancia (`vigilar.sh 20:40`) | App de pedidos: 14 búsquedas de producto, todas 200; 0 errores en la API |

Pendiente: jar en las 4 terminales (guardando el anterior) → Ctrl+↑ → crear el 5° precio. Vigilancia a las +2 h (`bash ~/deploy-sharon/v57/vigilar.sh 20:40`).
