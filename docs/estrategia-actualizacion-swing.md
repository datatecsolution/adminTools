# Estrategia de actualización automática del Swing

**Fecha:** 2026-08-01 · **Estado:** propuesta, pendiente de GO para fase 1

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

**Regla de la estrategia: el canal es POR CLIENTE, y publicar una versión en
él es el ÚLTIMO paso del deploy** — después de backup, migraciones y API,
exactamente donde el playbook pone hoy "distribuir el jar". La actualización
automática no reemplaza la ventana de deploy: reemplaza únicamente el ir
terminal por terminal.

## 3. Arquitectura

Tres piezas, ninguna nueva en el ecosistema:

```
┌─ Servidor del cliente (ya existe: docker + nginx) ─────────────┐
│  /updates/                                                     │
│    version.json   { "version": "2026.08.01-1",                 │
│                     "jar": "AdminTools-2026.08.01-1.jar",      │
│                     "sha256": "…", "notas": "US-127, US-128" } │
│    AdminTools-2026.08.01-1.jar                                 │
└────────────────────────────────────────────────────────────────┘
        ▲ publicar-jar.sh <cliente>              │ HTTP(S)/LAN
        │ (desde la Mac, por SSH;                ▼
        │  ultimo paso del deploy)      ┌─ Terminal ────────────────┐
┌─ Operador ──────────────┐             │  lanzador.jar  (~150 loc) │
│  construye + checksum + │             │  jre/                     │
│  sube + regenera json   │             │  AdminTools.jar           │
└─────────────────────────┘             │  AdminTools-prev.jar      │
                                        │  updates.properties (URL) │
                                        └───────────────────────────┘
```

**Flujo en la terminal, en cada arranque:**

1. El lanzador lee la URL de su `updates.properties`.
2. `GET version.json` con timeout de 3 s. **Sin internet o sin respuesta →
   arranca la versión local sin avisar nada.** La actualización jamás puede
   dejar a un cajero sin facturar.
3. Si `version` > local: descarga a `.tmp`, verifica el SHA-256, renombra
   atómicamente, guarda el jar anterior como `AdminTools-prev.jar`.
4. Lanza el AdminTools real como proceso hijo y termina.

Por cliente: Sharon puede servir por el dominio público con HTTPS (cubre
vendedores fuera del local); venecia y dulce por LAN
(`http://192.168.88.251/updates/`). Servir el directorio es un contenedor
nginx mínimo con un volumen, o una ruta más en el proxy existente.

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
- HTTPS donde exista dominio (Sharon); en LAN, HTTP + checksum es aceptable
  (el atacante que puede alterar el tráfico LAN ya está dentro de la red del
  negocio). Fase 2: firma RSA del manifiesto si se quiere elevar.
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
4. Directorio `/updates/` en el servidor de UN cliente piloto (dulce, que es
   de pruebas) y luego Sharon.
5. Playbook: "distribuir jar" pasa a ser "publicar en /updates/".

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
