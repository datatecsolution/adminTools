# Informe de UX/UI - Modulo de Facturacion

**Fecha:** 2026-04-17  
**Clases analizadas:** `ViewModuloFacturar.java` (JFrame contenedor) + `ViewFacturarFrame.java` (JInternalFrame)  
**Propósito:** Pantalla principal de facturación del POS

---

## 1. Arquitectura de la vista

El módulo de facturación se compone de **dos clases** que trabajan juntas:

### ViewModuloFacturar (JFrame - ventana principal)
- Ocupa toda la pantalla (`setSize(getToolkit().getScreenSize())`)
- Contiene un `JDesktopPane` donde se insertan las ventanas internas
- **Barra de menú (JMenuBar)** con botones:
  - `btnAgregar` — Agrega nueva ventana de facturación (máximo 1)
  - `btnDescuento` — Dispara F7 (descuento) en la ventana activa
  - `btnPrecio` — Dispara F8 (modificar precio) en la ventana activa
  - `btnCantidad` — Dispara F9 (modificar cantidad) en la ventana activa
  - `btnClientes` — Abre ventana de cobros
  - `btnProveedores` — Abre ventana de pago a proveedores
  - `btnSalidas` — Abre ventana de salidas de caja
  - `btnCaja` — Muestra la caja activa (solo lectura)
  - `btnUsuario` — Muestra el usuario logueado (solo lectura)

### ViewFacturarFrame (JInternalFrame - ventana interna)
- Se crea con tamaño 800x600 pero se **maximiza inmediatamente** dentro del JDesktopPane
- Solo se permite **1 instancia** a la vez
- Contiene toda la UI de facturación (datos cliente, tabla detalle, totales, acciones)

### Layout completo del módulo

