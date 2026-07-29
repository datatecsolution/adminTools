# Análisis de deploy — Swing en Distribuidora Sharon (Ronal)

**Fecha:** 2026-07-29 · **Verificado en vivo** contra el servidor (10.10.0.1) y su MySQL de producción.

## Resumen ejecutivo

Actualizar el Swing en Sharon es viable y trae los fixes que ese cliente motivó (sobreventa V33, decimal, US-117). El gap de BD es menor de lo temido: **común V32 → V41 (9 migraciones) + caja V9 (×6)**. Pero hay una dependencia que condiciona todo:

> **La app de pedidos (front React) SÍ puede quedarse como está. La API v2 que la sirve, NO.** La API de producción corre `39b795d` (pre-decimal, mayo) con `ddl-auto=validate`: al aplicar las migraciones decimal (V34-V37) es muy probable que deje de validar y el contenedor entre en crash-loop → `pedidos.distribuidorasharon.com` caído. **La API debe actualizarse a main en la misma ventana.** El contrato hacia el front viejo es retrocompatible (campos aditivos; los 409 del guard solo aparecen si se activa el bloqueo por usuario, que es opt-in) — el front no se toca (restricción vigente: no deploy de la app de pedidos sin orden explícita).

## 1. Estado actual (verificado 2026-07-29)

| Componente | Versión en prod | Observación |
|---|---|---|
| BD común `admin_tools` | **V32** (2026-07-02) | La migración grande V16→V31 corrió el 2026-06-20 |
| BD cajas ×6 | **V8** | Decimal de caja (V7) ya aplicado |
| API `admin-tools-api-v2` | **`39b795d`** (mayo) | Pre-hito1: sin decimal, sin US-074, sin nada de stock reservado |
| App pedidos `at-ordenes-ventas-v2` | Build ~jun/jul | **Queda como está** (decisión + restricción) |
| Swing en terminales | Build viejo (pre-V33) | El objetivo del deploy |
| MySQL user `admin@%` | Grants totales | CREATE ROUTINE/VIEW/TRIGGER OK — las migraciones pueden correr |

## 2. Migraciones pendientes (común V33→V40 + caja V9)

| Ver. | Qué hace | Riesgo/nota |
|---|---|---|
| V33 | SP `crear_venta_kardex_v2` — guard real de sobreventa (**el fix del caso cat. 107 de ESTE cliente**) | Pasivo hasta poner `facturar_sin_inventario=0` por usuario |
| V34-V37 | Decimal (hito 1) en común | **Rompe el validate de la API vieja** → API a main obligatoria |
| V38 | Categorías jerárquicas (`parent_id`) | Bajo |
| V39 | Vista `v_reservado_por_articulo` + índice | Bajo |
| V40 | Reserva `estado NOT IN (3,5)` (superseded) | Transitoria — V41 la corrige |
| V41 | **Reserva SOLO Activa/Modificada (`estado IN (1,2)`)** — US-121 | Semántica definitiva; "Enviado" no reserva |
| caja V9 ×6 | Trigger llama al SP v2 con usuario | Depende de V33 |

Las aplica el propio Flyway del Swing al arrancar (dueño de migraciones) o a mano en la ventana — el usuario de conexión del Swing en Sharon debe confirmarse en el ensayo (si no es `admin`, se aplican a mano como en dulce).

## 3. Compatibilidad con la app de pedidos (la pregunta central)

- **Front (queda igual)**: consume `/products/description`, `/orders/save|today|delete`. La API main mantiene esos contratos; los campos nuevos son aditivos. Si algún día se activa el bloqueo de stock para un vendedor, el front viejo muestra error genérico en vez del detalle bonito — degradación aceptable, no rotura.
- **API (debe ir a main)**: además de sobrevivir el decimal, trae US-074/109 (guard de pedidos + caja real), US-112, US-116 y US-118. Requiere `.env`: rotar `APP_JWT_SECRET` (pendiente de hito 1 — invalida sesiones activas, avisar) y agregar `APP_PUBLIC_INVOICE_SECRET`.
- **US-109 y los vendedores**: los 11 vendedores tienen cajas asignadas ✅ (resolución funciona). **Pero 2 violan US-110**: `RONAL` (cajas 2,4,3) y `MELVINC` (3,2) — normalizarlos a UNA caja antes o durante la ventana.

## 4. Impactos operativos que requieren DECISIÓN con el cliente

### 4.1 Los 401 pedidos vivos (el hallazgo grande)

Hoy en `encabezado_factura_temp` con estado vivo:
- **292 en estado 4 "Enviado"** — el más viejo de **diciembre 2024**: basura histórica nunca cerrada.
- 41 en estado 2 y 68 en estado 1 (julio, actividad real).
- **295 tienen más de 7 días.**

Interacción con lo nuevo (**actualizado 2026-07-29 con US-121/V41**: solo Activa/Modificada reservan):
- Los **292 "Enviado" ya NO reservan ni los toca el job** — dejan de ser un riesgo para el disponible (quedan como basura histórica inofensiva; limpiarlos es opcional/higiene).
- **US-118**: el job solo anula Activa/Modificada con >7 días — de los 109 pedidos 1/2 actuales (jul-2026), anularía los que superen la semana la primera noche.

