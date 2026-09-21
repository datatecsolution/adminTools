#!/bin/bash
# =====================================================================
# docker-repair.sh — auto-reparacion de los stacks de clientes tras un
# reinicio del server (incidente 2026-09-20: api-lafe no levanto por una IP
# tomada y el proxy no arranco → 16 dominios caidos 40 min hasta que alguien
# entro). Lo llama docker-watch.sh en cada corrida (cada 2 min).
#
# REGLAS (acordadas con el usuario 2026-09-20):
#   1. Contenedor de cliente que NO esta running → `docker compose up -d`
#      de SU servicio, como maximo 3 intentos con espera creciente
#      (0, 4 y 8 min). Al 3er fallo se rinde: correo con el motivo concreto
#      (IP tomada y quien la tiene, disco lleno, dependencia caida, log) y
#      no vuelve a tocarlo hasta que se vea running de nuevo (o se borre
#      ~/.docker-watch/rep_<contenedor>).
#   2. El proxy SOLO se reinicia si NO esta sirviendo (no responde en 443 en
#      el propio server y no hay workers de nginx), y como maximo una vez
#      cada 30 min. NUNCA se reinicia un proxy que sirve, aunque `nginx -t`
#      falle (eso solo tumba al cliente afectado; reiniciarlo tumbaria a
#      todos, como esta manana).
#   3. Pausa para deploys: si existe ~/.docker-watch/pausa no hace nada.
#      flock evita corridas solapadas.
#   4. Nunca desconecta/mueve otros contenedores, ni toca bases de datos,
#      ni reinicia nada que este funcionando.
# Uso: docker-repair.sh            (lo llama el vigilante)
#      docker-repair.sh --status   (que haria, sin hacerlo)
# Variables de prueba: REP_BACKOFF=0 (sin esperas), REP_SIN_CORREO=1.
# =====================================================================
set -u
STATE=$HOME/.docker-watch; mkdir -p "$STATE"
HOST=$(hostname); DEST=root
BACKOFF=(0 240 480)                 # segundos antes del intento 1, 2 y 3
[ "${REP_BACKOFF:-}" = 0 ] && BACKOFF=(0 0 0)
PROXY=nginx-proxy-manager
PROXY_MIN=1800                      # 30 min entre reinicios del proxy
SOLO_VER=0; [ "${1:-}" = --status ] && { SOLO_VER=1; REP_SIN_CORREO=1; }

# contenedor : carpeta del stack : servicio del compose
STACKS=(
  "mysql-samuel:samuel:mysql-samuel" "api-samuel:samuel:api-samuel" "pos-samuel:samuel:pos-samuel" "pedidos-samuel:samuel:pedidos-samuel"
  "mysql-lafe:lafe:mysql-lafe" "api-lafe:lafe:api-lafe" "pos-lafe:lafe:pos-lafe"
  "mysql-mariposas:mariposas:mysql-mariposas" "api-mariposas:mariposas:api-mariposas" "pos-mariposas:mariposas:pos-mariposas" "pedidos-mariposas:mariposas:pedidos-mariposas"
  "mysql-pruebas-dulce:pruebas-dulce:mysql-pruebas" "api-pruebas-dulce:pruebas-dulce:api-pruebas" "pos-pruebas-dulce:pruebas-dulce:pos-pruebas"
)

log() { echo "$(date "+%F %T") repair: $*"; }
correo() { # asunto cuerpo
  log "$1"
  [ "${REP_SIN_CORREO:-}" = 1 ] && return 0
  printf "%s\n\n-- %s · %s\n" "$2" "$HOST" "$(date "+%F %T")" | mail -s "$1" "$DEST"
}

[ -f "$STATE/pausa" ] && { log "pausa activa ($STATE/pausa): no se repara nada"; exit 0; }
exec 9>"$STATE/repair.lock"; flock -n 9 || { log "otra corrida en curso"; exit 0; }

diagnostico() { # contenedor stack
  local c=$1 s=$2 out=""
  local err; err=$(docker inspect -f "{{.State.Error}}" "$c" 2>/dev/null)
  [ -n "$err" ] && out+="error de docker: $err"$n
  if echo "$err" | grep -qi "address already in use"; then
    local ip; ip=$(grep -m1 "ipv4_address:" "$HOME/$s/stack/docker-compose.yml" | tr -d " " | cut -d: -f2)
    local quien; quien=$(docker network inspect n8n-docker_default -f "{{range .Containers}}{{.Name}}={{.IPv4Address}} {{end}}" | tr " " "\n" | grep "=$ip/" | cut -d= -f1)
    out+="la IP fija $ip la tiene: ${quien:-nadie (?)} — NO la libero automaticamente; hay que mover ese contenedor a mano"$n
  fi
  local libre; libre=$(df -h / | awk "NR==2{print \$4}"); out+="disco libre en /: $libre"$n
  out+="ultimas lineas del log:"$n"$(docker logs --tail 12 "$c" 2>&1 | cut -c1-160)"
  printf "%s" "$out"
}

