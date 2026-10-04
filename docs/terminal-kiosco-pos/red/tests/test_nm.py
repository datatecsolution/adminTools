"""Parseo de nmcli y backend real con un `ejecutar` falso (sin NetworkManager)."""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from nm import (ErrorRed, NmcliBackend, clasificar_error, dividir_terse,  # noqa: E402
                parsear_conexiones, parsear_dispositivos, parsear_redes)

DISPOSITIVOS = (
    "wlx00e04c123456:wifi:connected:Farmacia la Fe ALPHANET\n"
    "enp1s0:ethernet:unavailable:\n"
    "lo:loopback:connected (externally):lo\n"
)
REDES = (
    "*:Farmacia la Fe ALPHANET:78:WPA1 WPA2\n"
    " :Farmacia la Fe ALPHANET:41:WPA1 WPA2\n"      # otro punto de acceso, misma red
    " :Red\\:con dos puntos:55:WPA2\n"               # ':' escapado dentro del SSID
    " ::30:WPA2\n"                                   # red oculta: se omite
    " :Invitados:20:\n"                              # abierta
    " :Mayorga 5G:64:WPA2 WPA3\n"
)
CONEXIONES = (
    "Farmacia la Fe ALPHANET:aaaaaaaa-0000-4000-8000-000000000001:802-11-wireless:wlx00e04c123456:"
    "/usr/lib/NetworkManager/system-connections/Farmacia la Fe ALPHANET.nmconnection:30\n"
    "PRUEBA-LAFE (tienda):aaaaaaaa-0000-4000-8000-000000000002:802-11-wireless::"
    "/home/pos-red/system-connections/PRUEBA-LAFE (tienda).nmconnection:40\n"
    "cable:aaaaaaaa-0000-4000-8000-000000000003:802-3-ethernet::"
    "/usr/lib/NetworkManager/system-connections/cable.nmconnection:50\n"
)


class TestParseo(unittest.TestCase):
    def test_dividir_terse_con_escapes(self):
        self.assertEqual(dividir_terse("a\\:b:c\\\\d:"), ["a:b", "c\\d", ""])

    def test_dispositivos(self):
        d = parsear_dispositivos(DISPOSITIVOS)
        self.assertEqual(d[0], {"dispositivo": "wlx00e04c123456", "tipo": "wifi", "estado": "connected",
                                "conexion": "Farmacia la Fe ALPHANET"})
        self.assertEqual(len(d), 3)

    def test_redes_une_repetidas_omite_ocultas_y_ordena(self):
        r = parsear_redes(REDES)
        self.assertEqual([x["ssid"] for x in r],
                         ["Farmacia la Fe ALPHANET", "Mayorga 5G", "Red:con dos puntos", "Invitados"])
        self.assertTrue(r[0]["conectada"])
        self.assertEqual(r[0]["senal"], 78)
        self.assertTrue(r[3]["abierta"])
        self.assertFalse(r[1]["abierta"])

    def test_conexiones(self):
        c = parsear_conexiones(CONEXIONES)
        self.assertEqual(c[1]["nombre"], "PRUEBA-LAFE (tienda)")
        self.assertEqual(c[1]["prioridad"], 40)
        self.assertTrue(c[1]["archivo"].startswith("/home/pos-red/"))
        self.assertEqual(c[0]["dispositivo"], "wlx00e04c123456")

    def test_clasificar_error(self):
        self.assertEqual(clasificar_error("Error: Connection activation failed: Secrets were required, but not provided."), "clave")
        self.assertEqual(clasificar_error("Error: No network with SSID 'X' found."), "no_encontrada")
        self.assertEqual(clasificar_error("Error: 802-11-wireless-security.psk: property is invalid."), "clave_formato")
        self.assertEqual(clasificar_error("Error: Timeout expired (30 seconds)"), "tiempo")
        self.assertEqual(clasificar_error("algo raro"), "fallo")


class EjecutarFalso:
    """Responde a cada comando según el primer patrón que coincida."""

    def __init__(self, respuestas):
        self.respuestas = respuestas
        self.llamadas = []

    def __call__(self, args, timeout=45):
        self.llamadas.append(args)
        texto = " ".join(args)
        for patron, resp in self.respuestas:
            if patron in texto:
                return resp
        return (0, "", "")


class TestBackendReal(unittest.TestCase):
    def test_dispositivo_wifi(self):
        b = NmcliBackend(EjecutarFalso([("device", (0, DISPOSITIVOS, ""))]))
        self.assertEqual(b.dispositivo_wifi()["dispositivo"], "wlx00e04c123456")

    def test_sin_wifi(self):
        b = NmcliBackend(EjecutarFalso([("device", (0, "enp1s0:ethernet:connected:cable\n", ""))]))
        self.assertIsNone(b.dispositivo_wifi())

    def test_conectar_usa_lista_de_argumentos_y_devuelve_uuid(self):
        ej = EjecutarFalso([("wifi connect", (0, "Device 'wlx0' successfully activated with "
                                                 "'bbbbbbbb-0000-4000-8000-000000000009'.\n", ""))])
        b = NmcliBackend(ej)
        uuid = b.conectar("wlx0", "Mi red; rm -rf /", "clave123", "Mi red (tienda)")
        self.assertEqual(uuid, "bbbbbbbb-0000-4000-8000-000000000009")
        args = ej.llamadas[0]
        self.assertIn("Mi red; rm -rf /", args)          # el SSID va como UN argumento, sin shell
        self.assertEqual(args[args.index("name") + 1], "Mi red (tienda)")

    def test_conectar_red_abierta_sin_password(self):
        ej = EjecutarFalso([("wifi connect", (0, "activated with 'cccccccc-0000-4000-8000-000000000001'", ""))])
        NmcliBackend(ej).conectar("wlx0", "Invitados", "", "Invitados (tienda)")
        self.assertNotIn("password", ej.llamadas[0])

    def test_conectar_clave_mala_devuelve_uuid_para_borrar(self):
        ej = EjecutarFalso([
            ("wifi connect", (4, "", "Error: Connection activation failed: Secrets were required, but not provided.")),
            ("connection show", (0, "X (tienda):dddddddd-0000-4000-8000-000000000001:802-11-wireless::"
                                    "/home/pos-red/system-connections/X (tienda).nmconnection:0\n", "")),
        ])
        with self.assertRaises(ErrorRed) as cm:
            NmcliBackend(ej).conectar("wlx0", "X", "malamala", "X (tienda)")
        self.assertEqual(cm.exception.codigo, "clave")
        self.assertEqual(cm.exception.detalle, "dddddddd-0000-4000-8000-000000000001")

    def test_escanear_reintenta_sin_rescan(self):
        ej = EjecutarFalso([("--rescan yes", (1, "", "Error: Scanning not allowed")),
                            ("--rescan no", (0, REDES, ""))])
        redes = NmcliBackend(ej).escanear("wlx0")
        self.assertEqual(len(redes), 4)


if __name__ == "__main__":
    unittest.main()
