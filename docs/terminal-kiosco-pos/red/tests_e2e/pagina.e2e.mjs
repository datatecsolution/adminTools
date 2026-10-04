// Pruebas de punta a punta de la página de wifi (etapa B del plan).
//
// Levanta, por escenario, un pos-red SIMULADO limpio (python3 pos_red.py --simulado),
// un «POS» falso y un Chrome/Chromium headless SIN extensiones (perfil vacío, como el
// kiosco). Lo maneja por el protocolo DevTools con teclas y clics reales (keydown +
// keypress + keyup, como un teclado físico) y guarda una captura por escenario.
//
//   node tests_e2e/pagina.e2e.mjs [carpeta-de-capturas]
//   CHROME=/ruta/al/chromium node tests_e2e/pagina.e2e.mjs      (en Linux / la VM)
//
// Sin dependencias: Node ≥ 22 (WebSocket y fetch globales).
import { spawn } from "node:child_process";
import { mkdtempSync, writeFileSync, mkdirSync, existsSync } from "node:fs";
import { createServer } from "node:http";
import { tmpdir } from "node:os";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const AQUI = dirname(fileURLToPath(import.meta.url));
const RED = dirname(AQUI);
const SALIDA = process.argv[2] || join(tmpdir(), "pos-red-e2e");
mkdirSync(SALIDA, { recursive: true });
const PUERTO = 8090, PUERTO_POS = 8099, PUERTO_CDP = 9333;
const CHROME = process.env.CHROME || ["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
  "/usr/bin/chromium", "/usr/bin/chromium-browser", "/usr/bin/google-chrome"].find((p) => existsSync(p));
const espera = (ms) => new Promise((r) => setTimeout(r, ms));

// --- PIN de prueba (2468) con el mismo hash que pos_red.py ------------------------
async function configTemporal(extra = {}) {
  const dir = mkdtempSync(join(tmpdir(), "pos-red-"));
  const hash = await new Promise((res, rej) => {
    const p = spawn("python3", ["-c", "import sys;sys.path.insert(0,sys.argv[1]);from pos_red import hash_pin;print(hash_pin('2468','ab'*16))", RED]);
    let o = ""; p.stdout.on("data", (d) => (o += d)); p.on("close", (c) => (c ? rej(new Error("hash")) : res(o.trim())));
  });
  const cfg = Object.assign({ pos_url: `http://127.0.0.1:${PUERTO_POS}/`, registro: join(dir, "cambios.log"),
    pin_sal: "ab".repeat(16), pin_hash: hash, teclado: "auto" }, extra);
  const ruta = join(dir, "config.json");
  writeFileSync(ruta, JSON.stringify(cfg));
  return ruta;
}

async function arrancarPosRed(extra) {
  const p = spawn("python3", [join(RED, "pos_red.py"), "--simulado", "--config", await configTemporal(extra), "--puerto", String(PUERTO)],
    { stdio: "ignore" });
  for (let i = 0; i < 50; i++) {
    try { const r = await fetch(`http://127.0.0.1:${PUERTO}/api/estado`); if (r.ok) return p; } catch (e) { /* aún no */ }
    await espera(100);
  }
  throw new Error("pos-red no arrancó");
}

async function sim(accion, ssid, valor) {
  const r = await fetch(`http://127.0.0.1:${PUERTO}/api/_sim`, { method: "POST",
    headers: { "Content-Type": "application/json", Origin: `http://127.0.0.1:${PUERTO}` },
    body: JSON.stringify({ accion, ssid, valor }) });
  if (!r.ok) throw new Error("sim " + accion + " → " + r.status);
}

