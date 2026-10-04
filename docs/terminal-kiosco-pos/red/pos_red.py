#!/usr/bin/env python3
"""pos-red: wifi en autoservicio para las terminales kiosco (fase 5).

Servicio local que deja al encargado conectar la caja a una red wifi desde la
pantalla, sin soporte. Escucha SOLO en 127.0.0.1:8090 y sirve la página de wifi
(www/) más una API mínima. Ver docs/terminal-kiosco-pos/analisis-wifi-autoservicio.md
y plan-wifi-autoservicio.md.

Uso:
  python3 pos_red.py                      # servicio real (NetworkManager)
  python3 pos_red.py --simulado           # caja imaginaria, para la Mac y las pruebas
  python3 pos_red.py --fijar-pin          # pide el PIN de la tienda y lo guarda con hash
"""
import argparse
import getpass
import hashlib
import hmac
import json
import os
import secrets
import sys
import threading
import time
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nm import ErrorRed, NmcliBackend  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
CONFIG_POR_DEFECTO = {
    "escucha": "127.0.0.1",
    "puerto": 8090,
    "pos_url": "https://lafe.datatecsolution.com/",
    "hub": "10.10.0.1",
    "teclado": "auto",            # auto | pantalla | fisico
    "conn_dir": "/home/pos-red/system-connections",
    "registro": "/home/pos-red/cambios.log",
    "prioridad_tienda": 40,
    "dns_extra": "1.1.1.1",
    "espera_internet": 30,        # segundos para dar por buena una red
    "pin_hash": None,             # pbkdf2-sha256 hex; sin PIN configurado no se pide
    "pin_sal": None,
    "pin_intentos": 3,
    "pin_bloqueo": 60,
    "sesion_minutos": 15,
}
SUFIJO_TIENDA = " (tienda)"
MENSAJES = {
    "clave": "La clave no es correcta.",
    "clave_formato": "La clave no tiene un formato válido (8 a 63 caracteres).",
    "no_encontrada": "No se encontró la red. Acérquese o búsquela de nuevo.",
    "sin_internet": "Esa red no tiene salida a internet.",
    "tiempo": "La red no respondió a tiempo.",
    "fallo": "No se pudo conectar a esa red.",
    "sin_wifi": "Esta caja no tiene red inalámbrica; conecte el cable.",
}


# ----------------------------------------------------------------------------
# PIN
# ----------------------------------------------------------------------------
def hash_pin(pin, sal):
    return hashlib.pbkdf2_hmac("sha256", pin.encode(), bytes.fromhex(sal), 200_000).hex()


def pin_valido_formato(pin):
    return isinstance(pin, str) and pin.isdigit() and 4 <= len(pin) <= 6


# ----------------------------------------------------------------------------
# Validaciones de entrada
# ----------------------------------------------------------------------------
def validar_ssid(ssid):
    return isinstance(ssid, str) and 1 <= len(ssid.encode("utf-8")) <= 32 and "\x00" not in ssid


def validar_clave(clave, abierta):
    if abierta:
        return clave in ("", None)
    if not isinstance(clave, str):
        return False
    if len(clave) == 64 and all(c in "0123456789abcdefABCDEF" for c in clave):
        return True
    return 8 <= len(clave) <= 63 and all(32 <= ord(c) < 127 for c in clave)


