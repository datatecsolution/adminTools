#!/usr/bin/env python3
"""POS falso del laboratorio en 10.99.0.1:8080 (espacio de red «router»).

/healthz responde 200 con CORS, igual que el nginx del POS real, para que la página
de espera de la caja (esperando.html, que corre desde file://) lo pueda sondear.
"""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PAGINA = ("<!doctype html><meta charset=utf-8><title>POS (laboratorio)</title>"
          "<body style='margin:0;height:100vh;display:grid;place-items:center;background:#064e3b;"
          "color:#fff;font:32px system-ui'><div>✔ Punto de venta (laboratorio)</div>").encode()


class H(BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

    def do_GET(self):
        cuerpo = b"ok" if self.path.startswith("/healthz") else PAGINA
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Type", "text/plain" if cuerpo == b"ok" else "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(cuerpo)))
        self.end_headers()
        self.wfile.write(cuerpo)


ThreadingHTTPServer(("10.99.0.1", 8080), H).serve_forever()