// --- Chrome por el protocolo DevTools ------------------------------------------------
class Pagina {
  static async abrir() {
    const dir = mkdtempSync(join(tmpdir(), "chrome-e2e-"));
    const proc = spawn(CHROME, ["--headless=new", `--remote-debugging-port=${PUERTO_CDP}`, `--user-data-dir=${dir}`,
      "--no-first-run", "--no-default-browser-check", "--disable-extensions", "--window-size=1280,800",
      "--password-store=basic", "about:blank"], { stdio: "ignore" });
    let objetivo;
    for (let i = 0; i < 100 && !objetivo; i++) {
      try { objetivo = (await (await fetch(`http://127.0.0.1:${PUERTO_CDP}/json/list`)).json()).find((t) => t.type === "page"); }
      catch (e) { await espera(100); }
    }
    const p = new Pagina(proc, new WebSocket(objetivo.webSocketDebuggerUrl));
    await new Promise((r) => p.ws.addEventListener("open", r));
    await p.cdp("Page.enable"); await p.cdp("Runtime.enable");
    return p;
  }
  constructor(proc, ws) {
    this.proc = proc; this.ws = ws; this.id = 0; this.pend = new Map(); this.eventos = [];
    ws.addEventListener("message", (m) => {
      const d = JSON.parse(m.data);
      if (d.id && this.pend.has(d.id)) { const { res, rej } = this.pend.get(d.id); this.pend.delete(d.id); d.error ? rej(new Error(d.error.message)) : res(d.result); }
      else if (d.method) this.eventos.push(d);
    });
  }
  cdp(method, params = {}) {
    const id = ++this.id;
    this.ws.send(JSON.stringify({ id, method, params }));
    return new Promise((res, rej) => this.pend.set(id, { res, rej }));
  }
  async ir(url) { await this.cdp("Page.navigate", { url }); await espera(900); }
  async eval(expr) {
    const r = await this.cdp("Runtime.evaluate", { expression: expr, returnByValue: true, awaitPromise: true });
    if (r.exceptionDetails) throw new Error("eval: " + r.exceptionDetails.text);
    return r.result.value;
  }
  async esperarQue(expr, ms = 15000) {
    const fin = Date.now() + ms;
    while (Date.now() < fin) { if (await this.eval(expr).catch(() => false)) return true; await espera(150); }
    throw new Error("no se cumplió: " + expr);
  }
  async tecla(key) {
    const t = { Enter: [13, "\r"], Escape: [27, ""], ArrowDown: [40, ""], ArrowUp: [38, ""], Tab: [9, ""], Backspace: [8, ""] }[key];
    const base = { key, code: key, windowsVirtualKeyCode: t[0], nativeVirtualKeyCode: t[0] };
    await this.cdp("Input.dispatchKeyEvent", Object.assign({ type: t[1] ? "keyDown" : "rawKeyDown", text: t[1] || undefined }, base));
    await this.cdp("Input.dispatchKeyEvent", Object.assign({ type: "keyUp" }, base));
    await espera(120);
  }
  async escribir(texto) {
    for (const c of texto) {
      await this.cdp("Input.dispatchKeyEvent", { type: "keyDown", key: c, text: c, unmodifiedText: c });
      await this.cdp("Input.dispatchKeyEvent", { type: "keyUp", key: c });
    }
    await espera(80);
  }
  async clic(selectorJs) {
    const c = await this.eval(`(() => { const e = ${selectorJs}; if (!e) return null; e.scrollIntoView({block:'center'}); const r = e.getBoundingClientRect(); return {x: r.x + r.width/2, y: r.y + r.height/2}; })()`);
    if (!c) throw new Error("no encontré " + selectorJs);
    for (const type of ["mousePressed", "mouseReleased"]) {
      await this.cdp("Input.dispatchMouseEvent", { type, x: c.x, y: c.y, button: "left", clickCount: 1 });
    }
    await espera(150);
  }
  async captura(nombre) {
    const r = await this.cdp("Page.captureScreenshot", { format: "png" });
    writeFileSync(join(SALIDA, nombre + ".png"), Buffer.from(r.data, "base64"));
  }
  cerrar() { this.proc.kill(); }
}

// --- atajos de lectura -----------------------------------------------------------------
const H2 = "document.querySelector('#vista h2')?.textContent || ''";
const AVISO = "[...document.querySelectorAll('.aviso')].map(a => a.textContent).join(' | ')";
const FOCO = "document.activeElement?.getAttribute('aria-label') || document.activeElement?.tagName";
const URL_PAG = (q = "") => `http://127.0.0.1:${PUERTO}/?volver=${encodeURIComponent(`http://127.0.0.1:${PUERTO_POS}/`)}${q}`;
const boton = (texto) => `[...document.querySelectorAll('button')].find(b => b.textContent.trim() === ${JSON.stringify(texto)})`;
const red = (ssid) => `[...document.querySelectorAll('.lista button.red')].find(b => b.querySelector('.nombre')?.textContent === ${JSON.stringify(ssid)})`;

async function entrarConPin(p) {
  await p.esperarQue(`${H2} === 'PIN de la tienda'`);
  await p.escribir("2468"); await p.tecla("Enter");
  await p.esperarQue(`${H2} === 'Redes disponibles'`);
}
function ok(cond, msg) { if (!cond) throw new Error(msg); }