```
┌─ ViewModuloFacturar (JFrame - pantalla completa) ────────────────────┐
│ [+Agregar][Descuento][Precio][Cantidad][Cobros][Proveed][Salidas]    │
│                              [Caja: Caja 1][User: admin][Fecha: ..] │
├──────────────────────────────────────────────────────────────────────┤
│ ┌─ ViewFacturarFrame (JInternalFrame - maximizado) ────────────────┐ │
│ │ NORTH: Logo | Id Cliente | Nombre | RTN | Contado/Créd           │ │
│ │             | Buscar: [________________________]                 │ │
│ ├──────────┬──────────────────────────────────────────┬────────────┤ │
│ │ WEST     │ CENTER                                   │ EAST       │ │
│ │ F1 Buscar│                                          │ Pendientes │ │
│ │ F2 Cobrar│   Tabla de detalle (7 columnas)          │ (160px)    │ │
│ │ Clientes │                                          │            │ │
│ │ Guardar  │                                          │            │ │
│ │ Cotizar  │                                          │            │ │
│ │ Cotizac. │                                          │            │ │
│ │ F6 Cierre│                                          │            │ │
│ │ Actualiz.│                                          │            │ │
│ │ Esc Cerr │                                          │            │ │
│ ├──────────┴──────────────────────────────────────────┴────────────┤ │
│ │ SOUTH: Subtotal | Descuento | Imp 6% | Imp 10% | TOTAL         │ │
│ └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 2. Problemas detectados y propuestas de mejora

### 2.1 CRITICO - Panel de totales con gaps negativos [CORREGIDO]

**Problema:** `GridLayout(2, 10, -20, -20)` produce solapamiento visual entre componentes. Además, declara 10 columnas pero solo usa 5, dejando 5 celdas vacías.

**Solucion aplicada:** Cambiado a `GridLayout(2, 5, 5, 2)` con gaps positivos.

**Estado:** Corregido.

---

### 2.2 ALTO - Campo de busqueda mal ubicado

**Problema:** El campo de búsqueda está en el panel norte (zona de datos del cliente), lejos de la tabla donde aparecen los resultados. El usuario busca arriba pero mira abajo.

**Propuesta:**
- Mover el campo de búsqueda justo encima de la tabla de detalle (entre el panelNorte y el scrollPane).
- Hacerlo más prominente: mayor altura, placeholder text "Buscar artículo por nombre o código...".

**Solucion aplicada:** Campo de búsqueda movido a un panelCentral (BorderLayout.NORTH) justo encima del scrollPane de la tabla. Layout cambiado de GridLayout a BorderLayout con label "Buscar:" a la izquierda y campo expandible al centro.

**Estado:** Corregido.

---

### 2.3 ALTO - RadioButtons de Contado/Credito poco visibles [NO APLICA]

**Problema original:** Los RadioButtons son círculos sin texto propio.

**Analisis:** El diseño sigue el patrón del GridLayout(0, 6): fila de labels arriba, fila de componentes abajo. Todos los campos siguen esta convención (Fecha/campo, Id Cliente/campo, etc.). Agregar texto a los RadioButtons rompería la consistencia visual.

**Estado:** Descartado. El diseño actual es intencional y consistente.

---

### 2.4 ALTO - Atajos de teclado inconsistentes [PARCIALMENTE CORREGIDO]

**Problema:** Solo 3 de 9 botones del panel lateral mostraban su atajo. Además, los atajos F7/F8/F9 existen como botones en la barra del JFrame padre (ViewModuloFacturar) pero no son visibles para el usuario que solo mira la ventana interna.

**Solucion aplicada en ViewFacturarFrame:**
- `btnBuscar` → "F1 Buscar"
- `btnGuardar` → "Ctrl+G Guardar"
- `btnActualizar` → "Ctrl+A Actualizar"

**Propuesta pendiente para ViewModuloFacturar:**
- Los botones de la barra de menú (`btnDescuento`, `btnPrecio`, `btnCantidad`) simulan teclas F7/F8/F9 mediante `KeyEvent` sintético. Esto es un antipatrón. Sería mejor que estos botones llamen directamente a los métodos del controlador (`aplicarDescuento()`, `modificarPrecio()`, `modificarCantidad()`).
- Agregar tooltips a los botones de la barra: "Aplicar descuento (F7)", "Modificar precio (F8)", "Modificar cantidad (F9)".

**Estado:** Parcialmente corregido.

---

### 2.5 ALTO - Duplicacion de funcionalidad entre ViewModuloFacturar y ViewFacturarFrame

**Problema:** Hay funciones duplicadas entre la barra de menú del JFrame y el panel de acciones del JInternalFrame:
- **Cobros**: `btnClientes` en la barra (abre ViewCobro) vs tecla F10 en el controlador (hace lo mismo).
- **Proveedores**: `btnProveedores` en la barra vs F11 en el controlador.
- **Salidas de caja**: `btnSalidas` en la barra vs F12 en el controlador.

El usuario tiene dos formas de llegar a lo mismo, pero ninguna se lo indica.

**Solucion aplicada:** Código duplicado extraído a métodos públicos en CtlFacturarFrame (`abrirCobros()`, `abrirPagoProveedor()`, `abrirSalidaCaja()`). Los botones de la barra y los atajos F10/F11/F12 ahora llaman al mismo método. También se hicieron públicos `aplicarDescuento()`, `modificarPrecio()`, `modificarCantidad()` y se eliminaron los KeyEvent sintéticos (resuelve 2.13). Código muerto comentado eliminado (resuelve 2.14).

**Estado:** Corregido.

---

### 2.6 MEDIO - Informacion de estado parcialmente resuelta

**Problema original:** No había feedback de caja, usuario ni estado de factura.

**Realidad:** ViewModuloFacturar ya muestra caja y usuario en la barra de menú (`btnCaja`, `btnUsuario`). Lo que falta es:
- Indicador de si la factura actual es **nueva** o se está **editando** una pendiente.
- Indicador de conexión a base de datos (hoy solo se detecta al cobrar).

**Solucion aplicada:** Método `setEstadoFactura(boolean, int)` en ViewFacturarFrame. Factura nueva muestra "Datos Generales" con fondo normal (`#d4f4ff`). Al editar una orden pendiente muestra "Editando Orden #N" con fondo verde claro (`214, 245, 224`) acorde a la paleta. Se llama en 4 puntos del controlador: `setEmptyView()`, `cargarFacturaPendiente()`, `buscarOrden()`, `actualizarFactura()`.

**Estado:** Corregido.

---

### 2.7 MEDIO - Falta de tooltips [CORREGIDO]

