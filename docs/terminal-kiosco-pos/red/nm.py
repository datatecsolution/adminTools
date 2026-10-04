"""Acceso a NetworkManager por `nmcli` para pos-red (wifi en autoservicio).

Todo pasa por `nmcli` con una lista de argumentos (nunca por shell). El
servicio corre como el usuario `posred`; los permisos sobre NetworkManager los
da la regla de polkit `50-pos-red.rules`.

La salida "terse" de nmcli (`-t`) separa campos con ':' y escapa ':' y '\\'
dentro de los valores (`\\:` y `\\\\`). `dividir_terse` la deshace.
"""
import re
import socket
import subprocess
import urllib.error
import urllib.request

NMCLI = "nmcli"


class ErrorRed(Exception):
    """Falla al conectar, con un código que la página traduce a un mensaje."""

    def __init__(self, codigo, detalle=""):
        super().__init__(codigo)
        self.codigo = codigo
        self.detalle = detalle


def dividir_terse(linea):
    """Divide una línea de `nmcli -t` respetando ':' y '\\' escapados."""
    campos, actual, escape = [], [], False
    for c in linea:
        if escape:
            actual.append(c)
            escape = False
        elif c == "\\":
            escape = True
        elif c == ":":
            campos.append("".join(actual))
            actual = []
        else:
            actual.append(c)
    campos.append("".join(actual))
    return campos


def parsear_dispositivos(salida):
    """`nmcli -t -f DEVICE,TYPE,STATE,CONNECTION device` → lista de dicts."""
    res = []
    for linea in salida.splitlines():
        if not linea.strip():
            continue
        f = dividir_terse(linea)
        if len(f) < 4:
            continue
        res.append({"dispositivo": f[0], "tipo": f[1], "estado": f[2], "conexion": f[3]})
    return res


def parsear_redes(salida):
    """`nmcli -t -f IN-USE,SSID,SIGNAL,SECURITY device wifi list` → redes.

    Une las entradas repetidas del mismo SSID (varios puntos de acceso o
    bandas) quedándose con la mejor señal. Omite las redes ocultas (SSID vacío).
    """
    por_ssid = {}
    for linea in salida.splitlines():
        if not linea.strip():
            continue
        f = dividir_terse(linea)
        if len(f) < 4:
            continue
        en_uso, ssid, senal, seguridad = f[0].strip(), f[1], f[2], f[3].strip()
        if not ssid:
            continue
        try:
            senal = int(senal)
        except ValueError:
            senal = 0
        seguridad = "" if seguridad in ("", "--") else seguridad
        red = por_ssid.get(ssid)
        if red is None or senal > red["senal"]:
            por_ssid[ssid] = {"ssid": ssid, "senal": senal, "seguridad": seguridad,
                              "abierta": seguridad == "", "conectada": False}
        if en_uso == "*":
            por_ssid[ssid]["conectada"] = True
    return sorted(por_ssid.values(), key=lambda r: (not r["conectada"], -r["senal"], r["ssid"].lower()))


def parsear_conexiones(salida):
    """`nmcli -t -f NAME,UUID,TYPE,DEVICE,FILENAME,AUTOCONNECT-PRIORITY connection show`."""
    res = []
    for linea in salida.splitlines():
        if not linea.strip():
            continue
        f = dividir_terse(linea)
        if len(f) < 6:
            continue
        try:
            prioridad = int(f[5])
        except ValueError:
            prioridad = 0
        res.append({"nombre": f[0], "uuid": f[1], "tipo": f[2], "dispositivo": f[3],
                    "archivo": f[4], "prioridad": prioridad})
    return res


def clasificar_error(texto):
    """Traduce el mensaje de error de nmcli a un código para la página."""
    t = texto.lower()
    if "secrets were required" in t or "no secrets" in t:
        return "clave"
    if "psk: property is invalid" in t or "password is invalid" in t or "key-mgmt" in t and "invalid" in t:
        return "clave_formato"
    if "no network with ssid" in t:
        return "no_encontrada"
    if "timeout" in t or "timed out" in t:
        return "tiempo"
    return "fallo"