// --- escenarios --------------------------------------------------------------------------
const ESCENARIOS = {
  async "01 PIN incorrecto y correcto, sin teclado en pantalla"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET");
    await p.ir(URL_PAG());
    await p.esperarQue(`${H2} === 'PIN de la tienda'`);
    ok(!(await p.eval("!!document.querySelector('.osk')")), "no debería haber teclado en pantalla con teclado físico");
    await p.escribir("1111"); await p.tecla("Enter");
    await p.esperarQue(`(${AVISO}).includes('PIN incorrecto')`);
    await p.captura("01a-pin-incorrecto");
    await p.escribir("2468"); await p.tecla("Enter");
    await p.esperarQue(`${H2} === 'Redes disponibles'`);
    await p.captura("01b-lista");
  },
  async "02 hotspot: flechas, clave mala, clave buena y vuelta al POS"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET"); await sim("router_prender", "PRUEBA-LAFE");
    await p.ir(URL_PAG()); await entrarConPin(p);
    await p.tecla("ArrowDown"); await p.tecla("ArrowUp");
    ok((await p.eval(FOCO)).startsWith("PRUEBA-LAFE"), "las flechas deberían volver a PRUEBA-LAFE");
    await p.tecla("Enter");
    await p.esperarQue(`${H2} === 'Clave de PRUEBA-LAFE'`);
    await p.escribir("equivocada"); await p.tecla("Enter");
    await p.esperarQue(`(${AVISO}).includes('La clave no es correcta')`);
    await p.captura("02a-clave-incorrecta");
    ok((await p.eval(FOCO)) === "Clave de PRUEBA-LAFE", "el foco debería volver al campo de clave");
    await p.escribir("prueba123"); await p.tecla("Enter");
    await p.esperarQue(`(${AVISO}).includes('Conectado a PRUEBA-LAFE')`);
    await p.captura("02b-conectado");
    await p.esperarQue(`location.port === '${PUERTO_POS}'`, 8000);
  },
  async "03 red sin internet"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET");
    await p.ir(URL_PAG()); await entrarConPin(p);
    await p.clic(red("SIN-INTERNET"));
    await p.esperarQue(`${H2} === 'Clave de SIN-INTERNET'`);
    await p.escribir("sinred123"); await p.tecla("Enter");
    await p.esperarQue(`(${AVISO}).includes('no tiene salida a internet')`, 20000);
    await p.captura("03-sin-internet");
  },
  async "04 servidor caído: la red se conserva"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET"); await sim("servidor", null, false);
    await p.ir(URL_PAG()); await entrarConPin(p);
    await p.clic(red("ABIERTA"));
    await p.esperarQue(`(${AVISO}).includes('el servidor no responde')`);
    await p.captura("04-servidor-caido");
    ok((await p.eval("location.port")) === String(PUERTO), "no debería volver al POS con el servidor caído");
  },
  async "05 la red desaparece mientras se escribe la clave"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET"); await sim("router_prender", "PRUEBA-LAFE");
    await p.ir(URL_PAG()); await entrarConPin(p);
    await p.tecla("Enter"); await p.esperarQue(`${H2} === 'Clave de PRUEBA-LAFE'`);
    await sim("router_apagar", "PRUEBA-LAFE");
    await p.escribir("prueba123"); await p.tecla("Enter");
    await p.esperarQue(`(${AVISO}).includes('No se encontró la red')`);
    await p.captura("05-no-encontrada");
  },
  async "06 el router vuelve solo y la página regresa al POS"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET");
    await p.ir(URL_PAG()); await entrarConPin(p);
    await sim("router_prender", "Farmacia la Fe ALPHANET");
    await p.esperarQue(`(${AVISO}).includes('La red volvió')`, 9000);
    await p.captura("06-la-red-volvio");
    await p.esperarQue(`location.port === '${PUERTO_POS}'`, 8000);
  },
  async "07 olvidar una red de la tienda"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET"); await sim("router_prender", "PRUEBA-LAFE"); await sim("servidor", null, false);
    await p.ir(URL_PAG()); await entrarConPin(p);
    await p.tecla("Enter"); await p.esperarQue(`${H2} === 'Clave de PRUEBA-LAFE'`);
    await p.escribir("prueba123"); await p.tecla("Enter");
    await p.esperarQue(`(${AVISO}).includes('el servidor no responde')`);
    await p.clic(boton("Ver redes"));
    // el nombre tiene que VERSE en la fila de guardadas (no basta con que esté en la página)
    await p.esperarQue(`[...document.querySelectorAll('li.guardada .nombre')].some(n => n.textContent === 'PRUEBA-LAFE (tienda)' && n.getBoundingClientRect().width > 50)`);
    await p.captura("07a-guardadas");
    await p.clic(boton("Olvidar")); await p.esperarQue(`(${AVISO}).includes('¿Olvidar la red')`);
    await p.clic(boton("Olvidar"));
    await p.esperarQue(`(${AVISO}).includes('Red olvidada')`);
    ok(!(await p.eval(`[...document.querySelectorAll('li.guardada .nombre')].some(n => n.textContent === 'PRUEBA-LAFE (tienda)')`)), "la red debería desaparecer de las guardadas");
    await p.captura("07b-olvidada");
  },
  async "08 reiniciar la caja (sin PIN)"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET");
    await p.ir(URL_PAG());
    await p.esperarQue(`${H2} === 'PIN de la tienda'`);
    await p.clic(boton("Reiniciar la caja"));
    await p.esperarQue(`(${AVISO}).includes('¿Reiniciar la caja?')`);
    await p.clic(boton("Reiniciar"));
    await p.esperarQue(`document.body.textContent.includes('Reiniciando la caja')`);
    await p.captura("08-reiniciando");
  },
  async "09 caja sin placa wifi"(p) {
    await sim("wifi", null, false);
    await p.ir(URL_PAG());
    await p.esperarQue(`(${AVISO}).includes('no tiene red inalámbrica')`);
    await p.captura("09-sin-wifi");
  },
  async "10 tres PIN incorrectos bloquean"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET");
    await p.ir(URL_PAG());
    for (let i = 0; i < 3; i++) {
      await p.esperarQue(`${H2} === 'PIN de la tienda' && !document.querySelector('input').disabled`);
      await p.escribir("0000"); await p.tecla("Enter"); await espera(400);
    }
    await p.esperarQue(`(${AVISO}).includes('Demasiados intentos')`);
    await p.captura("10-pin-bloqueado");
  },
  async "11 nombre de red malicioso se muestra como texto"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET");
    await sim("router_agregar", `<img src=x onerror="document.title='XSS'">`, "vecino123");
    await p.ir(URL_PAG()); await entrarConPin(p);
    ok((await p.eval("document.querySelectorAll('#vista img').length")) === 0, "no debería inyectarse ninguna imagen");
    ok((await p.eval("document.title")) === "Red de esta caja", "el título no debería cambiar");
    ok(await p.eval(`document.body.textContent.includes('<img src=x')`), "el nombre debería verse como texto");
    await p.captura("11-nombre-malicioso");
  },
  async "12 caja táctil: teclado en pantalla para PIN y clave"(p) {
    await sim("router_apagar", "Farmacia la Fe ALPHANET"); await sim("router_prender", "PRUEBA-LAFE");
    await p.ir(URL_PAG());
    await p.esperarQue(`${H2} === 'PIN de la tienda' && !!document.querySelector('.osk')`);
    for (const d of "2468") await p.clic(`[...document.querySelectorAll('.osk button')].find(b => b.textContent === '${d}')`);
    await p.captura("12a-pin-en-pantalla");
    await p.clic(`[...document.querySelectorAll('.osk button')].find(b => b.textContent === 'OK')`);
    await p.esperarQue(`${H2} === 'Redes disponibles'`);
    await p.clic(red("PRUEBA-LAFE"));
    await p.esperarQue(`${H2} === 'Clave de PRUEBA-LAFE' && !!document.querySelector('.osk')`);
    for (const c of "prueba123") await p.clic(`[...document.querySelectorAll('.osk button')].find(b => b.textContent === '${c}')`);
    await p.captura("12b-clave-en-pantalla");
    await p.clic(`[...document.querySelectorAll('.osk button')].find(b => b.textContent === 'Conectar')`);
    await p.esperarQue(`(${AVISO}).includes('Conectado a PRUEBA-LAFE')`);
  },
};
const CONFIG_ESCENARIO = { "12 caja táctil: teclado en pantalla para PIN y clave": { teclado: "pantalla" } };

