#!/bin/bash
# Laboratorio de wifi en autoservicio: una «caja1-lafe virtual» en QEMU (etapa C del plan).
#
#   bash vm.sh crear          # discos, llave SSH y disco de cloud-init (una vez)
#   bash vm.sh iniciar        # arranca la VM en segundo plano (VNC en 127.0.0.1:5901)
#   bash vm.sh ssh [cmd]      # entra como «soporte» (sudo sin clave: SOLO laboratorio)
#   bash vm.sh lab <args>     # controla los routers simulados (ver lab-ctl)
#   bash vm.sh captura nombre # captura de la pantalla de la caja (PNG)
#   bash vm.sh reiniciar | apagar | estado
#   bash vm.sh instantanea guardar|volver|lista <nombre>   # requiere la VM apagada
#
# Todo vive en $LAB (por defecto ~/pos-lab-wifi). Ver lab/README.md.
set -euo pipefail
LAB=${LAB:-$HOME/pos-lab-wifi}
AQUI=$(cd "$(dirname "$0")" && pwd)
BASE=$LAB/imagenes/debian-13-generic-arm64.qcow2
RAIZ=$LAB/raiz.qcow2
HOME_DISK=$LAB/home.qcow2
SEED=$LAB/seed.iso
LLAVE=$LAB/llave
QMP=$LAB/qmp.sock
PUERTO_SSH=2222
FIRMWARE=/opt/homebrew/share/qemu/edk2-aarch64-code.fd
VARS_PLANTILLA=/opt/homebrew/share/qemu/edk2-arm-vars.fd
SSH_OPCIONES=(-i "$LLAVE" -p $PUERTO_SSH -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR -o ConnectTimeout=5)

corriendo() { [ -f "$LAB/vm.pid" ] && kill -0 "$(cat "$LAB/vm.pid")" 2>/dev/null; }
qmp() {  # qmp '{"execute":"..."}'
  python3 - "$QMP" "$1" <<'EOF'
import json, socket, sys
s = socket.socket(socket.AF_UNIX); s.connect(sys.argv[1]); f = s.makefile("rw")
f.readline(); f.write('{"execute":"qmp_capabilities"}\n'); f.flush(); f.readline()
f.write(sys.argv[2] + "\n"); f.flush()
while True:
    l = json.loads(f.readline())
    if "return" in l or "error" in l:
        print(json.dumps(l)); break
EOF
}

crear() {
  [ -f "$BASE" ] || { echo "falta la imagen base $BASE"; exit 1; }
  [ -f "$RAIZ" ] && { echo "ya existe $RAIZ (borrar $LAB/raiz.qcow2 para recrear)"; exit 1; }
  [ -f "$LLAVE" ] || ssh-keygen -q -t ed25519 -N "" -C "pos-lab-wifi" -f "$LLAVE"
  qemu-img create -q -f qcow2 -F qcow2 -b "$BASE" "$RAIZ" 16G
  qemu-img create -q -f qcow2 "$HOME_DISK" 4G          # /home aparte, como sda3 en caja1-lafe
  cp "$VARS_PLANTILLA" "$LAB/vars.fd"
  local seed; seed=$(mktemp -d)
  cat > "$seed/meta-data" <<EOF
instance-id: caja1-lab-$(date +%s)
local-hostname: caja1-lab
EOF
  cat > "$seed/user-data" <<EOF
#cloud-config
hostname: caja1-lab
users:
  - name: soporte
    groups: [sudo]
    shell: /bin/bash
    sudo: "ALL=(ALL) NOPASSWD:ALL"
    lock_passwd: true
    ssh_authorized_keys:
      - $(cat "$LLAVE.pub")
ssh_pwauth: false
package_update: false
EOF
  # mgmt0: administración SIN puerta de enlace (la caja solo sale a internet por wifi).
  # uplink0: internet para instalar paquetes; después pasa al espacio de red «router».
  cat > "$seed/network-config" <<EOF
version: 2
ethernets:
  mgmt0:
    match: {macaddress: "52:54:00:00:00:10"}
    set-name: mgmt0
    dhcp4: false
    addresses: [10.0.2.15/24]
  uplink0:
    match: {macaddress: "52:54:00:00:00:11"}
    set-name: uplink0
    dhcp4: true
EOF
  rm -f "$SEED"
  hdiutil makehybrid -quiet -iso -joliet -default-volume-name cidata -o "$SEED" "$seed"
  rm -rf "$seed"
  echo "creado: $RAIZ (16G, sobre la imagen base), $HOME_DISK (4G), $SEED, llave $LLAVE"
}

