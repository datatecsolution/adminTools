# Plan: Farmacia La Fe pasa de demo a cliente en producción

**EJECUTADO el 2026-09-19 12:31–12:35** (ver «Cómo se ejecutó» al final). Estado original: Pedido del usuario: "va a dejar de ser
un demo y pasar a ser un cliente; falta rescatar la base de datos actualizada del
cliente y montarla".

## Punto de partida

| Pieza | Hoy |
|---|---|
| Stack `~/lafe/` en Ronal | `mysql-lafe` (loopback :3310), `api-lafe` (`lafe-4acf753`, **viejo**), `pos-lafe` (`lafe-5499df2`, **viejo**), dominio `https://lafe.datatecsolution.com` (proxy host 17 del NPM) |
| BD | respaldo del cliente del **2025-06-18** (última factura 2025-06-16): 1,900 artículos, 824 clientes, 41,226 facturas en 2 cajas (`caja_prueba` → `admin_tools_caja_1`, `caja_SAR` → `admin_tools_caja_2`). Esquema en **V49** (baseline V1 + V2→V49 con el runner real; el cliente era pre-Flyway) |
| Usuarios | 8 en `usuario` (tecnico 2, admin 4, ventas 3, ISIS/FABIO/Ayjani/CESIA DIAZ/cesia = cajeros). Las claves del dump venían **en texto plano** (Swing viejo): en el demo se generaron BCrypt solo para `admin`/`tecnico` |
| Terminal | `caja1-lafe` (OptiPlex, no táctil) con fases 1–2 hechas; falta la ticketera (fase 3) y el endurecimiento (fase 4) |
| Vigilante | `docker-watch.sh` ya vigila los contenedores y el dominio de La Fe (etiquetados "demo") |
| El cliente | sigue facturando con el **Swing viejo** y MySQL local en la farmacia (los dumps del demo tienen el formato de respaldo automático `admin_tools_AAAA-MM-DD_HHhMMm.<día>.sql.gz`) |

## Decisiones que necesito del cliente/usuario

1. **Cómo rescatar la BD**: (a) copiar el último respaldo automático del PC del Swing
   (USB/Drive); o (b) **dump directo desde el MySQL del PC de la farmacia** usando
   `caja1-lafe` como puente cuando esté en esa LAN (necesito la IP del PC —no se
   escanea la red— y las credenciales del Swing, que van en `connection.dat` cifrado y
   se pueden descifrar en la Mac). (b) es lo ideal porque el corte es limpio: última
   venta en el Swing → dump → carga → abrir el POS.
2. **Fecha/hora de corte** (fuera de horario; ~1 h de trabajo real).
3. **Caja fiscal de la terminal**: ¿la caja del OptiPlex es `caja_SAR` (2) o una
   nueva? Rangos **CAI** vigentes, numeración donde va, y **RTN/datos de empresa**
   (en el demo el RTN estaba "en trámite").
4. **Usuarios**: qué cajeros siguen activos y **contraseñas nuevas** (las viejas no se
   pueden reutilizar: hay que generar BCrypt; mejor que cada quien ponga la suya el
   primer día desde el admin).
5. **El Swing**: ¿queda apagado tras el corte o en paralelo solo lectura? (Mi
   recomendación: apagado; dos sistemas facturando duplican numeración.)
6. **Ticketera**: modelo, para la fase 3.

## Secuencia de ejecución (día del corte)

0. **Preparación (antes, sin ventana)**
   - Reconstruir `api-lafe`/`pos-lafe` desde `main`/`master` actuales
     (`admintools-api:lafe-<hash>`, `admintools-pos:lafe-<hash>` con
     `VITE_API_BASE_URL=https://lafe.datatecsolution.com/admin_tools/api`), sin
     levantar todavía. Traen US-168..180 (empleados, cierres, órdenes, SAR, US-179
     efectivo neto, US-180 costo oculto…).
   - Fat-jar del Swing desde `master` (V53) para el runner `EnsayoMigrate`.
   - **Respaldo del stack demo** (`~/lafe/backups/pre-prod_<ts>.sql.gz`, verificado):
     rollback y referencia.
1. **Rescate**: dump de las 3 BDs del cliente (`--single-transaction --routines
   --triggers --events --set-gtid-purged=OFF`), quitar `DEFINER` con `sed`
   (ERROR 1449 si no), `scp` a `~/lafe/dumps/prod_<fecha>/`, verificar
   (`Dump completed`, conteo de facturas vs lo que muestre el Swing).
2. **Carga**: `docker stop api-lafe pos-lafe` → `DROP` + carga de las 3 BDs en
   `mysql-lafe` (los grants de `apilafe` sobreviven al DROP DATABASE) →
   **runner real por túnel** (`ssh -L 13310:127.0.0.1:3310`): baseline V1 y
   V2→V53 + cajas V9 (~2–3 min; en el demo fueron 0 fallidas).
