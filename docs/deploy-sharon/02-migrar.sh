#!/bin/bash
# PASO 2 — Migraciones UNA A UNA, con tiempo, verificación de post-condición
# y ABORTO inmediato ante el primer fallo (con el error a la vista).
# Ejecutado por Claude en la ventana; nada corre "a ciegas".
source "$(dirname "$0")/lib-sharon.sh"
log "=== MIGRACIONES común V33-V41 + caja V9 ==="

# post-condición por versión: devuelve 1 si quedó aplicada correctamente
check() {
  case "$1" in
    33) q -e "SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema='admin_tools' AND routine_name='crear_venta_kardex_v2';" ;;
    34) q -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='admin_tools' AND table_name='movimiento_kardex' AND column_name='cantidad' AND data_type='decimal';" ;;
    35) q -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='admin_tools' AND table_name='cliente' AND column_name='saldo' AND data_type='decimal';" ;;
    36) q -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='admin_tools' AND table_name='articulo' AND column_name='precio_articulo' AND data_type='decimal' AND is_nullable='NO';" ;;
    37) q -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='admin_tools' AND table_name='cierre_caja' AND column_name='efectivo' AND data_type='decimal';" ;;
    38) q -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='admin_tools' AND table_name='marcas' AND column_name='parent_id';" ;;
    39) q -e "SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics WHERE table_schema='admin_tools' AND table_name='encabezado_factura_temp' AND index_name='idx_eft_estado_caja';" ;;
    40) q -e "SELECT COUNT(*) FROM information_schema.views WHERE table_schema='admin_tools' AND table_name='v_reservado_por_articulo';" ;;
    41) q -e "SELECT COUNT(*) FROM information_schema.views WHERE table_schema='admin_tools' AND table_name='v_reservado_por_articulo' AND (view_definition LIKE '%(1,2)%' OR view_definition LIKE '%(1, 2)%');" ;;
  esac
}

for v in 33 34 35 36 37 38 39 40 41; do
  YA=$(q -e "SELECT COUNT(*) FROM admin_tools.schema_version WHERE CAST(version AS UNSIGNED)=$v;")
  [ "$YA" != "0" ] && { ok "V$v ya registrada — se salta"; continue; }
  F=$(ls "$DEPLOY_DIR"/migraciones/V${v}__*.sql 2>/dev/null | head -1)
  [ -z "$F" ] && { fail "no encuentro el archivo de V$v"; exit 1; }
  log "--- V$v: $(basename "$F")"
  T0=$(date +%s)
  ERRF="$DEPLOY_DIR/backup/err_V$v.txt"
  mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools < "$F" 2>"$ERRF"
  RC=$?
  DUR=$(( $(date +%s) - T0 ))
  ERRTXT=$(grep -v "^mysql: \[Warning\]" "$ERRF" | head -5)
  if [ $RC -ne 0 ] || [ -n "$ERRTXT" ]; then
    fail "V$v FALLÓ (rc=$RC, ${DUR}s)"; echo "$ERRTXT" | tee -a "$LOG"
    log "ABORTADO en V$v — el esquema quedó a medias. Decidir: fix-forward o ROLLBACK (99-rollback.sh)"
    exit 1
  fi
  [ "$(check $v)" = "1" ] && ok "V$v aplicada y verificada (${DUR}s)" || { fail "V$v corrió sin error pero la POST-CONDICIÓN no se cumple"; exit 1; }
  q -e "INSERT INTO admin_tools.schema_version (installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success)
        SELECT MAX(installed_rank)+1,'$v','sharon deploy','SQL','$(basename "$F")',NULL,'sharon-deploy',NOW(),${DUR}000,1 FROM admin_tools.schema_version;"
done

log "--- cajas: V9 ×6 ---"
F9=$(ls "$DEPLOY_DIR"/migraciones/V9__*.sql | head -1)
for i in $CAJAS; do
  YA=$(q -e "SELECT COUNT(*) FROM admin_tools_caja_$i.schema_version WHERE CAST(version AS UNSIGNED)=9;")
  [ "$YA" != "0" ] && { ok "caja_$i V9 ya registrada — se salta"; continue; }
  T0=$(date +%s); ERRF="$DEPLOY_DIR/backup/err_caja${i}_V9.txt"
  mysql -uadmin -p"$PW" -h127.0.0.1 admin_tools_caja_$i < "$F9" 2>"$ERRF"; RC=$?
  DUR=$(( $(date +%s) - T0 ))
  ERRTXT=$(grep -v "^mysql: \[Warning\]" "$ERRF" | head -5)
  if [ $RC -ne 0 ] || [ -n "$ERRTXT" ]; then fail "caja_$i V9 FALLÓ"; echo "$ERRTXT" | tee -a "$LOG"; exit 1; fi
  TRG=$(q -e "SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema='admin_tools_caja_$i' AND trigger_name='detalle_factura_b_insert' AND action_statement LIKE '%crear_venta_kardex_v2%';")
  [ "$TRG" = "1" ] && ok "caja_$i: trigger apunta al SP v2 (${DUR}s)" || { fail "caja_$i: trigger NO llama a crear_venta_kardex_v2"; exit 1; }
  q -e "INSERT INTO admin_tools_caja_$i.schema_version (installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success)
        SELECT MAX(installed_rank)+1,'9','sharon deploy','SQL','$(basename "$F9")',NULL,'sharon-deploy',NOW(),${DUR}000,1 FROM admin_tools_caja_$i.schema_version;"
done
log "TODAS LAS MIGRACIONES OK"
