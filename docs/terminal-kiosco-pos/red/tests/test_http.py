"""HTTP de pos-red: Host/Origin (DNS rebinding y CSRF), sesión y archivos estáticos."""
import http.client
import json
import os
import sys
import tempfile
import threading
import unittest
from http.server import ThreadingHTTPServer

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from pos_red import CONFIG_POR_DEFECTO, Servicio, crear_handler, hash_pin  # noqa: E402
from simulado import SimuladoBackend  # noqa: E402

WWW = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "www")


class TestHTTP(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        tmp = tempfile.mkdtemp()
        cls.cfg = dict(CONFIG_POR_DEFECTO, registro=os.path.join(tmp, "c.log"), espera_internet=2, puerto=0)
        cls.cfg["pin_sal"] = "11" * 16
        cls.cfg["pin_hash"] = hash_pin("1357", cls.cfg["pin_sal"])
        cls.b = SimuladoBackend(cls.cfg["conn_dir"])
        cls.srv = ThreadingHTTPServer(("127.0.0.1", 0), crear_handler(Servicio(cls.b, cls.cfg), WWW, cls.b))
        cls.puerto = cls.srv.server_address[1]
        # el handler valida Host/Origin contra cfg["puerto"]: se ajusta al puerto real
        cls.srv.RequestHandlerClass = crear_handler(Servicio(cls.b, dict(cls.cfg, puerto=cls.puerto)), WWW, cls.b)
        threading.Thread(target=cls.srv.serve_forever, daemon=True).start()

    @classmethod
    def tearDownClass(cls):
        cls.srv.shutdown()

    def pedir(self, metodo, ruta, cuerpo=None, host=None, origen="auto", sesion=None):
        c = http.client.HTTPConnection("127.0.0.1", self.puerto, timeout=10)
        h = {"Host": host or "127.0.0.1:%d" % self.puerto, "Content-Type": "application/json"}
        if origen == "auto":
            origen = "http://127.0.0.1:%d" % self.puerto
        if origen:
            h["Origin"] = origen
        if sesion:
            h["X-Sesion"] = sesion
        c.request(metodo, ruta, body=json.dumps(cuerpo) if cuerpo is not None else None, headers=h)
        r = c.getresponse()
        datos = r.read()
        try:
            return r.status, json.loads(datos)
        except ValueError:
            return r.status, datos

    def test_estado(self):
        st, d = self.pedir("GET", "/api/estado")
        self.assertEqual(st, 200)
        self.assertTrue(d["pin"])

    def test_host_ajeno_rechazado(self):
        st, _ = self.pedir("GET", "/api/estado", host="malo.example:%d" % self.puerto)
        self.assertEqual(st, 403)

    def test_post_sin_origen_o_ajeno_rechazado(self):
        self.assertEqual(self.pedir("POST", "/api/entrar", {"pin": "1357"}, origen=None)[0], 403)
        self.assertEqual(self.pedir("POST", "/api/entrar", {"pin": "1357"}, origen="https://lafe.datatecsolution.com")[0], 403)

    def test_conectar_sin_sesion_rechazado(self):
        st, d = self.pedir("POST", "/api/conectar", {"ssid": "PRUEBA-LAFE", "clave": "prueba123"})
        self.assertEqual((st, d["codigo"]), (401, "sesion"))

    def test_flujo_completo_por_http(self):
        st, d = self.pedir("POST", "/api/entrar", {"pin": "1357"})
        self.assertEqual(st, 200)
        token = d["token"]
        self.pedir("POST", "/api/_sim", {"accion": "router_prender", "ssid": "PRUEBA-LAFE"})
        st, d = self.pedir("POST", "/api/conectar", {"ssid": "PRUEBA-LAFE", "clave": "prueba123"}, sesion=token)
        self.assertTrue(d["ok"], d)
        st, d = self.pedir("GET", "/api/redes")
        self.assertIn("PRUEBA-LAFE (tienda)", [g["nombre"] for g in d["guardadas"]])
        uuid = d["guardadas"][0]["uuid"]
        self.assertTrue(self.pedir("POST", "/api/olvidar", {"uuid": uuid}, sesion=token)[1]["ok"])

    def test_estaticos_sin_salir_de_www(self):
        st, _ = self.pedir("GET", "/")
        self.assertEqual(st, 200)
        st, _ = self.pedir("GET", "/../pos_red.py")
        self.assertEqual(st, 404)
        st, _ = self.pedir("GET", "/%2e%2e/nm.py")
        self.assertEqual(st, 404)


if __name__ == "__main__":
    unittest.main()