class NmcliBackend:
    """Backend real: maneja la wifi con nmcli."""

    def __init__(self, ejecutar=None, timeout_conexion=30):
        self._ejecutar = ejecutar or self._subprocess
        self.timeout_conexion = timeout_conexion

    @staticmethod
    def _subprocess(args, timeout=45):
        p = subprocess.run(args, capture_output=True, text=True, timeout=timeout)
        return p.returncode, p.stdout, p.stderr

    def _nmcli(self, *args, timeout=45):
        return self._ejecutar([NMCLI, *args], timeout=timeout)

    # --- lectura -----------------------------------------------------------
    def dispositivo_wifi(self):
        rc, out, _ = self._nmcli("-t", "-f", "DEVICE,TYPE,STATE,CONNECTION", "device")
        if rc != 0:
            return None
        for d in parsear_dispositivos(out):
            if d["tipo"] == "wifi":
                return d
        return None

    def conexiones(self):
        rc, out, _ = self._nmcli("-t", "-f", "NAME,UUID,TYPE,DEVICE,FILENAME,AUTOCONNECT-PRIORITY",
                                 "connection", "show")
        return parsear_conexiones(out) if rc == 0 else []

    def conexion_activa_wifi(self):
        for c in self.conexiones():
            if c["tipo"] == "802-11-wireless" and c["dispositivo"]:
                return c
        return None

    def escanear(self, dispositivo):
        rc, out, _ = self._nmcli("-t", "-f", "IN-USE,SSID,SIGNAL,SECURITY", "device", "wifi", "list",
                                 "ifname", dispositivo, "--rescan", "yes", timeout=30)
        if rc != 0:  # p. ej. «Scanning not allowed»: devuelve lo último conocido
            rc, out, _ = self._nmcli("-t", "-f", "IN-USE,SSID,SIGNAL,SECURITY", "device", "wifi", "list",
                                     "ifname", dispositivo, "--rescan", "no", timeout=15)
        return parsear_redes(out) if rc == 0 else []

    # --- cambios ------------------------------------------------------------
    def conectar(self, dispositivo, ssid, clave, nombre):
        """Crea la conexión desde el escaneo (lo que funcionó con la ALPHANET).

        Devuelve el UUID. Lanza ErrorRed con el código de la falla; si alcanzó
        a crear la conexión, el UUID va en `detalle` para poder borrarla.
        """
        args = ["--wait", str(self.timeout_conexion), "device", "wifi", "connect", ssid]
        if clave:
            args += ["password", clave]
        args += ["name", nombre, "ifname", dispositivo]
        rc, out, err = self._nmcli(*args, timeout=self.timeout_conexion + 15)
        uuid = self._uuid_de_salida(out) or self._uuid_por_nombre(nombre)
        if rc != 0:
            raise ErrorRed(clasificar_error(err + "\n" + out), uuid or "")
        if not uuid:
            raise ErrorRed("fallo", "")
        return uuid

    def ajustar(self, uuid, prioridad, dns):
        self._nmcli("connection", "modify", "uuid", uuid,
                    "connection.autoconnect-priority", str(prioridad), "ipv4.dns", dns)

    def activar(self, uuid):
        rc, _, _ = self._nmcli("--wait", "30", "connection", "up", "uuid", uuid, timeout=45)
        return rc == 0

    def borrar(self, uuid):
        rc, _, _ = self._nmcli("connection", "delete", "uuid", uuid)
        return rc == 0

    def reiniciar(self):
        self._ejecutar(["systemctl", "reboot"], timeout=15)

    # --- ayudantes ------------------------------------------------------------
    @staticmethod
    def _uuid_de_salida(texto):
        m = re.search(r"([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", texto or "")
        return m.group(1) if m else None

    def _uuid_por_nombre(self, nombre):
        for c in self.conexiones():
            if c["nombre"] == nombre:
                return c["uuid"]
        return None

    # --- conectividad ----------------------------------------------------------
    @staticmethod
    def hay_internet(destinos=(("1.1.1.1", 443), ("8.8.8.8", 443)), timeout=3):
        """Salida a internet: basta con abrir TCP a una IP pública (no depende del DNS)."""
        for host, puerto in destinos:
            try:
                with socket.create_connection((host, puerto), timeout=timeout):
                    return True
            except OSError:
                continue
        return False

    @staticmethod
    def pos_responde(pos_url, timeout=4):
        """Igual que esperando.html: 200 o 404 en /healthz = servidor vivo."""
        url = pos_url.rstrip("/") + "/healthz"
        try:
            with urllib.request.urlopen(url, timeout=timeout) as r:
                return 200 <= r.status < 300
        except urllib.error.HTTPError as e:
            return e.code == 404
        except (OSError, ValueError):
            return False

    def soporte(self, hub, timeout=2):
        """VPN arriba = el hub responde al ping (wg show necesita root)."""
        try:
            rc, _, _ = self._ejecutar(["ping", "-c", "1", "-W", str(timeout), hub], timeout=timeout + 3)
            return rc == 0
        except (OSError, subprocess.TimeoutExpired):
            return False
