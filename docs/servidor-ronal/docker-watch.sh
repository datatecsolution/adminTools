#!/bin/bash
# =====================================================================
# docker-watch.sh — vigilante de contenedores y dominios del server Ronal.
#
# Corre cada 2 minutos desde el crontab de `ronal` (docker en su grupo,
# sin sudo) y avisa por correo por el MISMO canal que fail2ban:
# `mail -s ... root` → /etc/aliases → correo del usuario (postfix → Gmail).
#
# Qué vigila:
#   1. Contenedores con politica de reinicio (unless-stopped/always) que NO
#      esten "running".
#   2. Los dominios publicados: cada URL debe responder con uno de sus
#      codigos esperados (200 front / 401 endpoint de API sin token). Un 5xx
#      o sin respuesta = caido AUNQUE el contenedor este arriba (el 502 por
#      IP cacheada del proxy, 2026-09-15).
#
# UN SOLO CORREO RESUMEN (pedido del usuario 2026-09-16): en vez de un
# correo por cada cosa, se manda un unico correo con TODO lo caido y todo lo
# que esta arriba, y solo cuando el cuadro cambia:
#   - algo nuevo entra en falla (tras FALLOS_PARA_AVISAR chequeos seguidos)
#   - algo se recupera (incluido "todo recuperado")
#   - recordatorio cada REALERT_MIN minutos mientras siga algo caido
# Estado en ~/.docker-watch/ (contadores por clave + ultimo cuadro avisado).
#
# Uso:  docker-watch.sh            (chequeo normal, lo llama cron)
#       docker-watch.sh --test     (manda un resumen de prueba con el cuadro actual)
#       docker-watch.sh --boot     (aviso de "el servidor se reinicio", cron @reboot)
#       docker-watch.sh --status   (imprime el cuadro sin mandar nada)
# =====================================================================
set -u
STATE=$HOME/.docker-watch
mkdir -p "$STATE"
DEST=root
HOST=$(hostname)
REALERT_MIN=60
# 3 chequeos seguidos (~6 min): un rebuild de imagen pone lento el server y
# los curl de 15 s pueden vencer un par de veces sin que nada este caido.
FALLOS_PARA_AVISAR=3

# URL|codigos-ok (separados por coma)|descripcion
URLS=(
  "https://admintools.supermercadosurbina.com/|200|Samuel POS (PRODUCCION)"
  "https://admintools.supermercadosurbina.com/admin_tools/api/cierre-caja|401|Samuel API (PRODUCCION)"
  "https://pedidos.supermercadosurbina.com/|200|Samuel app de pedidos (PRODUCCION)"
  "https://pos.datatecsolution.com/|200|Sharon POS (PRODUCCION)"
  "https://pedidos.distribuidorasharon.com/|200|Sharon app de pedidos (PRODUCCION)"
  "https://pedidos.distribuidorasharon.com/admin_tools/api/cierre-caja|401|Sharon API (PRODUCCION)"
  "https://posdulce.datatecsolution.com/|200|dulce POS (pruebas)"
  "https://posdulce.datatecsolution.com/admin_tools/api/cierre-caja|401|dulce API (pruebas)"
  "https://mariposasdoradas.datatecsolution.com/|200|Mariposas POS (demo)"
  "https://mariposasdoradas.datatecsolution.com/admin_tools/api/cierre-caja|401|Mariposas API (demo)"
  "https://pedidosmariposas.datatecsolution.com/|200|Mariposas app de pedidos (demo)"
  "https://lafe.datatecsolution.com/|200|La Fe POS (prod)"
  "https://lafe.datatecsolution.com/admin_tools/api/cierre-caja|401|La Fe API (prod)"
)

enviar() { # asunto, cuerpo
  printf '%s\n\n-- %s · %s\n' "$2" "$HOST" "$(date '+%F %T')" | mail -s "$1" "$DEST"
}
clave() { printf '%s' "$1" | tr -c 'A-Za-z0-9' '_'; }

# cuadro de esta corrida
CAIDOS_KEYS=()      # claves confirmadas en falla (>= FALLOS_PARA_AVISAR)
CAIDOS_TXT=()       # texto de cada una
ARRIBA_TXT=()       # lo sano, compacto
PENDIENTES=()       # fallando pero aun sin confirmar

# registra el resultado de un chequeo y clasifica
# marca <clave> <fallo:1|0> <descripcion> <detalle>
marca() {
  local k=$1 fallo=$2 desc=$3 det=$4 f="$STATE/fail_$k" n=0
  if [ "$fallo" = 1 ]; then
    [ -f "$f" ] && read -r n < "$f"
    n=$((n + 1)); echo "$n" > "$f"
    if [ "$n" -ge "$FALLOS_PARA_AVISAR" ]; then
      CAIDOS_KEYS+=("$k"); CAIDOS_TXT+=("✘ $desc — $det")
    else
      PENDIENTES+=("$desc ($n/$FALLOS_PARA_AVISAR)")
    fi
  else
    rm -f "$f"
    ARRIBA_TXT+=("✔ $desc")
  fi
}

