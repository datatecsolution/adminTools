#!/bin/bash
# FASE 4 · Bloque E — Contrasena de GRUB.
#
# Sin esto, cualquiera con un teclado pulsa 'e' en el menu de GRUB y arranca
# con  overlayroot=disabled  o  systemd.unit=rescue.target : disco escribible
# y root sin contrasena. Es la ultima puerta abierta de la caja.
#
# Como queda:
#   - superusuario "admin" con hash PBKDF2 (nunca la contrasena en claro)
#   - las entradas normales llevan --unrestricted: la caja ARRANCA SOLA sin
#     pedir nada (Debian 13 no lo pone por defecto; sin eso pediria la
#     contrasena en cada encendido)
#   - editar una entrada ('e') o la consola de GRUB ('c') piden usuario+clave
#
# El hash NO se pasa por argumento ni por el chat: se genera en la propia caja
# y se deja en /root/grub-pass.hash (solo root). Desde el equipo de soporte:
#
#   ssh -t caja2-samuel 'grub-mkpasswd-pbkdf2 | grep -o "grub.pbkdf2.*" \
#       | sudo tee /root/grub-pass.hash >/dev/null && echo HASH GUARDADO'
#
# Se ejecuta ANTES de overlayroot (toca /etc/grub.d y /boot).
#
# Uso:  sudo bash /home/adminpos/pos-terminal/bin/fase4-grub.sh
#       sudo bash .../fase4-grub.sh --quitar     (deja GRUB sin contrasena)
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "Este script necesita root:  sudo bash $0" >&2
  exit 1
fi

HASH=/root/grub-pass.hash
PW=/etc/grub.d/01_pos_password
LINUX=/etc/grub.d/10_linux

if [ "${1:-}" = "--quitar" ]; then
  echo "== Quitando la contrasena de GRUB"
  rm -f "$PW"
  sed -i 's/^CLASS="--class gnu-linux --class gnu --class os --unrestricted"/CLASS="--class gnu-linux --class gnu --class os"/' "$LINUX"
  update-grub 2>&1 | sed 's/^/   /'
  grep -q password_pbkdf2 /boot/grub/grub.cfg && echo "   !! grub.cfg sigue con password" >&2 || echo "   grub.cfg sin contrasena"
  exit 0
fi

echo "== 1/4 · Hash de la contrasena"
if [ ! -s "$HASH" ] || ! grep -q '^grub\.pbkdf2\.sha512\.' "$HASH"; then
  echo "   !! No hay hash en $HASH. Generalo primero (ver cabecera del script)." >&2
  exit 1
fi
chmod 600 "$HASH"
echo "   $HASH presente ($(wc -c < "$HASH") bytes)"

echo "== 2/4 · Entradas normales con --unrestricted (arrancan sin contrasena)"
if grep -q '^CLASS="--class gnu-linux --class gnu --class os --unrestricted"' "$LINUX"; then
  echo "   ya estaba"
elif grep -q '^CLASS="--class gnu-linux --class gnu --class os"' "$LINUX"; then
  cp -n "$LINUX" "$LINUX.bak-antes-unrestricted"
  sed -i 's/^CLASS="--class gnu-linux --class gnu --class os"/CLASS="--class gnu-linux --class gnu --class os --unrestricted"/' "$LINUX"
  echo "   anadido (backup en $LINUX.bak-antes-unrestricted)"
else
  echo "   !! No encuentro la linea CLASS= esperada en $LINUX — revisar a mano" >&2
  exit 1
fi

echo "== 3/4 · Escribiendo $PW"
cat > "$PW" <<GEN
#!/bin/sh
# Fase 4 — contrasena de GRUB. Solo se pide para EDITAR entradas o abrir la
# consola de GRUB; las entradas normales llevan --unrestricted (10_linux).
# Para cambiarla: regenerar /root/grub-pass.hash y repetir fase4-grub.sh.
set -e
cat <<EOT
set superusers="admin"
password_pbkdf2 admin $(tr -d '[:space:]' < "$HASH")
EOT
GEN
chmod 755 "$PW"
sed -n '6,8p' "$PW" | sed 's/^/   /' | cut -c1-70

echo "== 4/4 · update-grub y comprobacion"
update-grub 2>&1 | sed 's/^/   /'
CFG=/boot/grub/grub.cfg
grep -q '^set superusers="admin"' "$CFG" && grep -q '^password_pbkdf2 admin grub.pbkdf2' "$CFG" \
  && echo "   grub.cfg: superusers + password_pbkdf2   OK" \
  || { echo "   !! grub.cfg no tiene la contrasena — NO reiniciar sin revisar" >&2; exit 1; }
n=$(grep -c "^menuentry .*--unrestricted" "$CFG" || true)
m=$(grep -c "^menuentry " "$CFG" || true)
echo "   entradas --unrestricted: $n de $m (las de 'recovery' van dentro del submenu y tambien lo llevan)"
grep -c "^\s*menuentry .*--unrestricted" "$CFG" | sed 's/^/   total con --unrestricted (incl. submenu): /'
[ "$n" -ge 1 ] || { echo "   !! ninguna entrada principal es --unrestricted: la caja PEDIRIA contrasena al arrancar" >&2; exit 1; }

cat <<'TXT'

   Listo. En el proximo arranque:
     - la entrada por defecto arranca sola a los 5 s, como siempre
     - 'e' o 'c' en el menu piden usuario  admin  + la contrasena
   Siguiente:  sudo bash ~/pos-terminal/bin/fase4-cierre-instalacion.sh --red
TXT