**Recomendación**: avisar al cliente de la expiración a 7 días (o arrancar con `expiration-days=0` y activarla después); la limpieza en bloque de los Enviado históricos queda como higiene opcional, sin urgencia.

### 4.2 Sharon es MULTI-BODEGA real

Cajas → bodegas: 1,2,4,5 → bodega 1 · **caja 3 (DETALLE) → bodega 2** · **caja 6 (MAYOREO) → bodega 4**. Consecuencias:
- Con US-109, los vendedores de caja 3 (`LUIS21AVILA`, `OSCARCHINO`, y según default `MELVINC`) reservarán en **bodega 2** — correcto para el guard, PERO el `stock` que la app les muestra viene de `articulo_view` **clavada en bodega 1** (limitación documentada y diferida). El número visible no será el de su bodega.
- **Decisión**: (a) aceptar y documentar (el guard valida bien; solo el número mostrado difiere), (b) reasignar vendedores a cajas de bodega 1, o (c) adelantar el fix de `articulo_view` por bodega del usuario. Recomiendo (a) o (b) para esta ventana.

### 4.3 Guard de sobreventa (V33/US-117)

Pasivo por defecto (todos los usuarios en `facturar_sin_inventario=1`). Activarlo por usuario es decisión del cliente, gradual, DESPUÉS de la limpieza de pedidos — si no, los 292 Enviados bloquearían ventas legítimas.

## 5. Qué gana Sharon con el jar nuevo

Todo lo acumulado desde su build: fix sobreventa V33 + validación contra disponible US-117 (su propio caso de stock fantasma cat. 107), decimal completo, total en letras, hotfix contar efectivo, US-098 toma física, US-108 docs de caja, columna Disponible (US-120) y consulta de existencias con reservado.

## 6. Plan de ventana propuesto

**Pre-ventana (sin tocar prod):**
1. Backup fresco de las 7 BDs de Sharon → **ensayo completo en local/stack**: aplicar V33-V41+V9, levantar API main con `validate` (la prueba que decide si la API vieja realmente rompe — si sorprendentemente valida, se reevalúa), smoke Swing nuevo + app pedidos vieja contra el ensayo.
2. Acordar con el cliente: limpieza de pedidos (§4.1), política de expiración, vendedores multi-caja (§3), multi-bodega (§4.2).
3. Preparar `.env` nuevo de la API (secrets) y el jar firmado del Swing.

**Ventana (estimo 60-90 min, fuera de horario):**
1. Backup completo + stop API v2.
2. Limpieza acordada de pedidos históricos.
3. Aplicar migraciones común V33-V41 + caja V9 ×6 (Swing Flyway o manual como `admin`).
4. Normalizar vendedores RONAL/MELVINC a una caja.
5. Levantar API main (validate contra V41) + verificar `pedidos.distribuidorasharon.com` (login, listar, guardar pedido con la app VIEJA).
6. Actualizar jar del Swing en las terminales; arrancar una, verificar Flyway alineado y venta E2E.
7. Smoke: venta en 2 cajas de bodegas distintas, pedido desde la app, disponible en consulta de existencias.

**Rollback**: restore del backup + reiniciar la API v2 vieja (imagen intacta) + jar anterior. Los triggers/SPs son recreables desde el backup.

## 7. Checklist de decisiones antes de agendar

1. (Opcional, sin urgencia tras US-121) ¿Limpieza de higiene de los 292 "Enviado" históricos?
2. ¿Expiración a 7 días activa desde el día uno, o apagada al inicio?
3. ¿RONAL y MELVINC con cuál caja única?
4. Multi-bodega: ¿aceptar el número de bodega 1 en la app, o reasignar vendedores?
5. ¿A quién y cuándo se activa el bloqueo de sobreventa (`facturar_sin_inventario=0`)?
6. Fecha/hora de la ventana + quién actualiza los jars en las terminales.

## 8. Resultados del ensayo (2026-07-29, en curso)

Ensayo ejecutado en el propio servidor con un MySQL 8.0 descartable (`ensayo-sharon-mysql`, puerto local 3310) y dump fresco sanitizado de las 7 BDs (348 MB).

**Hallazgo E1 — restore necesita `log_bin_trust_function_creators=1`.** La carga del dump aborta con `ERROR 1418` al recrear las funciones (MySQL 8 + binlog activo). Impacto directo en el plan de ROLLBACK de la ventana: antes de un restore hay que `SET GLOBAL log_bin_trust_function_creators=1` (o restaurar con un usuario con privilegios de SUPER/SET_USER_ID efectivos). Se agrega al runbook de la ventana.

*(Los resultados de migraciones V33-V41, validate de la API vieja y de la nueva se documentan al completar el ensayo.)*

---
*Verificación en vivo 2026-07-29: schema_version común y ×6 cajas, contenedores, grants de `admin@%`, vendedores/cajas_usuarios, distribución de bodegas y conteo de pedidos vivos. Restricción vigente: la app de pedidos NO se despliega (solo se valida que siga funcionando).*