**Solucion aplicada:** Se agregaron tooltips a los 13 componentes principales:
- Campos: txtFechafactura, txtIdcliente, txtNombrecliente, txtRtn, txtBuscar
- Botones: btnBuscar, btnCobrar, btnCliente, btnGuardar, btnGuardarCotizacion, btnGetCotizacion, btnCierreCaja, btnActualizar, btnCerrar
- Tabla: tableDetalle

**Estado:** Corregido.

---

### 2.8 MEDIO - Contraste insuficiente en campo de busqueda [CORREGIDO]

**Problema:** `txtBuscar` usaba fondo verde claro (`Color(60, 179, 113)`) con texto blanco. Contraste ~2.4:1, por debajo del mínimo WCAG AA de 4.5:1.

**Solucion aplicada:** Fondo cambiado a verde más oscuro `Color(30, 120, 70)` para mejorar contraste.

**Estado:** Corregido.

---

### 2.9 MEDIO - Columna de descripcion demasiado ancha

**Problema:** La columna 0 de la tabla tiene 420px fijos. Al estar maximizada la ventana en un monitor grande, la distribución de columnas se desbalancea.

**Solucion aplicada:** Agregado `AUTO_RESIZE_ALL_COLUMNS`. Proporciones redistribuidas: Artículo 400, Precio 120, Cantidad 100, SubTotal 100, Impuesto 100, Descuento 100, Total 180.

**Estado:** Corregido.

---

### 2.10 MEDIO - Panel de facturas pendientes con titulo [CORREGIDO]

**Problema:** `scrollPane2.setPreferredSize(new Dimension(160, 0))` hacía el panel invisible. Sin título.

**Solucion aplicada:**
- Altura inicial cambiada a 100px.
- Agregado `TitledBorder` con texto "Pendientes".

**Estado:** Corregido.

---

### 2.11 BAJO - Codigo muerto en la vista [CORREGIDO]

**Solucion aplicada:** Eliminados:
- `color2` (Color cian nunca usado)
- `Font "OCR A Extended"` (nunca asignada)
- `cbxEmpleados` + getter (nunca inicializado en esta clase)
- ~30 líneas de comentarios muertos

**Estado:** Corregido.

---

### 2.12 BAJO - Bug de scope en panelNorte [CORREGIDO]

**Problema:** Variable local `JPanel panelNorte` ocultaba el campo de clase `protected JPanel panelNorte`.

**Solucion aplicada:** Cambiado a `panelNorte = new JPanel()` (asigna al campo de clase).

**Estado:** Corregido.

---

### 2.13 BAJO - Antipatron de KeyEvent sintetico en ViewModuloFacturar

**Problema:** Los botones de la barra de menú crean `KeyEvent` sintéticos para simular teclas F7/F8/F9:
```java
KeyEvent key = new KeyEvent(btnDescuento, KeyEvent.KEY_PRESSED, 
    System.currentTimeMillis(), 0, KeyEvent.VK_F7);
activo.getCtl().keyPressed(key);
```
Esto acopla la barra de menú al mecanismo de teclado del controlador. Si alguien cambia el manejo de F7 en `keyPressed`, rompe el botón.

**Propuesta:**
- Llamar directamente al método: `activo.getCtl().aplicarDescuento()`.
- Lo mismo para F8 → `modificarPrecio()` y F9 → `modificarCantidad()`.

**Solucion aplicada:** Resuelto como parte de 2.5. Botones de la barra llaman directamente a `aplicarDescuento()`, `modificarPrecio()`, `modificarCantidad()`.

**Estado:** Corregido.

---

### 2.14 BAJO - Codigo muerto en ViewModuloFacturar [CORREGIDO]

**Problema:** Bloque de código comentado en el ActionListener de `btnClientes` (líneas 94-102).

**Solucion aplicada:** Resuelto como parte de 2.5. Código comentado eliminado, ActionListeners simplificados a lambdas.

**Estado:** Corregido.

---

### 2.15 MEDIO - Fecha ubicada en panel interno [CORREGIDO]

**Problema:** El campo de fecha estaba en el panelDatosFactura del JInternalFrame junto a los campos editables (Id Cliente, Nombre, RTN). La fecha es información de solo lectura del sistema, no un dato de la factura que el cajero modifique.

