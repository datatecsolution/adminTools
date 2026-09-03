#!/bin/bash
set -uo pipefail
echo "VT inicial: $(cat /sys/class/tty/tty0/active)"
echo
echo "=== chvt 1 como root, con codigo de salida ==="
chvt 1; echo "chvt exit=$?"
echo "--- muestreo del VT activo cada 0.5s durante 6s ---"
for i in $(seq 1 12); do printf '%s ' "$(cat /sys/class/tty/tty0/active)"; sleep 0.5; done; echo
echo
echo "=== modo del VT (VT_PROCESS = alguien debe autorizar) ==="
python3 - <<'PY'
import fcntl, struct, os
VT_GETMODE = 0x5601
try:
    fd = os.open('/dev/tty0', os.O_RDWR)
    buf = fcntl.ioctl(fd, VT_GETMODE, struct.pack('BBhhh', 0,0,0,0,0))
    mode, waitv, relsig, acqsig, frsig = struct.unpack('BBhhh', buf)
    print(f"  mode={'VT_PROCESS (controlado por un proceso)' if mode==1 else 'VT_AUTO (kernel)'}  relsig={relsig} acqsig={acqsig}")
    os.close(fd)
except Exception as e:
    print("  no pude leer VT_GETMODE:", e)
PY
echo
echo "=== volviendo a tty2 ==="
chvt 2; echo "chvt exit=$?"; sleep 1
echo "VT final: $(cat /sys/class/tty/tty0/active)"