chequear() {
  # ---- 1. contenedores ----
  local nombre estado politica
  while read -r nombre estado politica; do
    [ "$politica" = no ] && continue
    local k="c_$(clave "$nombre")"
    if [ "$estado" != running ]; then
      marca "$k" 1 "contenedor $nombre" "$estado · $(docker inspect -f 'salida {{.State.ExitCode}} {{.State.Error}} · desde {{.State.FinishedAt}}' "$nombre" 2>/dev/null | cut -c1-120)"
    else
      marca "$k" 0 "contenedor $nombre" "running"
    fi
  done < <(docker ps -a --format '{{.Names}} {{.State}}' | while read -r n s; do echo "$n $s $(docker inspect -f '{{.HostConfig.RestartPolicy.Name}}' "$n" 2>/dev/null)"; done)
  # contadores de contenedores que ya no existen (borrados/recreados con otro nombre)
  for f in "$STATE"/fail_c_*; do
    [ -e "$f" ] || continue
    local k; k=$(basename "$f" | sed 's/^fail_//'); local existe=0
    while read -r n; do [ "c_$(clave "$n")" = "$k" ] && existe=1; done < <(docker ps -a --format '{{.Names}}')
    [ "$existe" = 1 ] || rm -f "$f"
  done
  # ---- 2. dominios ----
  local item url oks desc code
  for item in "${URLS[@]}"; do
    IFS='|' read -r url oks desc <<< "$item"
    code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 15 "$url" 2>/dev/null || echo 000)
    local k="u_$(clave "$url")"
    if echo ",$oks," | grep -q ",$code,"; then
      marca "$k" 0 "$desc" "HTTP $code"
    else
      marca "$k" 1 "$desc" "HTTP $code (esperado $oks) $url"
    fi
  done
}

cuadro() { # texto completo del resumen
  local out=""
  if [ ${#CAIDOS_TXT[@]} -gt 0 ]; then
    out+="CAIDO (${#CAIDOS_TXT[@]}):"$'\n'; local t; for t in "${CAIDOS_TXT[@]}"; do out+="  $t"$'\n'; done; out+=$'\n'
  else
    out+="CAIDO: nada"$'\n\n'
  fi
  if [ ${#PENDIENTES[@]} -gt 0 ]; then
    out+="EN OBSERVACION (fallo reciente, aun sin confirmar):"$'\n'; for t in "${PENDIENTES[@]}"; do out+="  · $t"$'\n'; done; out+=$'\n'
  fi
  out+="ARRIBA (${#ARRIBA_TXT[@]}):"$'\n'; for t in "${ARRIBA_TXT[@]}"; do out+="  $t"$'\n'; done
  printf '%s' "$out"
}

case "${1:-}" in
  --test)
    chequear
    enviar "[$HOST] prueba del vigilante — resumen actual" "$(cuadro)"
    echo "resumen de prueba enviado a $DEST"; exit 0;;
  --boot)
    sleep 120
    "$HOME/bin/docker-repair.sh" || true
    chequear
    enviar "[$HOST] el servidor se REINICIO ($(uptime -s))" "$(cuadro)"
    # el arranque ya se reporto: el cuadro queda como avisado
    printf '%s\n' "${CAIDOS_KEYS[@]:-}" | sort > "$STATE/alerted"; date +%s > "$STATE/alerted_at"
    exit 0;;
  --status)
    chequear; cuadro; exit 0;;
esac

# US: auto-reparacion (docker-repair.sh) ANTES de chequear: reglas y pruebas en
# adminTools/docs/servidor-ronal/plan-ips-fijas-y-kubernetes.md (2026-09-20).
"$HOME/bin/docker-repair.sh" || true

chequear

# ---- decidir si toca UN correo ----
ahora=$(date +%s)
actual=$(printf '%s\n' "${CAIDOS_KEYS[@]:-}" | sed '/^$/d' | sort)
previo=$(sed '/^$/d' "$STATE/alerted" 2>/dev/null | sort)
alerted_at=0; [ -f "$STATE/alerted_at" ] && read -r alerted_at < "$STATE/alerted_at"

if [ "$actual" != "$previo" ]; then
  # el cuadro cambio: algo nuevo cayo y/o algo se recupero
  nuevos=$(comm -13 <(printf '%s\n' "$previo") <(printf '%s\n' "$actual") | sed '/^$/d' | wc -l | tr -d ' ')
  recuperados=$(comm -23 <(printf '%s\n' "$previo") <(printf '%s\n' "$actual") | sed '/^$/d' | wc -l | tr -d ' ')
  if [ -z "$actual" ]; then
    asunto="[$HOST] TODO RECUPERADO — $recuperados servicio(s) volvieron"
  else
    asunto="[$HOST] ${#CAIDOS_TXT[@]} CAIDO(S) · $nuevos nuevo(s) · $recuperados recuperado(s)"
  fi
  enviar "$asunto" "$(cuadro)"
  printf '%s\n' "$actual" > "$STATE/alerted"; echo "$ahora" > "$STATE/alerted_at"
elif [ -n "$actual" ] && [ $((ahora - alerted_at)) -ge $((REALERT_MIN * 60)) ]; then
  enviar "[$HOST] SIGUE CAIDO: ${#CAIDOS_TXT[@]} servicio(s) (recordatorio cada $REALERT_MIN min)" "$(cuadro)"
  echo "$ahora" > "$STATE/alerted_at"
fi
