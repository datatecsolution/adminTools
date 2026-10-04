"""Imita el nginx de la app de pedidos: estáticos del build, SPA fallback,
index sin caché y /admin_tools/api → API local (18080). Registra cada llamada."""
import http.server, os, sys, urllib.request, urllib.error, datetime

BUILD = sys.argv[1]
API = "http://127.0.0.1:18080"
LOG = open(os.path.join(os.path.dirname(BUILD), "..", "llamadas_app.log"), "a", buffering=1)

class H(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *a, **k):
        super().__init__(*a, directory=BUILD, **k)

    def log_message(self, fmt, *args):
        pass

    def _registrar(self, status):
        LOG.write(f"{datetime.datetime.now():%H:%M:%S} {status} {self.command} {self.path}\n")

    def _proxy(self):
        n = int(self.headers.get("Content-Length") or 0)
        body = self.rfile.read(n) if n else None
        req = urllib.request.Request(API + self.path, data=body, method=self.command)
        for h in ("Authorization", "Content-Type", "Accept"):
            if self.headers.get(h):
                req.add_header(h, self.headers[h])
        try:
            r = urllib.request.urlopen(req, timeout=60)
            status, headers, data = r.status, r.headers, r.read()
        except urllib.error.HTTPError as e:
            status, headers, data = e.code, e.headers, e.read()
        self.send_response(status)
        for k, v in headers.items():
            if k.lower() not in ("transfer-encoding", "connection", "content-length"):
                self.send_header(k, v)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)
        self._registrar(status)

    def do_GET(self):
        if self.path.startswith("/admin_tools/api"):
            return self._proxy()
        ruta = self.path.split("?")[0]
        if not os.path.isfile(os.path.join(BUILD, ruta.lstrip("/"))):
            self.path = "/index.html"
        if self.path.split("?")[0] in ("/", "/index.html"):
            self.path = "/index.html"
            f = open(os.path.join(BUILD, "index.html"), "rb").read()
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
            self.send_header("Content-Length", str(len(f)))
            self.end_headers()
            self.wfile.write(f)
            return
        return super().do_GET()

    do_POST = do_PUT = do_DELETE = _proxy

http.server.ThreadingHTTPServer(("127.0.0.1", 3100), H).serve_forever()