// --- ejecución -------------------------------------------------------------------------------
const pos = createServer((req, res) => { res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" }); res.end("<!doctype html><title>POS simulado</title><h1>POS</h1>"); });
await new Promise((r) => pos.listen(PUERTO_POS, "127.0.0.1", r));
const p = await Pagina.abrir();
const resultados = [];
const filtro = process.env.SOLO;
for (const [nombre, fn] of Object.entries(ESCENARIOS)) {
  if (filtro && !nombre.startsWith(filtro)) continue;
  const srv = await arrancarPosRed(CONFIG_ESCENARIO[nombre]);
  const t0 = Date.now();
  try { await fn(p); resultados.push([nombre, "OK", ""]); }
  catch (e) { resultados.push([nombre, "FALLA", e.message]); await p.captura("FALLA-" + nombre.slice(0, 2)).catch(() => {}); }
  finally { srv.kill(); await espera(300); }
  console.log(`${resultados.at(-1)[1] === "OK" ? "✔" : "✘"} ${nombre} (${((Date.now() - t0) / 1000).toFixed(1)} s)${resultados.at(-1)[2] ? " — " + resultados.at(-1)[2] : ""}`);
}
p.cerrar(); pos.close();
const fallas = resultados.filter((r) => r[1] !== "OK").length;
console.log(`\n${resultados.length - fallas}/${resultados.length} escenarios OK · capturas en ${SALIDA}`);
process.exit(fallas ? 1 : 0);
