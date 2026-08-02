# Estrategia de actualización automática del Swing

**Fecha:** 2026-08-01 · **Revisión:** 2026-08-02 (canal centralizado, decisión de hosting) · **Estado:** diseño aprobado, implementación pendiente de GO

## 1. El problema

Hoy el jar se distribuye a mano: se construye, se copia por SSH/USB a cada
terminal y alguien lo reemplaza. Consecuencias reales, todas vividas:

- Terminales corriendo builds viejos sin que nadie lo sepa (cliente B facturaba
  con app V26 contra BD V27; el descuadre del hotfix NFE salió de ahí).
- "Distribuir el jar" queda como pendiente eterno de cada deploy (Sharon lo
  arrastra desde el 30-jul).
- No hay forma de saber qué versión corre una terminal sin ir a verla.

## 2. La restricción que manda el diseño

**El Swing aplica las migraciones Flyway en cada arranque.** Actualizar el jar
de una terminal actualiza el ESQUEMA de la base del cliente. Esto invalida el
diseño obvio (un canal global "última versión para todos"):

> La primera terminal de un cliente que bajara un jar nuevo migraría la base
> de ese cliente por su cuenta, con la API vieja corriendo contra ella — el
> fallo E3 del ensayo de Sharon (validate muere contra el esquema migrado),
> pero disparado por una terminal cualquiera, sin ventana, sin backup y sin
> nadie mirando. La deriva V42 (2026-07-31) fue una versión en miniatura del
> mismo mecanismo.

**Regla de la estrategia (INVARIANTE, sobrevive a cualquier hosting): la
VERSIÓN es POR CLIENTE, y publicar en el canal de un cliente es el ÚLTIMO
paso del deploy DE ESE cliente** — después de backup, migraciones y API,
exactamente donde el playbook pone hoy "distribuir el jar". La actualización
automática no reemplaza la ventana de deploy: reemplaza únicamente el ir
terminal por terminal.

## 3. Arquitectura — un solo punto en internet, un canal por cliente

Revisión 2026-08-02 (decisión del usuario): en vez de hospedar `/updates/` en
el servidor de cada cliente, hay UN punto único en internet. Lo que antes
estaba pegado se separa: el HOSTING es centralizado; la VERSIÓN sigue siendo
por cliente. Publicar para venecia no toca a Sharon.

```
┌─ updates.datatecsolution.com (punto unico) ────────────────────┐
│  sharon/   version.json + AdminTools-2026.08.01-1.jar          │
│  venecia/  version.json + jar                                  │
│  dulce/    version.json + jar                                  │
│    version.json = { "version", "jar", "sha256", "notas" }      │
└────────────────────────────────────────────────────────────────┘
        ▲ publicar-jar.sh <cliente>              │ HTTPS
        │ (desde la Mac; ultimo paso             ▼
        │  del deploy DE ESE cliente)   ┌─ Terminal ────────────────┐
┌─ Operador ──────────────┐             │  lanzador.jar  (~150 loc) │
│  construye + checksum + │             │  jre/                     │
│  sube al canal del      │             │  AdminTools.jar           │
│  cliente + regenera SU  │             │  AdminTools-prev.jar      │
│  version.json           │             │  updates.properties (URL  │
└─────────────────────────┘             │   de SU canal)            │
                                        └───────────────────────────┘
```

### 3.1 Hosting elegido: servidor de Ronal bajo dominio datatec

Evaluado 2026-08-02; opciones: Cloudflare R2, servidor de Ronal, VPS propio,
GitHub Releases. **Decisión del usuario: servidor de Ronal** — cero
infraestructura nueva: un contenedor nginx estático más (volumen propiedad de
`ronal`, solo lectura) detrás del nginx-proxy-manager existente, con
`updates.datatecsolution.com` apuntando a la IP pública de Ronal y TLS por el
mismo mecanismo que ya emite los certificados de posdulce.

Trade-offs aceptados y sus mitigaciones:

- **El servidor de UN cliente distribuye a TODOS.** Si Ronal está caído, no
  salen actualizaciones — pero ninguna terminal se rompe: el lanzador arranca
  la versión local ante cualquier fallo del canal (§6). Las actualizaciones
  se retrasan, no se pierden.
