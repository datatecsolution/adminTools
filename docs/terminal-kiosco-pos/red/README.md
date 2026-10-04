# pos-red — wifi en autoservicio (fase 5)

Servicio local que deja al encargado conectar la terminal a una red wifi desde la pantalla, sin soporte. Diseño y plan: [`../analisis-wifi-autoservicio.md`](../analisis-wifi-autoservicio.md) y [`../plan-wifi-autoservicio.md`](../plan-wifi-autoservicio.md).

| Archivo | Qué es |
|---|---|
| `pos_red.py` | Servicio HTTP en `127.0.0.1:8090` (página + API): conectar, olvidar, PIN, reiniciar y registro en `cambios.log`. `--simulado` usa la caja imaginaria; `--fijar-pin` guarda el PIN con hash |
| `nm.py` | Acceso a NetworkManager por `nmcli` (lista de argumentos, sin shell) y parseo de la salida `-t` |
| `simulado.py` | Caja1-lafe imaginaria para pruebas y para la Mac: routers que se apagan o cambian de clave, conexiones de base y de la tienda, autoconectar |
| `www/index.html` | Página de wifi: PIN, redes, clave, resultados y vuelta automática al POS. Teclado en pantalla solo si la caja es táctil (`any-pointer: coarse` o `teclado: "pantalla"`) |
| `tests/` | Pruebas unitarias (Python, sin dependencias) |
| `tests_e2e/pagina.e2e.mjs` | Pruebas de la página en un Chrome headless sin extensiones, con teclas y clics reales (Node ≥ 22, sin dependencias) |
| `capturas/` | Capturas de cada escenario de la última corrida de las e2e |

## Probar en la Mac

```bash
cd docs/terminal-kiosco-pos/red
python3 -m unittest discover -s tests -t .        # 37 pruebas
node tests_e2e/pagina.e2e.mjs capturas            # 12 escenarios + capturas
python3 pos_red.py --simulado --config /tmp/x.json  # y abrir http://127.0.0.1:8090/
```

Para cambiar la simulación a mano, con `POST /api/_sim` (solo existe con `--simulado`):

```bash
curl -X POST -H 'Origin: http://127.0.0.1:8090' -H 'Content-Type: application/json' \
  -d '{"accion":"router_apagar","ssid":"Farmacia la Fe ALPHANET"}' http://127.0.0.1:8090/api/_sim
```

Acciones: `router_apagar`, `router_prender`, `router_clave` (con `valor`), `router_agregar`, `servidor` (`valor` true/false), `wifi` (true/false) y `autoconectar`.

## Seguridad (resumen)

- Escucha solo en `127.0.0.1`. Rechaza `Host` ajeno (DNS rebinding) y POST con `Origin` ajeno (CSRF).
- Conectar y olvidar requieren la sesión que da el PIN. Tres PIN incorrectos bloquean 1 minuto.
- Solo se olvidan redes cuyo archivo está en `conn_dir` (`/home/pos-red/...`); las de base, nunca.
- Los nombres de red los elige cualquiera con un router cerca: la página los muestra con `textContent`, nunca como HTML (escenario 11 de las e2e).
- La clave y el PIN no usan `type="password"`, sino texto enmascarado con CSS: así ningún gestor de contraseñas, ni el «¿Guardar contraseña?» de Chromium, aparece en la caja.

## Pendiente (etapas C–H del plan)

Instalador `fase5-wifi-autoservicio.sh`, unidad systemd, regla polkit, redes de seguridad, laboratorio en QEMU con `mac80211_hwsim` y la ventana en caja1-lafe.
