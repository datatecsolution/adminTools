#!/bin/bash
# Ciclo completo de la etapa D en la caja virtual: aplicar → reiniciar → puerta D →
# quitar → reiniciar → puerta C (y esperando.html original) → aplicar → reiniciar → puerta D.
# Desde la Mac: bash ciclo-d.sh
cd "$(dirname "$0")"
filtro() { grep -v "LC_\|LANG\|perl\|locale\|are supported"; }
reiniciar() {
  bash vm.sh reiniciar 2>/dev/null; sleep 15
  for _ in $(seq 1 40); do bash vm.sh ssh true 2>/dev/null && break; sleep 3; done
  sleep 35
}
aplicar() {
  bash vm.sh scp -r ../../red ../../bin ../../www soporte@127.0.0.1:pos-terminal/ >/dev/null
  printf '2468\n2468\n' | bash vm.sh ssh 'sudo bash ~/pos-terminal/bin/fase5-wifi-autoservicio.sh --aplicar --soporte-clave soporte123 --pin-stdin' 2>&1 | filtro | grep -E "respaldo de antes|FALLA|ABORTADO|LISTO"
}
RES=0
echo "##### 1. aplicar";            aplicar; reiniciar
bash verificar-d.sh 2>&1 | filtro | grep -E "✘|PUERTA" || RES=1
echo "##### 2. quitar";             bash vm.sh ssh 'sudo bash ~/pos-terminal/bin/fase5-wifi-autoservicio.sh --quitar' 2>&1 | filtro | grep -E "OK|FALLA"; reiniciar
PYTHONIOENCODING=utf-8 bash verificar-c.sh 2>&1 | filtro | grep -E "✘|PUERTA" || RES=1
L=$(bash vm.sh ssh 'grep -c revisarRed /opt/pos/www/esperando.html; systemctl is-enabled pos-red 2>/dev/null' 2>&1 | filtro | tr '\n' ' ')
echo "   esperando.html con fase 5: ${L%% *} líneas (debe ser 0) · pos-red: $(echo $L | cut -d' ' -f2-)"
[ "${L%% *}" = 0 ] || RES=1
echo "##### 3. aplicar de nuevo";   aplicar; reiniciar
bash verificar-d.sh 2>&1 | filtro | grep -E "✘|PUERTA" || RES=1
echo; [ $RES = 0 ] && echo "CICLO D: OK" || echo "CICLO D: CON FALLAS"