**Solucion aplicada:**
- Fecha movida a la barra de menú de ViewModuloFacturar como `btnFecha` (junto a Caja y Usuario).
- `txtFechafactura` se mantiene oculto en ViewFacturarFrame por compatibilidad con el controlador.
- `CtlFacturarFrame.setEmptyView()` actualiza ambos: el campo oculto y el botón visible en la barra.
- Eliminado `KeyListener` de `txtFechafactura` ya que el campo es invisible.
- GridLayout de panelDatosFactura ajustado de `(0,6)` a `(0,5)`.

**Estado:** Corregido.

---

### 2.16 DEUDA TECNICA - Arquitectura JDesktopPane/JInternalFrame

**Contexto:** El diseño original contemplaba múltiples ventanas de facturación abiertas simultáneamente dentro del JDesktopPane. El enfoque final cambió a guardar facturas temporalmente como órdenes en el panel lateral y usar una sola ventana.

**Situacion actual:**
- ViewModuloFacturar usa JDesktopPane pero limita a 1 JInternalFrame.
- ViewFacturarFrame hereda de JInternalFrame pero se maximiza inmediatamente.
- La indirección JFrame → JDesktopPane → JInternalFrame no aporta valor con una sola ventana.

**Riesgo de cambio:** Alto. Eliminar JInternalFrame requiere migrar ViewFacturarFrame a JPanel, ajustar CtlFacturarFrame (referencias a `getTopLevelAncestor()`, `isClosed()`, etc.), y probar toda la interacción factura-barra. No se recomienda sin cobertura de pruebas.

**Estado:** Documentado como deuda técnica. No se planea corrección a corto plazo.

---

## 3. Resumen de estado

### Ya corregidos
| # | Cambio | Clase |
|---|--------|-------|
| 2.1 | GridLayout totales: `(2,10,-20,-20)` → `(2,5,5,2)` | ViewFacturarFrame |
| 2.2 | Campo búsqueda reubicado junto a tabla | ViewFacturarFrame |
| 2.3 | RadioButtons: descartado (diseño intencional) | ViewFacturarFrame |
| 2.4 | Atajos visibles: F1, Ctrl+G, Ctrl+A | ViewFacturarFrame |
| 2.5 | Funciones unificadas: llamadas directas a métodos públicos del controlador | Ambas |
| 2.6 | Indicador de estado nueva/editando orden con color diferenciado | ViewFacturarFrame + CtlFacturarFrame |
| 2.7 | Tooltips en 13 componentes | ViewFacturarFrame |
| 2.8 | Contraste de búsqueda mejorado | ViewFacturarFrame |
| 2.9 | Columnas de tabla proporcionales con AUTO_RESIZE_ALL_COLUMNS | ViewFacturarFrame |
| 2.10 | Panel pendientes con título y altura | ViewFacturarFrame |
| 2.11 | Código muerto eliminado | ViewFacturarFrame |
| 2.12 | Bug scope panelNorte corregido | ViewFacturarFrame |
| 2.13 | KeyEvent sintético eliminado (resuelto con 2.5) | ViewModuloFacturar |
| 2.14 | Código muerto limpiado (resuelto con 2.5) | ViewModuloFacturar |
| 2.15 | Fecha movida a barra de menú | Ambas + CtlFacturarFrame |
| — | Barra título InternalFrame eliminada | ViewFacturarFrame |
| — | Logo reducido, separador visual, gap sutil | ViewFacturarFrame |
| — | Altura uniforme botones texto en barra de menú | ViewModuloFacturar + BotonesApp |
| — | Foco automático en txtBuscar al abrir ventana | ViewModuloFacturar |

### Pendientes
| # | Cambio | Estado |
|---|--------|--------|
| 2.16 | Deuda técnica JDesktopPane/JInternalFrame | Documentado, sin acción planificada |

---

## 4. Notas tecnicas

- Todas las propuestas son compatibles con Java Swing y no requieren librerías externas.
- La mayoría de cambios no afectaron el controlador. Excepción: la migración de fecha (2.15) requirió modificar `CtlFacturarFrame.setEmptyView()` para actualizar `btnFecha` en la barra de menú.
- La ventana interna ya se maximiza al crear (en ViewModuloFacturar línea 177), por lo que `setSize(800, 600)` solo define el tamaño mínimo antes de maximizar.
- ViewModuloFacturar limita a 1 ventana de facturación simultánea (línea 165).