# ---------------------------------------------------------------- contenedores
for item in "${STACKS[@]}"; do
  IFS=: read -r c s svc <<< "$item"
  docker inspect "$c" >/dev/null 2>&1 || continue     # no existe en este server
  estado=$(docker inspect -f "{{.State.Status}}" "$c")
  f="$STATE/rep_$c"
  if [ "$estado" = running ]; then
    if [ -f "$f" ]; then
      n=0; read -r n _ < "$f" 2>/dev/null || true
      rm -f "$f"; [ "${n:-0}" -ge 3 ] && correo "[$HOST] $c volvio a estar arriba" "El contenedor $c esta running otra vez; se limpia el estado de rendicion."
    fi
    continue
  fi
  n=0; ultimo=0; [ -f "$f" ] && read -r n ultimo < "$f"
  if [ "$n" -ge 3 ]; then
    log "$c $estado — rendido (3 intentos); a la espera de una persona"; continue
  fi
  espera=${BACKOFF[$n]}; ahora=$(date +%s)
  if [ $((ahora - ultimo)) -lt "$espera" ]; then
    log "$c $estado — intento $((n+1)) en $((espera - (ahora - ultimo))) s"; continue
  fi
  if [ $SOLO_VER = 1 ]; then log "HARIA: compose up -d $svc en ~/$s/stack ($c esta $estado, intento $((n+1))/3)"; continue; fi
  log "$c esta $estado: intento $((n+1))/3 de compose up -d $svc"
  ( cd "$HOME/$s/stack" && docker compose up -d "$svc" ) >"$STATE/rep_$c.out" 2>&1
  sleep 8
  if [ "$(docker inspect -f "{{.State.Status}}" "$c" 2>/dev/null)" = running ]; then
    rm -f "$f"
    correo "[$HOST] REPARADO: $c levantado solo (intento $((n+1)))" "$c estaba $estado y se levanto con docker compose up -d $svc.$(printf "\n\n"; cat "$STATE/rep_$c.out")"
  else
    n=$((n+1)); echo "$n $ahora" > "$f"
    if [ "$n" -ge 3 ]; then
      correo "[$HOST] ME RINDO con $c: 3 intentos fallidos — hace falta una persona" "$c sigue $(docker inspect -f "{{.State.Status}}" "$c" 2>/dev/null).$(printf "\n\n")$(diagnostico "$c" "$s")$(printf "\n\nsalida del compose:\n")$(cat "$STATE/rep_$c.out")"
    else
      log "$c sigue caido tras el intento $n; proximo en ${BACKOFF[$n]} s"
    fi
  fi
done

# ---------------------------------------------------------------------- proxy
if docker inspect "$PROXY" >/dev/null 2>&1; then
  sirve=$(curl -sk -o /dev/null -m 5 -w "%{http_code}" -H "Host: datatecsolution.com" https://127.0.0.1/ 2>/dev/null || echo 000)
  workers=$(docker top "$PROXY" 2>/dev/null | grep -c "nginx: worker" || echo 0)
  if [ "$sirve" != 000 ] || [ "$workers" -gt 0 ]; then
    # sirve: NUNCA se reinicia. Si la config esta rota se avisa (1 vez/30 min).
    if ! docker exec "$PROXY" nginx -t >/dev/null 2>&1; then
      f="$STATE/proxy_cfg_at"; ult=0; [ -f "$f" ] && read -r ult < "$f"
      if [ $(( $(date +%s) - ult )) -ge $PROXY_MIN ]; then
        [ $SOLO_VER = 1 ] || date +%s > "$f"
        correo "[$HOST] proxy sirviendo pero su config NO valida (un reinicio lo tumbaria)" "$(docker exec "$PROXY" nginx -t 2>&1 | grep -i emerg | head -3)$(printf "\n\nNo se reinicia: seguiria caido para todos. Levantar el contenedor que falta y recargar con: docker exec %s nginx -s reload" "$PROXY")"
      fi
    fi
  else
    f="$STATE/proxy_restart_at"; ult=0; [ -f "$f" ] && read -r ult < "$f"
    if [ $(( $(date +%s) - ult )) -lt $PROXY_MIN ]; then
      log "proxy sin servir; ya se reinicio hace menos de 30 min, no insisto"
    elif [ $SOLO_VER = 1 ]; then
      log "HARIA: docker restart $PROXY (no sirve: http $sirve, workers $workers)"
    else
      date +%s > "$f"
      log "proxy NO sirve (http $sirve, workers $workers): docker restart $PROXY"
      docker restart "$PROXY" >/dev/null 2>&1; sleep 12
      if docker exec "$PROXY" nginx -t >/dev/null 2>&1 && [ "$(docker top "$PROXY" 2>/dev/null | grep -c "nginx: worker")" -gt 0 ]; then
        correo "[$HOST] REPARADO: proxy reiniciado y sirviendo" "El proxy no respondia; tras docker restart valida y tiene workers."
      else
        correo "[$HOST] proxy reiniciado pero SIGUE sin arrancar — hace falta una persona" "$(docker exec "$PROXY" nginx -t 2>&1 | grep -i emerg | head -3)$(printf "\n\nSuele ser un upstream que no existe: levantar ese contenedor (o quitar su proxy host) y reiniciar el proxy.")"
      fi
    fi
  fi
fi
exit 0
