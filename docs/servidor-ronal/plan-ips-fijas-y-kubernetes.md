# Plan — IPs fijas fuera del rango dinámico en el server de Ronal (+ evaluación de Kubernetes)

**Fecha**: 2026-09-20 · **Disparador**: incidente de hoy 09:58–10:38 (reinicio del server → `catalogo-dulce-morena`
tomó la `172.25.0.18` de `api-lafe` → `api-lafe` no arrancó → nginx del proxy no validó → **los 16 dominios en 521
durante 40 min**). Segundo incidente de la misma familia (2026-09-16, `pos-samuel`).

## 1. Por qué pasa

- Red compartida del proxy `n8n-docker_default` = `172.25.0.0/16`, 21 contenedores. Docker reparte las IPs
  dinámicas **de la más baja libre hacia arriba**: hoy ocupan exactamente `.2` … `.22`, sin huecos.
- Las 4 APIs de clientes tienen IP **fija** (`ipv4_address`) para que el proxy las encuentre: dulce `.22`,
  samuel `.21`, lafe `.18`, mariposas `.14`. **Están en medio del rango dinámico**: en cada reinicio, si un
  contenedor sin IP fija arranca antes y le toca ese número, la API con IP fija muere con
  `Address already in use`.
- El proxy (nginx-proxy-manager) genera para `/admin_tools/api` un `proxy_pass http://api-x:8080;` **literal**:
  nginx exige resolverlo al arrancar; si el contenedor no existe, **falla toda la configuración** y no levanta
  ningún sitio (no solo el del cliente afectado). Para `/` usa variables (`set $server`), por eso el POS
  estático nunca cae.
- Agravante: el server **se ha reiniciado 8 veces desde el 16 de septiembre**, todas sin apagado limpio
  (el journal termina de golpe: 09:54:46 → arranque 09:58:50). Eso es corte de energía o cuelgue de
  hardware, no software; mientras siga, la lotería de IPs se juega cada pocos días.

## 2. Objetivo

Que un reinicio del server deje **todo arriba solo**, sin intervención, y que la caída de un contenedor
**no tumbe el proxy de los demás clientes**.

## 3. Plan de ejecución (con clientes en producción)

### Paso 0 — preparación (sin corte)
- Respaldo de los 4 `docker-compose.yml` (`.bak-ips-<fecha>`) y export de la config del NPM
  (`/data` del contenedor, `tar` a `~/backups-npm/`).
- Elegir las IPs nuevas, **fuera del alcance dinámico**: `api-samuel .201`, `api-lafe .202`,
  `api-mariposas .203`, `api-pruebas-dulce .204` (con 21 contenedores el reparto dinámico no pasa de `~.30`;
  aunque se dupliquen los contenedores, jamás llega a `.200`).
- Editar los 4 compose cambiando solo `ipv4_address` (no se toca imagen ni nada más).
- Verificar que el vigilante (`~/bin/docker-watch.sh`) sigue programado.

### Paso 1 — ventana de corte (≈ 20–25 s en total, un solo corte)
Hacerlo en horario de menor uso (p. ej. después del cierre de Samuel, ~20:30, o antes de las 07:00).
Secuencia en un único script para que el corte sea el mínimo:
1. `docker compose up -d api-<cliente>` en los **4 stacks a la vez** (en paralelo): cada API tarda ~9 s en
   volver a estar lista. Durante esos ~9 s el POS de cada cliente muestra error en las llamadas a la API
   (la pantalla no se cae; una venta en curso se reintenta al confirmar).
2. Esperar `Started AdmintoolsApplication` en las 4.
3. `docker restart nginx-proxy-manager` (≈ 3–5 s en que ningún dominio responde) — necesario porque el
   proxy tiene cacheadas las IPs viejas en las custom locations.
4. `nginx -t` dentro del NPM + curl a los 16 dominios.

Corte percibido por un cajero: la API de su cliente ~9 s + el proxy ~4 s. Sin migraciones, sin tocar BD.