iniciar() {
  corriendo && { echo "ya está corriendo (pid $(cat "$LAB/vm.pid"))"; return; }
  rm -f "$QMP"
  qemu-system-aarch64 -name caja1-lab -M virt,accel=hvf -cpu host -smp 4 -m 4096 \
    -drive if=pflash,format=raw,readonly=on,file="$FIRMWARE" \
    -drive if=pflash,format=raw,file="$LAB/vars.fd" \
    -drive if=virtio,format=qcow2,file="$RAIZ" \
    -drive if=virtio,format=qcow2,file="$HOME_DISK" \
    -drive if=virtio,format=raw,readonly=on,file="$SEED" \
    -netdev user,id=mgmt,net=10.0.2.0/24,hostfwd=tcp:127.0.0.1:$PUERTO_SSH-:22,hostfwd=tcp:127.0.0.1:9223-:9222 \
    -device virtio-net-pci,netdev=mgmt,mac=52:54:00:00:00:10 \
    -netdev user,id=uplink,net=10.0.3.0/24 \
    -device virtio-net-pci,netdev=uplink,mac=52:54:00:00:00:11 \
    -device virtio-gpu-pci,xres=1366,yres=768 -device qemu-xhci -device usb-kbd -device usb-tablet \
    -display none -vnc 127.0.0.1:1 \
    -qmp unix:"$QMP",server,nowait \
    -serial file:"$LAB/serial.log" \
    -daemonize -pidfile "$LAB/vm.pid"
  echo "VM arrancando (pid $(cat "$LAB/vm.pid")) · VNC 127.0.0.1:5901 · ssh: bash vm.sh ssh"
  for _ in $(seq 1 90); do
    ssh "${SSH_OPCIONES[@]}" soporte@127.0.0.1 true 2>/dev/null && { echo "SSH listo"; return; }
    sleep 2
  done
  echo "SSH no respondió en 3 minutos (ver $LAB/serial.log)"; exit 1
}

case "${1:-}" in
  crear) crear ;;
  iniciar) iniciar ;;
  ssh) shift; exec ssh "${SSH_OPCIONES[@]}" soporte@127.0.0.1 "$@" ;;
  scp) shift; exec scp -i "$LLAVE" -P $PUERTO_SSH -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR "$@" ;;
  lab) shift; exec ssh "${SSH_OPCIONES[@]}" soporte@127.0.0.1 sudo /usr/local/sbin/lab-ctl "$@" ;;
  captura)
    nombre=${2:-pantalla}; mkdir -p "$LAB/capturas"
    qmp "{\"execute\":\"screendump\",\"arguments\":{\"filename\":\"$LAB/capturas/$nombre.png\",\"format\":\"png\"}}" >/dev/null
    echo "$LAB/capturas/$nombre.png" ;;
  reiniciar) ssh "${SSH_OPCIONES[@]}" soporte@127.0.0.1 sudo systemctl reboot || true ;;
  apagar)
    corriendo || { echo "no está corriendo"; exit 0; }
    qmp '{"execute":"system_powerdown"}' >/dev/null
    for _ in $(seq 1 60); do corriendo || { echo "apagada"; rm -f "$LAB/vm.pid"; exit 0; }; sleep 1; done
    echo "no se apagó sola: forzando"; kill "$(cat "$LAB/vm.pid")"; rm -f "$LAB/vm.pid" ;;
  estado) corriendo && echo "corriendo (pid $(cat "$LAB/vm.pid"))" || echo "apagada" ;;
  instantanea)
    corriendo && { echo "apague la VM antes (bash vm.sh apagar)"; exit 1; }
    case "${2:-}" in
      guardar) qemu-img snapshot -c "$3" "$RAIZ"; qemu-img snapshot -c "$3" "$HOME_DISK"; cp "$LAB/vars.fd" "$LAB/vars-$3.fd"; echo "instantánea $3 guardada" ;;
      volver) qemu-img snapshot -a "$3" "$RAIZ"; qemu-img snapshot -a "$3" "$HOME_DISK"; cp "$LAB/vars-$3.fd" "$LAB/vars.fd"; echo "vuelta a $3" ;;
      lista) qemu-img snapshot -l "$RAIZ" ;;
      *) echo "uso: instantanea guardar|volver|lista <nombre>"; exit 1 ;;
    esac ;;
  *) sed -n '2,13p' "$0"; exit 1 ;;
esac