# ----------------------------------------------------------------------------
# Lógica del servicio (independiente del HTTP: es lo que prueban los tests)
# ----------------------------------------------------------------------------
class Servicio:
    def __init__(self, backend, config, reloj=time.monotonic, dormir=time.sleep):
        self.b = backend
        self.cfg = config
        self.reloj = reloj
        self.dormir = dormir
        self._lock = threading.Lock()
        self._fallos_pin = 0
        self._bloqueado_hasta = 0.0
        self._sesiones = {}

    # --- PIN y sesiones -------------------------------------------------------
    def pin_requerido(self):
        return bool(self.cfg.get("pin_hash"))

    def entrar(self, pin):
        """Valida el PIN. Devuelve (token|None, código de error|None, segundos de bloqueo)."""
        ahora = self.reloj()
        if ahora < self._bloqueado_hasta:
            return None, "bloqueado", int(self._bloqueado_hasta - ahora) + 1
        if not self.pin_requerido():
            return self._nueva_sesion(), None, 0
        ok = pin_valido_formato(pin) and hmac.compare_digest(
            hash_pin(pin, self.cfg["pin_sal"]), self.cfg["pin_hash"])
        if ok:
            self._fallos_pin = 0
            return self._nueva_sesion(), None, 0
        self._fallos_pin += 1
        if self._fallos_pin >= self.cfg["pin_intentos"]:
            self._fallos_pin = 0
            self._bloqueado_hasta = ahora + self.cfg["pin_bloqueo"]
            return None, "bloqueado", self.cfg["pin_bloqueo"]
        return None, "pin", 0

    def _nueva_sesion(self):
        token = secrets.token_hex(16)
        self._sesiones[token] = self.reloj() + self.cfg["sesion_minutos"] * 60
        return token

    def sesion_valida(self, token):
        vence = self._sesiones.get(token or "")
        if vence is None:
            return False
        if self.reloj() > vence:
            del self._sesiones[token]
            return False
        return True

    # --- lectura ------------------------------------------------------------
    def estado(self):
        dev = self.b.dispositivo_wifi()
        internet = self.b.hay_internet()
        return {
            "wifi": dev is not None,
            "conectado_a": (dev or {}).get("conexion") or None,
            "internet": internet,
            "pos": self.b.pos_responde(self.cfg["pos_url"]) if internet else False,
            "soporte": self.b.soporte(self.cfg["hub"]) if internet else False,
            "teclado": self.cfg.get("teclado", "auto"),
            "pin": self.pin_requerido(),
            "pos_url": self.cfg["pos_url"],
        }

    def es_de_la_tienda(self, conexion):
        return (conexion.get("archivo") or "").startswith(self.cfg["conn_dir"].rstrip("/") + "/")

    def redes(self):
        dev = self.b.dispositivo_wifi()
        if dev is None:
            return {"wifi": False, "redes": [], "guardadas": []}
        guardadas = [{"nombre": c["nombre"], "uuid": c["uuid"]}
                     for c in self.b.conexiones()
                     if c["tipo"] == "802-11-wireless" and self.es_de_la_tienda(c)]
        return {"wifi": True, "redes": self.b.escanear(dev["dispositivo"]), "guardadas": guardadas}

    # --- cambios --------------------------------------------------------------
    def conectar(self, ssid, clave):
        """Pasos de §4.3 del análisis. Devuelve dict con ok, código y mensaje."""
        if not self._lock.acquire(blocking=False):
            return {"ok": False, "codigo": "ocupado", "mensaje": "Ya hay una conexión en curso."}
        try:
            return self._conectar(ssid, clave)
        finally:
            self._lock.release()

    def _conectar(self, ssid, clave):
        dev = self.b.dispositivo_wifi()
        if dev is None:
            return self._resultado(False, "sin_wifi", ssid)
        # Un router recién encendido puede no estar aún en el escaneo: dos reintentos.
        red = None
        for intento in range(3):
            red = {r["ssid"]: r for r in self.b.escanear(dev["dispositivo"])}.get(ssid)
            if red is not None:
                break
            if intento < 2:
                self.dormir(3)
        if not validar_ssid(ssid):
            return self._resultado(False, "fallo", ssid)
        if red is None:
            return self._resultado(False, "no_encontrada", ssid)
        if not validar_clave(clave, red["abierta"]):
            return self._resultado(False, "clave_formato", ssid)

        anterior = self.b.conexion_activa_wifi()
        nombre = ssid + SUFIJO_TIENDA
        # «Misma red, clave nueva»: la conexión de la tienda con la clave vieja se reemplaza.
        for c in self.b.conexiones():
            if c["nombre"] == nombre and self.es_de_la_tienda(c):
                self.b.borrar(c["uuid"])

        try:
            uuid = self.b.conectar(dev["dispositivo"], ssid, clave or "", nombre)
        except ErrorRed as e:
            if e.detalle:
                self.b.borrar(e.detalle)
            self._volver_a(anterior)
            return self._resultado(False, e.codigo, ssid, anterior)

        self.b.ajustar(uuid, self.cfg["prioridad_tienda"], self.cfg["dns_extra"])
        # Lo que decide es la salida a internet; el POS se informa aparte (§4.3 punto 3).
        limite = self.reloj() + self.cfg["espera_internet"]
        while not self.b.hay_internet():
            if self.reloj() >= limite:
                self.b.borrar(uuid)
                self._volver_a(anterior)
                return self._resultado(False, "sin_internet", ssid, anterior)
            self.dormir(2)
        pos = self.b.pos_responde(self.cfg["pos_url"])
        res = self._resultado(True, "ok", ssid, anterior)
        res["pos"] = pos
        res["mensaje"] = ("Conectado a %s." % ssid) if pos else \
            "La wifi funciona, pero el servidor no responde. Llame a soporte."
        return res

    def _volver_a(self, anterior):
        if anterior:
            self.b.activar(anterior["uuid"])

    def olvidar(self, uuid):
        c = next((c for c in self.b.conexiones() if c["uuid"] == uuid), None)
        if c is None:
            return {"ok": False, "codigo": "no_existe", "mensaje": "Esa red ya no está guardada."}
        if not self.es_de_la_tienda(c):
            self._registrar("olvidar_rechazado", c["nombre"], "no es de la tienda")
            return {"ok": False, "codigo": "protegida", "mensaje": "Esa red no se puede borrar desde aquí."}
        ok = self.b.borrar(uuid)
        self._registrar("olvidar", c["nombre"], "ok" if ok else "fallo")
        return {"ok": ok, "codigo": "ok" if ok else "fallo",
                "mensaje": "Red olvidada." if ok else "No se pudo olvidar la red."}

    def reiniciar(self):
        self._registrar("reiniciar", "", "pedido desde la pantalla")
        threading.Timer(2.0, self.b.reiniciar).start()
        return {"ok": True, "mensaje": "Reiniciando la caja…"}

    # --- registro -------------------------------------------------------------
    def _resultado(self, ok, codigo, ssid, anterior=None):
        self._registrar("conectar", ssid, codigo, (anterior or {}).get("nombre"))
        return {"ok": ok, "codigo": codigo, "mensaje": MENSAJES.get(codigo, "")}

    def _registrar(self, accion, red, resultado, anterior=None):
        linea = {"fecha": datetime.now().isoformat(timespec="seconds"), "accion": accion,
                 "red": red, "resultado": resultado}
        if anterior:
            linea["anterior"] = anterior
        try:
            with open(self.cfg["registro"], "a", encoding="utf-8") as f:
                f.write(json.dumps(linea, ensure_ascii=False) + "\n")
        except OSError:
            pass


