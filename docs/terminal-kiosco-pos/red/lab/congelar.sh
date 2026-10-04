#!/bin/bash
# Activa overlayroot en la VM de laboratorio (como el bloque C de la fase 4).
# Correr DESPUÉS de guardar una instantánea: si el arranque congelado falla, se vuelve a ella.
#   bash vm.sh ssh 'sudo bash /tmp/plantilla/red/lab/congelar.sh' && bash vm.sh reiniciar
set -euo pipefail
# Lección del laboratorio (2026-10-03): el script de overlayroot hace `mount --move` en el
# initramfs. Sin busybox, el mount mínimo de klibc no lo entiende → «mount: invalid option --»
# → kernel panic: la caja NO ARRANCA. Se verifica ANTES de congelar.
command -v busybox >/dev/null || { echo "falta busybox: la caja no arrancaría con overlayroot"; exit 1; }
sed -i 's/^overlayroot=.*/overlayroot="tmpfs:swap=1,recurse=0"/' /etc/overlayroot.conf
grep '^overlayroot=' /etc/overlayroot.conf
update-initramfs -u -k all >/dev/null
for i in /boot/initrd.img-*; do
  lsinitramfs "$i" | grep -qE '(^|/)bin/busybox$' && echo "busybox en $i" || { echo "busybox NO está en $i: abortado"; sed -i 's/^overlayroot=.*/overlayroot=""/' /etc/overlayroot.conf; update-initramfs -u -k all >/dev/null; exit 1; }
done
echo "Listo: reiniciar para entrar con la raíz congelada."
