#!/bin/bash
# PASO 0 — Preflight: verifica que TODO esté listo y captura el estado baseline.
# No modifica nada. Si algo falla aquí, la ventana NO arranca.
source "$(dirname "$0")/lib-sharon.sh"
ERR=0
log "=== PREFLIGHT ventana Sharon ==="

# 1) Staging completo
for f in migraciones/V33*.sql migraciones/V34*.sql migraciones/V35*.sql migraciones/V36*.sql \
         migraciones/V37*.sql migraciones/V38*.sql migraciones/V39*.sql migraciones/V40*.sql \
         migraciones/V41*.sql migraciones/V9__*.sql AdminTools-1.0_sharon_8fd9dc5.jar \
         docker-compose.sharon.yml .env-sharon-nuevo; do
  [ -e "$DEPLOY_DIR/$f" ] && ok "staging: $f" || { fail "FALTA $f"; ERR=1; }
done

# 2) Imágenes docker (nueva y de rollback)
for img in admintools-api:sharon-v41 admintools-api:sharon-rollback-39b795d; do
  docker image inspect "$img" >/dev/null 2>&1 && ok "imagen $img" || { fail "FALTA imagen $img"; ERR=1; }
done

# 3) .env nuevo tiene lo que la API nueva exige (hallazgo E4)
for v in CORS_ALLOWED_ORIGINS APP_PUBLIC_INVOICE_SECRET APP_JWT_SECRET_NUEVO_ROTAR MYSQL_HOST MYSQL_USER; do
  grep -q "^$v=" "$DEPLOY_DIR/.env-sharon-nuevo" && ok ".env tiene $v" || { fail ".env SIN $v"; ERR=1; }
done

# 4) Recursos del server (E5: RAM justa; ALTERs necesitan disco temporal)
DISCO=$(df -BG --output=avail / | tail -1 | tr -dc '0-9')
[ "$DISCO" -ge 20 ] && ok "disco libre: ${DISCO}G" || { fail "disco insuficiente: ${DISCO}G"; ERR=1; }
MEM=$(free -m | awk '/^Mem:/{print $7}')
[ "$MEM" -ge 1000 ] && ok "memoria disponible: ${MEM}M" || fail "AVISO memoria baja: ${MEM}M (no correr otros contenedores)"
docker ps --format '{{.Names}}' | grep -q '^ensayo-sharon-mysql$' && fail "AVISO: el MySQL del ensayo está CORRIENDO — pararlo (compite por RAM)" || ok "ensayo MySQL apagado"

# 5) Estado de partida esperado
V=$(q -e "SELECT MAX(CAST(version AS UNSIGNED)) FROM admin_tools.schema_version;")
[ "$V" = "32" ] && ok "schema común en V$V (esperado 32)" || { fail "schema común en V$V — REVISAR antes de seguir"; ERR=1; }
for i in $CAJAS; do
  VC=$(q -e "SELECT MAX(CAST(version AS UNSIGNED)) FROM admin_tools_caja_$i.schema_version;")
  [ "$VC" = "8" ] && ok "caja_$i en V$VC" || { fail "caja_$i en V$VC (esperado 8)"; ERR=1; }
done
docker ps --format '{{.Names}}' | grep -q '^admin-tools-api-v2$' && ok "API v2 corriendo (se parará en el paso 2)" || fail "AVISO: API v2 no está corriendo"

# 6) BASELINE de datos — para comparar después de las migraciones
log "--- capturando baseline de filas ---"
{
  echo "# baseline $(date +%F_%H:%M:%S)"
  for t in articulo movimiento_kardex detalle_movimiento_kardex cliente cuentas_por_cobrar \
           cuentas_por_cobrar_facturas precios_articulos encabezado_factura_temp detalle_factura_temp cierre_caja; do
    echo "admin_tools.$t=$(q -e "SELECT COUNT(*) FROM admin_tools.$t;")"
  done
  for i in $CAJAS; do
    echo "caja_$i.encabezado_factura=$(q -e "SELECT COUNT(*) FROM admin_tools_caja_$i.encabezado_factura;")"
    echo "caja_$i.detalle_factura=$(q -e "SELECT COUNT(*) FROM admin_tools_caja_$i.detalle_factura;")"
  done
  echo "suma_saldo_cliente=$(q -e "SELECT IFNULL(ROUND(SUM(saldo),2),0) FROM admin_tools.cliente;")"
  echo "suma_kardex_cantidad=$(q -e "SELECT IFNULL(ROUND(SUM(cantidad),2),0) FROM admin_tools.movimiento_kardex;")"
} > "$DEPLOY_DIR/baseline.txt"
cat "$DEPLOY_DIR/baseline.txt" | tee -a "$LOG"

echo
[ $ERR -eq 0 ] && log "PREFLIGHT OK — se puede arrancar la ventana" || log "PREFLIGHT CON FALLAS — NO arrancar"
exit $ERR