- **Mezcla rol de prod de un cliente con infraestructura propia.** Aislado a
  un contenedor estático de solo lectura; no comparte volumen ni red interna
  con el stack del cliente más allá del proxy.
- **Migrable sin tocar terminales**: las terminales conocen solo la URL del
  dominio. Mover el hosting (a R2, a un VPS) el día de mañana es cambiar el
  DNS — ninguna terminal se reinstala.

### 3.2 Opt-in por cliente y autorización de uso (revisión 2026-08-02)

El canal por cliente modela ambas cosas sin piezas nuevas:

**Opt-in al deploy automático.** Es por cliente y por contrato, no global:

- Cliente que lo desea → sus terminales llevan el lanzador y su canal activo.
- Cliente que no → no se le instala el lanzador (sigue con jar manual), o su
  `version.json` lleva `"pausado": true` y el lanzador no actualiza aunque
  haya versión nueva. Pausar un canal no toca a los demás.

**Autorización de uso.** El `version.json` de cada canal incorpora un bloque
de licencia, y TODO el manifiesto va firmado (la firma de fase 1.5 pasa a
cubrir también la autorización):

```json
{ "version": "2026.08.10-1", "jar": "…", "sha256": "…",
  "licencia": { "cliente": "sharon", "valida_hasta": "2026-12-31" },
  "firma": "…" }
```

El lanzador verifica, ANTES de actualizar: firma válida (clave pública
embebida) → el `cliente` coincide con su canal → la fecha está vigente. Si
algo falla, **no actualiza** — y arranca la versión local con normalidad.
Como la clave privada vive solo en la Mac del operador, un cliente no puede
editarse el `version.json` para extenderse la licencia ni cambiarse de canal,
y ni siquiera root en el servidor que hospeda el punto único puede hacerlo.

**Regla de oro de la autorización: condiciona ACTUALIZACIONES, nunca el
ARRANQUE.** Un POS que deja de facturar por una licencia vencida o un canal
caído es un incidente en el negocio del cliente. El límite duro de este
diseño: sin autorización no hay versiones nuevas (y a lo sumo un aviso no
bloqueante "contacte a Datatec para renovar soporte"); la aplicación
instalada sigue funcionando siempre. Si algún día el negocio exigiera algo
más fuerte, se diseña aparte, con períodos de gracia y avisos — nunca
apagado remoto.

Efecto lateral valioso: el punto único se vuelve el registro operativo de
qué versión tiene autorizada cada cliente y hasta cuándo — la base para
formalizar contratos de soporte.

**Flujo en la terminal, en cada arranque:**

1. El lanzador lee la URL de SU canal (`updates.properties`, p. ej.
   `https://updates.datatecsolution.com/sharon/`).
2. `GET version.json` con timeout de 3 s. **Sin internet o sin respuesta →
   arranca la versión local sin avisar nada.** La actualización jamás puede
   dejar a un cajero sin facturar.
3. Si `version` > local: descarga a `.tmp`, verifica el SHA-256, renombra
   atómicamente, guarda el jar anterior como `AdminTools-prev.jar`.
4. Lanza el AdminTools real como proceso hijo y termina.

Todas las terminales — Sharon, venecia, dulce — consultan el mismo dominio
por HTTPS; solo cambia el segmento del canal. Requisito nuevo respecto al
diseño original: las terminales necesitan salida a internet (las de venecia
la tienen). Si algún cliente quedara sin internet en el local, el lanzador
degrada con gracia (arranca local) y ese cliente puede volver al espejo LAN
del diseño original — la URL del canal es configurable por terminal.

## 4. Alternativas evaluadas

| Opción | Veredicto | Por qué |
|---|---|---|
| **Lanzador propio** (~150 líneas Java 8, cero deps) | **Recomendada** | Legible entero, sin formatos ajenos, encaja con el paquete de escritorio (jre + bat) y con el estilo del repo |
| **Getdown** (Three Rings) | Viable, segunda opción | Java 7+, probado en serio (Deutsche Börse lo usa para la GUI de trading T7). Trae verificación de digests y patching de recursos. En contra: formato de config propio (`getdown.txt`), más piezas que aprender para lo que acá es un caso simple |
| update4j | Descartada | Exige Java 9+ y está construida sobre el sistema de módulos; nuestro runtime verificado es 8 |
| GitHub Releases como canal | Descartada | Repo privado → token en cada terminal (riesgo); repo público expone los jars del negocio; y es un canal global, que es justo lo que la restricción §2 prohíbe |
| Auto-update dentro de la app | Descartada | En Windows un jar no puede reemplazarse a sí mismo (lock del archivo); termina necesitando lanzador igual — mejor hacerlo explícito |