# ----------------------------------------------------------------------------
# HTTP
# ----------------------------------------------------------------------------
TIPOS = {".html": "text/html; charset=utf-8", ".js": "application/javascript; charset=utf-8",
         ".css": "text/css; charset=utf-8", ".svg": "image/svg+xml"}


def crear_handler(servicio, www, simulado=None):
    host_ok = {"127.0.0.1:%d" % servicio.cfg["puerto"], "localhost:%d" % servicio.cfg["puerto"]}
    origen_ok = {"http://" + h for h in host_ok}

    class Handler(BaseHTTPRequestHandler):
        server_version = "pos-red"

        def log_message(self, fmt, *args):
            pass

        def _json(self, codigo, datos, cors_null=False):
            cuerpo = json.dumps(datos, ensure_ascii=False).encode()
            self.send_response(codigo)
            if cors_null:
                # esperando.html corre desde file:// (origen «null») y solo LEE el estado
                self.send_header("Access-Control-Allow-Origin", "null")
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(cuerpo)))
            self.end_headers()
            self.wfile.write(cuerpo)

        def _host_valido(self):
            # Contra DNS rebinding: solo se atiende a 127.0.0.1/localhost en el puerto propio.
            return self.headers.get("Host", "") in host_ok

        def _origen_valido(self):
            return self.headers.get("Origin", "") in origen_ok

        def do_GET(self):
            if not self._host_valido():
                return self._json(403, {"error": "host"})
            ruta = self.path.split("?")[0]
            if ruta == "/api/estado":
                return self._json(200, servicio.estado(), cors_null=self.headers.get("Origin") == "null")
            if ruta == "/api/redes":
                return self._json(200, servicio.redes())
            if ruta in ("/", "/index.html"):
                ruta = "/index.html"
            nombre = os.path.basename(ruta)
            archivo = os.path.join(www, nombre)
            ext = os.path.splitext(nombre)[1]
            if nombre and ext in TIPOS and os.path.isfile(archivo):
                with open(archivo, "rb") as f:
                    datos = f.read()
                self.send_response(200)
                self.send_header("Content-Type", TIPOS[ext])
                self.send_header("Cache-Control", "no-store")
                self.send_header("X-Frame-Options", "DENY")
                self.send_header("X-Content-Type-Options", "nosniff")
                self.send_header("Content-Length", str(len(datos)))
                self.end_headers()
                self.wfile.write(datos)
                return
            self._json(404, {"error": "no existe"})

        def do_POST(self):
            if not self._host_valido():
                return self._json(403, {"error": "host"})
            if not self._origen_valido():
                return self._json(403, {"error": "origen"})
            largo = int(self.headers.get("Content-Length") or 0)
            if largo > 4096:
                return self._json(413, {"error": "muy grande"})
            try:
                datos = json.loads(self.rfile.read(largo) or b"{}")
            except ValueError:
                return self._json(400, {"error": "json"})
            ruta = self.path.split("?")[0]
            if ruta == "/api/reiniciar":
                return self._json(200, servicio.reiniciar())
            if ruta == "/api/entrar":
                token, error, espera = servicio.entrar(str(datos.get("pin", "")))
                if token:
                    return self._json(200, {"ok": True, "token": token})
                return self._json(403 if error == "pin" else 429, {"ok": False, "codigo": error, "espera": espera})
            if ruta == "/api/_sim" and simulado is not None:
                try:
                    return self._json(200, simulado.controlar(datos.get("accion"), datos.get("ssid"), datos.get("valor")))
                except (KeyError, ValueError) as e:
                    return self._json(400, {"error": str(e)})
            if ruta in ("/api/conectar", "/api/olvidar"):
                if not servicio.sesion_valida(self.headers.get("X-Sesion")):
                    return self._json(401, {"ok": False, "codigo": "sesion"})
                if ruta == "/api/conectar":
                    return self._json(200, servicio.conectar(datos.get("ssid"), datos.get("clave")))
                return self._json(200, servicio.olvidar(datos.get("uuid")))
            self._json(404, {"error": "no existe"})

    return Handler