### Paso 2 — que el server se cure solo tras un reinicio (sin corte)
Añadir al vigilante (`docker-watch.sh`, ya corre por cron) o a una unidad `systemd` de arranque:
- Si hay contenedores `Exited` de la lista de clientes → `docker compose up -d` de ese stack.
- Si `docker exec nginx-proxy-manager nginx -t` falla → tras levantar lo caído, `docker restart nginx-proxy-manager`.
- Correo de aviso (canal actual: `mail root` → alias → yahoo) diciendo qué reparó.
Con esto, aunque volviera a haber una colisión, el proxy se recupera en < 2 min sin que nadie entre.

### Paso 3 — rollback
Volver a las IPs anteriores es el mismo procedimiento al revés (compose `.bak` + `up -d` + restart del
proxy). El riesgo real es bajo: el cambio son 4 números; nginx re-resuelve al reiniciar.

### Paso 4 — el problema de fondo: los reinicios del server
Ocho arranques sin apagado limpio en 5 días. Revisar con el dueño del server: UPS (¿existe?, ¿batería?),
`journalctl -b -1 -p err` y `dmesg` del último arranque, temperatura/RAID del T140 (ya se sabe que es RAID
sin caché). Ningún cambio de software compensa un servidor que pierde la energía.

## 4. ¿Kubernetes?

Lo que tenemos: **un solo servidor** (T140, 6 núcleos, 62 GB RAM con 6 GB usados, disco al 14 %),
26 contenedores en ~10 stacks de Docker Compose, un proxy compartido (NPM) con TLS por Cloudflare.

| | Docker Compose (hoy + este plan) | Kubernetes (k3s, un nodo) |
|---|---|---|
| Descubrimiento de servicios | DNS de Docker + IPs fijas como parche | Nativo y estable (Service/ClusterIP), sin IPs fijas |
| Proxy | NPM con custom locations literales (el punto débil) | Ingress (ingress-nginx/Traefik) que re-resuelve solo; cert-manager |
| Recuperación tras reinicio | `restart: unless-stopped` + el vigilante del paso 2 | Reconciliación automática (el nodo vuelve y recrea todo) |
| Alta disponibilidad | No (un server) | **Tampoco** con un nodo: si el server se apaga, se apaga todo igual |
| Migración | — | Reescribir 10 stacks a manifests/Helm, volúmenes de MySQL (StatefulSets), secretos, red del NPM; 2–3 semanas y un corte por cliente |
| Operación | `docker compose`, que el usuario ya domina | kubectl, namespaces, PVC, ingress, RBAC: curva de aprendizaje real |
| Lo que resuelve del incidente de hoy | Sí, con los pasos 1–2 (un día de trabajo) | Sí, pero el problema raíz (el server se reinicia) sigue igual |

**Recomendación**: **no** migrar a Kubernetes ahora. La complejidad que añade no compra disponibilidad con un
solo nodo, y el incidente de hoy se resuelve con los pasos 1 y 2. Kubernetes tendría sentido cuando se dé
alguna de estas condiciones: (a) un **segundo servidor** (ahí sí hay HA real y vale la pena k3s con 2–3
nodos); (b) más de ~15 clientes con el mismo stack, donde plantillar despliegues (Helm) ahorre trabajo;
(c) necesidad de escalar la API por cliente. Mientras tanto, dos mejoras baratas en la misma línea, si se
quiere ir preparando el terreno: (1) un `docker-compose` por cliente **generado desde una plantilla** (hoy
cada stack se escribió a mano y por eso mariposas quedó sin IP fija hasta anoche); (2) reemplazar las
custom locations literales del NPM por un proxy que re-resuelva (Traefik con labels de Docker, o
`proxy_pass` con variables + `resolver 127.0.0.11`) — eso elimina de raíz la dependencia de IPs fijas.

## 5. Decisión que se pide

1. ¿Ejecuto el paso 1 esta noche después del cierre de Samuel (o mañana antes de las 07:00)?
2. ¿Autorizás el paso 2 (auto-reparación en el arranque)?
3. Los reinicios del server: ¿quién puede revisar UPS/energía del T140?
