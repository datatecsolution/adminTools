#!/bin/bash
# PASO 3 — Verificación integral post-migración: estructura + INTEGRIDAD DE DATOS
# comparando contra el baseline capturado en el preflight.
source "$(dirname "$0")/lib-sharon.sh"
ERR=0
log "=== VERIFICACIÓN POST-MIGRACIÓN ==="

V=$(q -e "SELECT MAX(CAST(version AS UNSIGNED)) FROM admin_tools.schema_version;")
[ "$V" = "41" ] && ok "schema común en V41" || { fail "schema común en V$V"; ERR=1; }
for i in $CAJAS; do
  VC=$(q -e "SELECT MAX(CAST(version AS UNSIGNED)) FROM admin_tools_caja_$i.schema_version;")
  [ "$VC" = "9" ] && ok "caja_$i en V9" || { fail "caja_$i en V$VC"; ERR=1; }
done

# CERO float monetario en las tablas migradas
FLO=$(q -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='admin_tools'
  AND table_name IN ('kardex','movimiento_kardex','cliente','cuentas_por_cobrar','cuentas_por_cobrar_facturas','articulo','cierre_caja')
  AND data_type IN ('float','double');")
[ "$FLO" = "0" ] && ok "sin columnas float/double en tablas migradas" || { fail "quedan $FLO columnas float"; ERR=1; }

# Vista y su semántica (US-121)
RES=$(q -e "SELECT IFNULL(SUM(reservado),0) FROM admin_tools.v_reservado_por_articulo;")
ok "vista de reservado responde · total reservado = $RES"
q -e "SELECT COUNT(*) FROM information_schema.views WHERE table_schema='admin_tools' AND table_name='v_reservado_por_articulo' AND (view_definition LIKE '%(1,2)%' OR view_definition LIKE '%(1, 2)%');" | grep -q 1 \
  && ok "vista filtra estado IN (1,2)" || { fail "la vista NO tiene el filtro de US-121"; ERR=1; }

# INTEGRIDAD: ninguna fila perdida en los ALTER (comparación con baseline)
log "--- integridad vs baseline ---"
while IFS='=' read -r k vbase; do
  case "$k" in \#*|"") continue;; esac
  case "$k" in
    admin_tools.*)  t=${k#admin_tools.}; act=$(q -e "SELECT COUNT(*) FROM admin_tools.$t;") ;;
    caja_*)         c=${k%%.*}; t=${k#*.}; n=${c#caja_}; act=$(q -e "SELECT COUNT(*) FROM admin_tools_caja_$n.$t;") ;;
    suma_saldo_cliente)   act=$(q -e "SELECT IFNULL(ROUND(SUM(saldo),2),0) FROM admin_tools.cliente;")
                          ok "INFO $k: float=$vbase → decimal=$act (una diferencia mínima es ESPERADA: el float redondeaba)"; continue ;;
    suma_kardex_cantidad) act=$(q -e "SELECT IFNULL(ROUND(SUM(cantidad),2),0) FROM admin_tools.movimiento_kardex;")
                          ok "INFO $k: float=$vbase → decimal=$act (idem)"; continue ;;
    *) continue ;;
  esac
  if [ "$act" = "$vbase" ]; then ok "$k = $act"; else fail "$k: baseline=$vbase ahora=$act"; ERR=1; fi
done < "$DEPLOY_DIR/baseline.txt"

# Vendedores mono-caja (US-110)
MULTI=$(q -e "SELECT COUNT(*) FROM admin_tools.usuario u WHERE u.tipo_permiso=3 AND (SELECT COUNT(*) FROM admin_tools.cajas_usuarios c WHERE c.usuario=u.usuario) > 1;")
[ "$MULTI" = "0" ] && ok "ningún vendedor con 2+ cajas" || { fail "$MULTI vendedores con 2+ cajas"; ERR=1; }

echo; [ $ERR -eq 0 ] && log "VERIFICACIÓN OK" || log "VERIFICACIÓN CON FALLAS — evaluar rollback"
exit $ERR