def cargar_config(ruta):
    cfg = dict(CONFIG_POR_DEFECTO)
    if ruta and os.path.isfile(ruta):
        with open(ruta, encoding="utf-8") as f:
            cfg.update(json.load(f))
    return cfg


def fijar_pin(ruta):
    cfg = cargar_config(ruta)
    if sys.stdin.isatty():
        pin = getpass.getpass("PIN de la tienda (4 a 6 dígitos): ")
        repetido = getpass.getpass("Repita el PIN: ")
    else:  # desde el instalador: dos líneas por la entrada estándar
        pin, repetido = (sys.stdin.readline().strip(), sys.stdin.readline().strip())
    if not pin_valido_formato(pin):
        sys.exit("El PIN debe tener de 4 a 6 dígitos.")
    if repetido != pin:
        sys.exit("Los PIN no coinciden.")
    sal = secrets.token_hex(16)
    guardar = {}
    if os.path.isfile(ruta):
        with open(ruta, encoding="utf-8") as f:
            guardar = json.load(f)
    guardar.update({"pin_sal": sal, "pin_hash": hash_pin(pin, sal)})
    tmp = ruta + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(guardar, f, indent=2, ensure_ascii=False)
    os.chmod(tmp, 0o640)
    os.replace(tmp, ruta)
    print("PIN guardado en %s" % ruta)
    return cfg


def main(argv=None):
    ap = argparse.ArgumentParser(description="Wifi en autoservicio (pos-red)")
    ap.add_argument("--config", default=os.environ.get("POS_RED_CONFIG", "/opt/pos/red/config.json"))
    ap.add_argument("--simulado", action="store_true", help="caja imaginaria, sin NetworkManager")
    ap.add_argument("--fijar-pin", action="store_true")
    ap.add_argument("--puerto", type=int)
    a = ap.parse_args(argv)

    if a.fijar_pin:
        fijar_pin(a.config)
        return
    cfg = cargar_config(a.config)
    if a.puerto:
        cfg["puerto"] = a.puerto
    simulado = None
    if a.simulado:
        from simulado import SimuladoBackend
        simulado = SimuladoBackend(cfg["conn_dir"], demora=1.5)
        backend = simulado
        cfg["espera_internet"] = 6
        if cfg["registro"].startswith("/home/pos-red"):
            cfg["registro"] = os.path.join(AQUI, "cambios-simulado.log")
    else:
        backend = NmcliBackend()
    servicio = Servicio(backend, cfg)
    srv = ThreadingHTTPServer((cfg["escucha"], cfg["puerto"]),
                              crear_handler(servicio, os.path.join(AQUI, "www"), simulado))
    print("pos-red escuchando en http://%s:%d%s" % (cfg["escucha"], cfg["puerto"], " (SIMULADO)" if simulado else ""))
    srv.serve_forever()


if __name__ == "__main__":
    main()