Si el lanzador propio creciera en pretensiones (parches diferenciales,
múltiples recursos, UI de progreso), migrar a Getdown es el camino natural —
el layout servidor (`/updates/` + manifiesto) es conceptualmente el mismo.

## 5. Versionado (prerrequisito)

`build.gradle` dice `version '1.0'` desde siempre; los jars se distinguen por
sufijos manuales (`_sharon_8fd9dc5`). Para comparar versiones hace falta un
esquema real:

- **`AAAA.MM.DD-n`** (fecha de build + contador del día), estampado por Gradle
  en el manifest (`Implementation-Version`) y en el nombre del archivo.
- Comparación lexicográfica directa — sin parseos frágiles.
- El login muestra la versión (hoy es imposible saber qué corre una terminal).

## 6. Seguridad y modos de fallo

- **SHA-256 obligatorio** antes de reemplazar: un download truncado o
  corrupto se descarta y se arranca la versión local.
- **HTTPS siempre** (el canal es público en internet; no hay caso LAN-HTTP en
  el diseño centralizado).
- **La firma del manifiesto sube de prioridad** con el punto único: un solo
  lugar comprometido alcanzaría a TODOS los clientes, y además vive en el
  servidor de un cliente. Fase 1.5 (antes de sumar al segundo cliente): firmar
  `version.json` con una clave RSA que vive SOLO en la Mac; el lanzador lleva
  la pública embebida y no actualiza nada sin firma válida. Con eso, ni el
  acceso root al servidor de Ronal permite empujar un jar a las terminales.
- Descarga a `.tmp` + rename atómico: nunca hay un jar a medias en el nombre
  activo.
- `AdminTools-prev.jar` siempre presente: volver atrás es renombrar.
- El lanzador nunca actualiza si no puede verificar; en la duda, arranca lo
  que hay.

## 7. Fases

**Fase 1 — el circuito completo mínimo:**
1. Versionado real en Gradle (§5).
2. `lanzador.jar` (Java 8 puro, sin dependencias, testeable la lógica de
   comparación/checksum sin red).
3. `publicar-jar.sh <cliente>`: construye, calcula checksum, sube por SSH,
   regenera `version.json`. Con guarda: pide confirmación mostrando la
   versión de esquema que lleva el jar vs la del cliente.
4. Punto único en el servidor de Ronal: contenedor nginx estático de solo
   lectura con `sharon/ venecia/ dulce/`, proxy host en NPM para
   `updates.datatecsolution.com` + DNS + TLS. Piloto con el canal `dulce/`
   (es de pruebas) y luego `sharon/`.
5. Playbook: "distribuir jar" pasa a ser "publicar en /updates/".

**Fase 1.5 — firma + autorización (antes de sumar al segundo cliente):**
- Firma RSA del manifiesto (clave privada solo en la Mac; pública embebida
  en el lanzador).
- Bloque `licencia` en el manifiesto (§3.2): opt-in por cliente, vigencia,
  y la regla de oro — condiciona actualizaciones, nunca el arranque.

**Fase 2 — endurecimiento:**
- Rollback automático: si el jar nuevo no llega al login en N segundos, el
  lanzador restaura `prev` y marca la versión como mala (no la reintenta).
- Publicación canaria: `version.json` con lista de terminales piloto; el
  resto recibe la versión al promoverla. Es el patrón "primero UNA terminal"
  del playbook, automatizado.
- Firma del manifiesto.

## 8. Qué NO resuelve esto

- No reemplaza la ventana de deploy ni su orden (backup → migraciones → API →
  publicar). Lo automatizado es el último kilómetro, no la orquestación.
- No actualiza el JRE empaquetado ni el lanzador mismo (cambian una vez cada
  años; se llevan con el paquete de instalación).
- No sirve para el POS/React ni la API — esos ya se actualizan por docker.
