"""Repite contra la API de prod (481b4b0) sobre la copia V48 y la copia V57 las rutas
que la app de pedidos de Sharon usa en producción (sacadas de los logs del proxy),
y compara status y contenido."""
import hashlib, json, os, sys, urllib.request, urllib.error, uuid

APIS = {"V48": "http://127.0.0.1:18081/admin_tools/api", "V57": "http://127.0.0.1:18080/admin_tools/api"}
# Vendedor real con una clave de prueba puesta SOLO en las copias locales (nunca en producción).
USER, PASS = os.environ["ENS_APP_USER"], os.environ["ENS_APP_PASS"]
TERMINOS = ["Rexona", "Dove", "Sardina", "Pastilla", "Acet", "Seda", "Vick", "Pal", "dove", "Bota"]
ORDEN_MODELO = int(os.environ.get("ENS_ORDEN_MODELO", "111892"))  # pedido real del mismo vendedor, se clona con un clientRef nuevo


def llamar(base, metodo, ruta, token=None, cuerpo=None):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    req = urllib.request.Request(base + ruta, data=datos, method=metodo)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            txt = r.read().decode()
            return r.status, (json.loads(txt) if txt else None)
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()[:200]


def huella(obj, quitar=()):
    """hash estable del JSON, sin los campos que cambian solos (ids nuevos, horas)."""
    def limpiar(o):
        if isinstance(o, dict):
            return {k: limpiar(v) for k, v in sorted(o.items()) if k not in quitar}
        if isinstance(o, list):
            return [limpiar(x) for x in o]
        return o
    return hashlib.sha256(json.dumps(limpiar(obj), sort_keys=True, default=str).encode()).hexdigest()[:12]


def recorrido(nombre, base):
    res = []
    st, login = llamar(base, "POST", "/auth/login", cuerpo={"username": USER, "password": PASS})
    res.append(("POST auth/login", st, login.get("role") if isinstance(login, dict) else login))
    token = login["token"]
    st, ref = llamar(base, "POST", "/auth/refresh", token=token)
    res.append(("POST auth/refresh", st, "token" if isinstance(ref, dict) and "token" in ref else ref))
    for t in TERMINOS:
        st, b = llamar(base, "GET", "/products/description/" + t, token=token)
        res.append((f"GET products/description/{t}", st, f"{len(b) if isinstance(b, list) else '-'} items {huella(b)}"))
    st, b = llamar(base, "GET", "/customers", token=token)
    res.append(("GET customers", st, f"{len(b) if isinstance(b, list) else '-'} items {huella(b)}"))
    st, hoy = llamar(base, "GET", "/orders/today", token=token)
    n_hoy = len(hoy) if isinstance(hoy, list) else hoy
    res.append(("GET orders/today", st, f"{n_hoy} pedidos {huella(hoy)}"))

    st, modelo = llamar(base, "GET", f"/orders/{ORDEN_MODELO}", token=token)
    res.append((f"GET orders/{ORDEN_MODELO} (modelo)", st, huella(modelo)))
    nuevo = dict(modelo)
    for k in ("orderId", "date", "time", "dateTime"):
        nuevo.pop(k, None)
    # como la app: líneas nuevas, sin detailId ni orderId del pedido original
    nuevo["details"] = [{k: v for k, v in d.items() if k not in ("detailId", "orderId")} for d in modelo["details"]]
    nuevo["clientRef"] = "ensayo-v57-" + nombre + "-" + uuid.uuid4().hex[:8]
    st, guardado = llamar(base, "POST", "/orders/save", token=token, cuerpo=nuevo)
    oid = guardado.get("orderId") if isinstance(guardado, dict) else None
    res.append(("POST orders/save", st, f"total={guardado.get('total') if isinstance(guardado, dict) else guardado} "
                f"lineas={len(guardado.get('details') or []) if isinstance(guardado, dict) else '-'} "
                f"{huella(guardado, quitar=('orderId', 'detailId', 'date', 'time', 'dateTime'))}"))
    st, hoy2 = llamar(base, "GET", "/orders/today", token=token)
    res.append(("GET orders/today (después)", st, f"{len(hoy2) if isinstance(hoy2, list) else hoy2} pedidos"))
    if oid:
        st, b = llamar(base, "DELETE", f"/orders/delete/{oid}", token=token)
        res.append(("DELETE orders/delete/{n}", st, "ok" if st < 300 else b))
    st, hoy3 = llamar(base, "GET", "/orders/today", token=token)
    res.append(("GET orders/today (tras borrar)", st, f"{len(hoy3) if isinstance(hoy3, list) else hoy3} pedidos"))
    return res


r48 = recorrido("V48", APIS["V48"])
r57 = recorrido("V57", APIS["V57"])
iguales = 0
print(f"{'paso':38} {'V48':34} {'V57':34} igual")
for (paso, s48, d48), (_, s57, d57) in zip(r48, r57):
    ok = s48 == s57 and d48 == d57
    iguales += ok
    print(f"{paso:38} {str(s48)+' '+str(d48):34.34} {str(s57)+' '+str(d57):34.34} {'✔' if ok else '✘'}")
print(f"\n{iguales}/{len(r48)} pasos idénticos")
sys.exit(0 if iguales == len(r48) else 1)
