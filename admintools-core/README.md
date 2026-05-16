# admintools-core

Módulo Gradle con lógica pura (cálculos financieros, validaciones de
dominio) reutilizable entre:

- El Swing legacy en `/` (root `AdminTools`) — consume vía
  `compile project(':admintools-core')` ya configurado.
- El backend Spring Boot `admintools` (en `/Users/jdmayorga/Desktop/admintools`)
  — consume vía Maven dependency contra `mavenLocal()` o un registro
  remoto.

## Diseño

- Cero dependencias Swing, JPA, Spring, JDBC.
- Inputs/outputs son tipos primitivos o `BigDecimal` — nada de
  entidades application-specific.
- Métodos estáticos donde aplica, para reuso sin DI.

## Coordenadas Maven

```
net.datatecsolution:admintools-core:0.1.0-SNAPSHOT
```

Versionado semántico. `0.x.y` indica API inestable mientras el módulo
crece.

## Build

```bash
# Desde el root del proyecto:
./gradlew :admintools-core:build       # compila + tests
./gradlew :admintools-core:test        # solo tests
./gradlew :admintools-core:publishToMavenLocal   # publica a ~/.m2
```

## Cómo importarlo desde el backend `admintools` (API)

En `admintools/build.gradle` agregar:

```groovy
repositories {
    mavenLocal()         // antes de mavenCentral
    mavenCentral()
}

dependencies {
    implementation 'net.datatecsolution:admintools-core:0.1.0-SNAPSHOT'
}
```

Luego refrescar el wrapper de Gradle/Maven en IntelliJ y ya se pueden
importar las clases bajo `net.datatecsolution.admintools.core.*`.

## Contenido actual

| Clase | Propósito |
|---|---|
| `FacturacionCalculadora.calcularDescuentoPorcentaje` | Monto de descuento porcentual sobre una línea, HALF_EVEN scale 0. Bit-idéntico al cálculo histórico de `CtlFacturarFrame.aplicarDescuentoPorcentaje`. |

Más métodos se irán extrayendo a medida que la API los necesite (ver
`docs/plan-desacoplar-facturacion.md` Fase 4.2+).
