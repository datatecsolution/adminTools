"""Lógica de pos-red contra el backend simulado: cada camino de §4.3 y las protecciones."""
import json
import os
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from pos_red import CONFIG_POR_DEFECTO, Servicio, hash_pin, validar_clave, validar_ssid  # noqa: E402
from simulado import SimuladoBackend  # noqa: E402


class Reloj:
    def __init__(self):
        self.t = 1000.0

    def __call__(self):
        return self.t

    def dormir(self, s):
        self.t += s


def nuevo(pin=None):
    reloj = Reloj()
    tmp = tempfile.mkdtemp()
    cfg = dict(CONFIG_POR_DEFECTO, registro=os.path.join(tmp, "cambios.log"), espera_internet=10)
    if pin:
        cfg["pin_sal"] = "00" * 16
        cfg["pin_hash"] = hash_pin(pin, cfg["pin_sal"])
    b = SimuladoBackend(cfg["conn_dir"])
    return Servicio(b, cfg, reloj=reloj, dormir=reloj.dormir), b, reloj, cfg


def registro(cfg):
    with open(cfg["registro"], encoding="utf-8") as f:
        return [json.loads(l) for l in f]


class TestConectar(unittest.TestCase):
    def test_arranque_normal_conectado_a_la_farmacia(self):
        s, b, _, _ = nuevo()
        e = s.estado()
        self.assertEqual(e["conectado_a"], "Farmacia la Fe ALPHANET")
        self.assertTrue(e["internet"] and e["pos"])

    def test_hotspot_clave_correcta_queda_guardado_con_prioridad(self):
        s, b, _, cfg = nuevo()
        b.controlar("router_apagar", "Farmacia la Fe ALPHANET")
        b.controlar("router_prender", "PRUEBA-LAFE")
        r = s.conectar("PRUEBA-LAFE", "prueba123")
        self.assertTrue(r["ok"], r)
        self.assertTrue(r["pos"])
        guardada = [c for c in b.conexiones() if c["nombre"] == "PRUEBA-LAFE (tienda)"][0]
        self.assertEqual(guardada["prioridad"], 40)
        self.assertTrue(guardada["archivo"].startswith(cfg["conn_dir"]))
        self.assertEqual(registro(cfg)[-1]["resultado"], "ok")

    def test_persistencia_tras_reiniciar(self):
        s, b, _, _ = nuevo()
        b.controlar("router_apagar", "Farmacia la Fe ALPHANET")
        b.controlar("router_prender", "PRUEBA-LAFE")
        s.conectar("PRUEBA-LAFE", "prueba123")
        b.reiniciar()
        self.assertEqual(s.estado()["conectado_a"], "PRUEBA-LAFE (tienda)")

    def test_clave_incorrecta_borra_la_nueva_y_vuelve_a_la_anterior(self):
        s, b, _, _ = nuevo()
        b.controlar("router_prender", "PRUEBA-LAFE")
        r = s.conectar("PRUEBA-LAFE", "equivocada")
        self.assertEqual(r["codigo"], "clave")
        self.assertNotIn("PRUEBA-LAFE (tienda)", [c["nombre"] for c in b.conexiones()])
        self.assertEqual(s.estado()["conectado_a"], "Farmacia la Fe ALPHANET")

    def test_red_sin_internet_se_deshace(self):
        s, b, _, _ = nuevo()
        r = s.conectar("SIN-INTERNET", "sinred123")
        self.assertEqual(r["codigo"], "sin_internet")
        self.assertNotIn("SIN-INTERNET (tienda)", [c["nombre"] for c in b.conexiones()])
        self.assertEqual(s.estado()["conectado_a"], "Farmacia la Fe ALPHANET")

    def test_servidor_caido_no_deshace_una_red_buena(self):
        s, b, _, _ = nuevo()
        b.controlar("router_apagar", "Farmacia la Fe ALPHANET")
        b.controlar("router_prender", "PRUEBA-LAFE")
        b.controlar("servidor", valor=False)
        r = s.conectar("PRUEBA-LAFE", "prueba123")
        self.assertTrue(r["ok"])
        self.assertFalse(r["pos"])
        self.assertIn("servidor no responde", r["mensaje"])
        self.assertEqual(s.estado()["conectado_a"], "PRUEBA-LAFE (tienda)")

    def test_misma_red_clave_nueva(self):
        s, b, _, _ = nuevo()
        b.controlar("router_clave", "Farmacia la Fe ALPHANET", "nuevaclave9")
        self.assertIsNone(s.estado()["conectado_a"])           # la de base tiene la clave vieja
        r = s.conectar("Farmacia la Fe ALPHANET", "nuevaclave9")
        self.assertTrue(r["ok"], r)
        self.assertEqual(s.estado()["conectado_a"], "Farmacia la Fe ALPHANET (tienda)")
        nombres = [c["nombre"] for c in b.conexiones()]
        self.assertIn("Farmacia la Fe ALPHANET", nombres)       # la de base sigue ahí, debajo
        # Otra vez clave nueva: reemplaza la de la tienda, no acumula
        b.controlar("router_clave", "Farmacia la Fe ALPHANET", "otraclave99")
        s.conectar("Farmacia la Fe ALPHANET", "otraclave99")
        self.assertEqual([c["nombre"] for c in b.conexiones()].count("Farmacia la Fe ALPHANET (tienda)"), 1)

    def test_red_que_aparece_en_el_segundo_escaneo(self):
        s, b, _, _ = nuevo()
        b.controlar("router_apagar", "Farmacia la Fe ALPHANET")
        original = b.escanear
        llamadas = []
        def escanear(dev):
            llamadas.append(1)
            if len(llamadas) == 2:             # el router «termina de encender» recién ahora
                b.controlar("router_prender", "PRUEBA-LAFE")
            return original(dev)
        b.escanear = escanear
        self.assertTrue(s.conectar("PRUEBA-LAFE", "prueba123")["ok"])

    def test_red_no_visible(self):
        s, _, _, _ = nuevo()
        self.assertEqual(s.conectar("PRUEBA-LAFE", "prueba123")["codigo"], "no_encontrada")

    def test_formato_de_clave(self):
        s, b, _, _ = nuevo()
        b.controlar("router_prender", "PRUEBA-LAFE")
        self.assertEqual(s.conectar("PRUEBA-LAFE", "corta")["codigo"], "clave_formato")

    def test_red_abierta(self):
        s, b, _, _ = nuevo()
        b.controlar("router_apagar", "Farmacia la Fe ALPHANET")
        self.assertTrue(s.conectar("ABIERTA", "")["ok"])

    def test_sin_adaptador_wifi(self):
        s, b, _, _ = nuevo()
        b.controlar("wifi", valor=False)
        self.assertFalse(s.estado()["wifi"])
        self.assertEqual(s.conectar("PRUEBA-LAFE", "prueba123")["codigo"], "sin_wifi")

    def test_vuelve_sola_cuando_regresa_el_router(self):
        s, b, _, _ = nuevo()
        b.controlar("router_apagar", "Farmacia la Fe ALPHANET")
        self.assertFalse(s.estado()["internet"])
        b.controlar("router_prender", "Farmacia la Fe ALPHANET")   # nadie toca nada
        e = s.estado()
        self.assertTrue(e["internet"] and e["pos"])


