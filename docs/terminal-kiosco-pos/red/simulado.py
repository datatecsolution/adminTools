"""Backend simulado de pos-red: una caja1-lafe imaginaria, sin NetworkManager.

Sirve para las pruebas automáticas y para recorrer la página de wifi en la Mac
(`python3 pos_red.py --simulado`). Imita lo que importa del comportamiento real:

- «routers» que se pueden apagar, prender o cambiar de clave;
- conexiones de base (archivo en /usr/lib) y de la tienda (archivo en conn_dir);
- autoconectar: sin conexión activa, NetworkManager toma la conexión conocida de
  mayor prioridad cuyo router esté prendido y con la clave correcta.

El estado se controla con `/api/_sim` (solo existe en modo simulado).
"""
import itertools
import threading

from nm import ErrorRed

DIR_BASE = "/usr/lib/NetworkManager/system-connections"


class SimuladoBackend:
    def __init__(self, conn_dir="/home/pos-red/system-connections", demora=0.0):
        self.conn_dir = conn_dir
        self.demora = demora  # segundos que «tarda» conectar (para ver la pantalla de espera)
        self._lock = threading.RLock()
        self._ids = itertools.count(1)
        self.hay_wifi = True
        self.servidor_ok = True
        # Routers alrededor de la caja (lo que aparece en el escaneo).
        self.routers = {
            "Farmacia la Fe ALPHANET": {"encendido": True, "clave": "alphanet1", "seguridad": "WPA1 WPA2", "senal": 78, "internet": True},
            "Mayorga 5G": {"encendido": False, "clave": "mayorga55", "seguridad": "WPA2 WPA3", "senal": 64, "internet": True},
            "LAFE-SOPORTE": {"encendido": False, "clave": "soporte123", "seguridad": "WPA2", "senal": 70, "internet": True},
            "PRUEBA-LAFE": {"encendido": False, "clave": "prueba123", "seguridad": "WPA2", "senal": 81, "internet": True},
            "SIN-INTERNET": {"encendido": True, "clave": "sinred123", "seguridad": "WPA2", "senal": 55, "internet": False},
            "ABIERTA": {"encendido": True, "clave": "", "seguridad": "", "senal": 40, "internet": True},
        }
        # Conexiones de base, como quedarían tras la fase 5 (archivos en /usr/lib).
        self.conns = []
        for ssid, prio in (("Farmacia la Fe ALPHANET", 30), ("Mayorga 5G", 10), ("LAFE-SOPORTE", 5)):
            self._nueva(ssid, ssid, self.routers[ssid]["clave"], prio, DIR_BASE)
        self.activa = None
        self._autoconectar()

    # --- utilidades internas ------------------------------------------------
    def _nueva(self, nombre, ssid, clave, prioridad, carpeta):
        uuid = "00000000-0000-4000-8000-%012d" % next(self._ids)
        archivo = "%s/%s.nmconnection" % (carpeta, nombre)
        self.conns.append({"nombre": nombre, "uuid": uuid, "ssid": ssid, "clave": clave,
                           "prioridad": prioridad, "archivo": archivo, "autoconectar": True})
        return uuid

    def _conn(self, uuid):
        return next((c for c in self.conns if c["uuid"] == uuid), None)

    def _puede(self, c):
        r = self.routers.get(c["ssid"])
        return bool(self.hay_wifi and r and r["encendido"] and r["clave"] == c["clave"])

    def _autoconectar(self):
        """Lo que hace NetworkManager en segundo plano."""
        if self.activa:
            c = self._conn(self.activa)
            if c is None or not self._puede(c):
                self.activa = None
        if self.activa is None:
            for c in sorted(self.conns, key=lambda c: -c["prioridad"]):
                if c["autoconectar"] and self._puede(c):
                    self.activa = c["uuid"]
                    break

    # --- interfaz del backend -------------------------------------------------
    def dispositivo_wifi(self):
        with self._lock:
            if not self.hay_wifi:
                return None
            self._autoconectar()
            c = self._conn(self.activa) if self.activa else None
            return {"dispositivo": "wlx-sim", "tipo": "wifi",
                    "estado": "connected" if c else "disconnected", "conexion": c["nombre"] if c else ""}

    def conexiones(self):
        with self._lock:
            self._autoconectar()
            return [{"nombre": c["nombre"], "uuid": c["uuid"], "tipo": "802-11-wireless",
                     "dispositivo": "wlx-sim" if c["uuid"] == self.activa else "",
                     "archivo": c["archivo"], "prioridad": c["prioridad"]} for c in self.conns]

    def conexion_activa_wifi(self):
        return next((c for c in self.conexiones() if c["dispositivo"]), None)

    def escanear(self, dispositivo):
        with self._lock:
            self._autoconectar()
            activa = self._conn(self.activa) if self.activa else None
            redes = [{"ssid": s, "senal": r["senal"], "seguridad": r["seguridad"], "abierta": r["clave"] == "",
                      "conectada": bool(activa and activa["ssid"] == s)}
                     for s, r in self.routers.items() if r["encendido"]]
            return sorted(redes, key=lambda r: (not r["conectada"], -r["senal"], r["ssid"].lower()))

    def conectar(self, dispositivo, ssid, clave, nombre):
        import time
        if self.demora:
            time.sleep(self.demora)
        with self._lock:
            r = self.routers.get(ssid)
            if r is None or not r["encendido"]:
                raise ErrorRed("no_encontrada")
            if r["clave"] and clave != r["clave"]:
                # NetworkManager deja creada la conexión aunque falle la clave
                uuid = self._nueva(nombre, ssid, clave, 0, self.conn_dir)
                raise ErrorRed("clave", uuid)
            uuid = self._nueva(nombre, ssid, clave, 0, self.conn_dir)
            self.activa = uuid
            return uuid

    def ajustar(self, uuid, prioridad, dns):
        with self._lock:
            c = self._conn(uuid)
            if c:
                c["prioridad"] = prioridad

    def activar(self, uuid):
        with self._lock:
            c = self._conn(uuid)
            if c and self._puede(c):
                self.activa = uuid
                return True
            return False

    def borrar(self, uuid):
        with self._lock:
            c = self._conn(uuid)
            if not c:
                return False
            self.conns.remove(c)
            if self.activa == uuid:
                self.activa = None
                self._autoconectar()
            return True

    def reiniciar(self):
        with self._lock:
            self.activa = None
            self._autoconectar()

    def hay_internet(self):
        with self._lock:
            self._autoconectar()
            c = self._conn(self.activa) if self.activa else None
            return bool(c and self.routers[c["ssid"]]["internet"])

    def pos_responde(self, pos_url):
        return self.hay_internet() and self.servidor_ok

    def soporte(self, hub):
        return self.hay_internet()

    # --- control de la simulación (/api/_sim) -----------------------------------
    def controlar(self, accion, ssid=None, valor=None):
        with self._lock:
            if accion in ("router_apagar", "router_prender"):
                self.routers[ssid]["encendido"] = accion == "router_prender"
            elif accion == "router_agregar":
                self.routers[ssid] = {"encendido": True, "clave": valor or "", "seguridad": "WPA2" if valor else "",
                                      "senal": 50, "internet": True}
            elif accion == "router_clave":
                self.routers[ssid]["clave"] = valor or ""
            elif accion == "servidor":
                self.servidor_ok = bool(valor)
            elif accion == "wifi":
                self.hay_wifi = bool(valor)
            elif accion == "autoconectar":
                for c in self.conns:
                    if c["nombre"] == ssid:
                        c["autoconectar"] = bool(valor)
            else:
                raise ValueError("acción desconocida: %s" % accion)
            self._autoconectar()
            return self.resumen()

    def resumen(self):
        with self._lock:
            activa = self._conn(self.activa) if self.activa else None
            return {"routers": {s: {"encendido": r["encendido"], "clave": r["clave"]} for s, r in self.routers.items()},
                    "activa": activa["nombre"] if activa else None,
                    "servidor_ok": self.servidor_ok, "hay_wifi": self.hay_wifi,
                    "conexiones": [c["nombre"] for c in self.conns]}