3. **Ajustes de datos (desde el POS, no por SQL salvo lo inevitable)**:
   claves BCrypt de `admin` (única por SQL/htpasswd con `docker exec -i`), luego
   todo desde Usuarios/Empleados/Cajas: cajeros activos y sus claves, caja de la
   terminal y su rango CAI, datos de empresa (`datos_factura`), tipo de precio
   por defecto. Verificar `existencia_articulo_bodega` poblada (V18 backfill) y
   `v_existencia_alert` (V53).
4. **Levantar** con las imágenes nuevas; API `Started` con validate limpio;
   dominio 200/401 sin tocar el NPM (pinnear IP de `api-lafe` en el compose como
   dulce/samuel si el recreate cambia la IP).
5. **Verificación**: conteos (artículos, clientes, facturas por caja, última
   factura) idénticos al dump; login de admin y de un cajero; Analítica del último
   mes cuadra con el Swing; **venta de prueba y anulación** en la terminal con
   ticket impreso; cierre de caja de prueba.
6. **Cutover**: Swing apagado (o solo lectura), `caja1-lafe` en producción;
   etiquetas del vigilante de "demo" a "prod"; memoria/docs actualizados.

## Riesgos y notas

- El dump de 2025 cargó bien (MySQL 8 destino); si el cliente cambió de versión de
  MySQL o de collation, se ve en la carga (misma receta que Wyc/La Fe demo).
- Usuarios con `codigo_empleado` inexistente o cajeros sin empleado → US-178 ya lo
  cubre (409 claro; se corrige desde Usuarios).
- Facturas viejas con `cobro_efectivo` = billete (si el Swing viejo lo hacía así,
  no aplica: el Swing guarda neto). US-179 protege los cierres nuevos.
- Numeración fiscal: si el Swing sigue facturando después del dump, esas facturas
  se pierden → el dump se hace **después de la última venta** del corte.


## Cómo se ejecutó (2026-09-19)

- **Rescate de la BD**: el PC del cliente (`fabio-OptiPlex-3050`, Ubuntu 24.04, MySQL 8.0.46)
  no arrancaba (`initramfs`, `UUID… does not exist`): la BIOS estaba en *SATA Operation =
  RAID On* y el NVMe quedaba remapeado (`ahci: Found 1 remapped NVMe devices`). Diagnóstico
  desde un Ubuntu live por SSH; con **AHCI** arrancó. Dump definitivo con
  `sudo mysqldump --defaults-file=/etc/mysql/debian.cnf --single-transaction --routines
  --triggers --events --set-gtid-purged=OFF --databases <db>` (lo corrió el usuario con `!`).
  `admin_tools_caja_2` ya no existía en ese MySQL → se cargó la de 2025-06-26 del respaldo
  `automysqlbackup` de la USB. Última factura del cliente: **#53266 del 2026-09-16 14:26**
  (no hubo ventas después: el dump es el corte completo). `fstab` de la USB de respaldos
  con `nofail`.
- **Kit del corte** en Ronal `~/lafe/corte/`: `01-respaldo.sh` (respaldo del demo
  `pre-corte_20260919_123101.sql.gz`), `02-cargar.sh <dir>` (DROP + carga con `sed` de
  DEFINER, 8 s), runner `EnsayoMigrate` desde la Mac por túnel `:3310` con el fat-jar de
  master (**V1→V53 + cajas V9, 49 s, 0 fallidas**), `03-levantar.sh` (compose `.corte`
  con `admintools-api:lafe-18c0e41` / `admintools-pos:lafe-4776863` y la IP de `api-lafe`
  pinneada en `172.25.0.18` → sin 502, sin tocar el NPM), `04-cifrar-claves.sh` (las
  MISMAS claves de los 9 usuarios pasadas a BCrypt en el server con `htpasswd` en
  `httpd:2.4-alpine` — gotcha: sin `-i` en `docker run` dentro del bucle, o se come el
  stdin del SELECT), `verificar.sql`.
- **Resultado**: común V53 / cajas V9, 2,223 artículos, 994 clientes (412 gestionados),
  53,266 facturas, `existencia_articulo_bodega` 2,193 filas; API `Started 6.3 s`, 0 errores;
  login por dominio OK; ABC 1–19 sep = L 81,084.93. CAI/RTN se dejaron como venían (sin
  rango, RTN "EN TRAMITE"). Vigilante relabelado a "prod". Kiosco `caja1-lafe` recargado.
- **Corte total del sitio**: ~3 min. Ensayo previo aislado (`~/lafe/ensayo`, borrado).
- Pendiente para el cliente: mínimos de stock (todos en 20 por defecto → alerta ruidosa),
  CAI cuando lo tengan, apagar el Swing viejo, ticketera de `caja1-lafe` (fase 3) y fase 4.