class TestProtecciones(unittest.TestCase):
    def test_no_olvida_redes_de_base(self):
        s, b, _, cfg = nuevo()
        base = [c for c in b.conexiones() if c["nombre"] == "Farmacia la Fe ALPHANET"][0]
        r = s.olvidar(base["uuid"])
        self.assertEqual(r["codigo"], "protegida")
        self.assertIn("Farmacia la Fe ALPHANET", [c["nombre"] for c in b.conexiones()])
        self.assertEqual(registro(cfg)[-1]["accion"], "olvidar_rechazado")

    def test_olvida_red_de_la_tienda_y_vuelve_a_la_base(self):
        s, b, _, _ = nuevo()
        b.controlar("router_prender", "PRUEBA-LAFE")
        s.conectar("PRUEBA-LAFE", "prueba123")
        uuid = [c for c in b.conexiones() if c["nombre"] == "PRUEBA-LAFE (tienda)"][0]["uuid"]
        self.assertTrue(s.olvidar(uuid)["ok"])
        self.assertEqual(s.estado()["conectado_a"], "Farmacia la Fe ALPHANET")

    def test_redes_guardadas_solo_lista_las_de_la_tienda(self):
        s, b, _, _ = nuevo()
        self.assertEqual(s.redes()["guardadas"], [])
        b.controlar("router_prender", "PRUEBA-LAFE")
        s.conectar("PRUEBA-LAFE", "prueba123")
        self.assertEqual([g["nombre"] for g in s.redes()["guardadas"]], ["PRUEBA-LAFE (tienda)"])

    def test_validaciones(self):
        self.assertTrue(validar_ssid("Farmacia la Fe ALPHANET"))
        self.assertFalse(validar_ssid("x" * 33))
        self.assertFalse(validar_ssid(""))
        self.assertFalse(validar_ssid(None))
        self.assertTrue(validar_clave("12345678", False))
        self.assertTrue(validar_clave("a" * 64, False))
        self.assertFalse(validar_clave("1234567", False))
        self.assertFalse(validar_clave("ñandúñandú", False))
        self.assertTrue(validar_clave("", True))


class TestPin(unittest.TestCase):
    def test_sin_pin_configurado_entra_directo(self):
        s, _, _, _ = nuevo()
        token, error, _ = s.entrar("")
        self.assertTrue(token and error is None)

    def test_pin_correcto_e_incorrecto(self):
        s, _, _, _ = nuevo(pin="2468")
        self.assertEqual(s.entrar("1111")[1], "pin")
        token, error, _ = s.entrar("2468")
        self.assertTrue(s.sesion_valida(token))

    def test_tres_fallos_bloquean_un_minuto(self):
        s, _, reloj, _ = nuevo(pin="2468")
        for _ in range(2):
            self.assertEqual(s.entrar("0000")[1], "pin")
        self.assertEqual(s.entrar("0000")[1], "bloqueado")
        self.assertEqual(s.entrar("2468")[1], "bloqueado")      # ni con el correcto
        reloj.t += 61
        self.assertIsNone(s.entrar("2468")[1])

    def test_sesion_vence(self):
        s, _, reloj, _ = nuevo(pin="2468")
        token = s.entrar("2468")[0]
        reloj.t += 16 * 60
        self.assertFalse(s.sesion_valida(token))


if __name__ == "__main__":
    unittest.main()
