# Cómo cobrar por el uso del POS — análisis

**Fecha:** 2026-08-02 · **Estado:** análisis para decisión del negocio ·
**Relacionado:** `estrategia-actualizacion-swing.md` §3.2 (licencia en el
canal), `analisis-futuro-swing.md` (el POS como producto central)

## 1. Punto de partida

La pieza técnica de control ya está diseñada: el canal de actualización lleva
un bloque `licencia { cliente, valida_hasta }` firmado, con la regla de oro
de que la falta de pago corta **actualizaciones y soporte, nunca la
operación**. Lo que falta es el circuito comercial: qué se cobra, por dónde
entra el dinero y cómo se concilia con la licencia.

Este documento NO fija precios — eso es decisión del negocio. Fija la
estructura para que cualquier precio funcione.

## 2. Qué cobrar (el modelo)

| Modelo | Cómo funciona | Evaluación |
|---|---|---|
| **Por caja activa (recomendado)** | tarifa base mensual por cliente + monto por caja registrada | Es auditable directo de la BD del cliente (tabla `cajas`), escala con el tamaño real del negocio (Sharon 6 cajas ≠ miscelánea 1 caja), y es fácil de explicar |
| Por terminal | por cada PC/dispositivo | Difícil de auditar (el POS corre en cualquier navegador) |
| Plano por cliente | una tarifa única | Simple pero injusto en ambas puntas: caro para el chico, regalado para el grande |
| Por módulo | base + app de pedidos + reportería… | Buen **add-on futuro** (la app de pedidos por vendedor activo es medible en `usuarios_precios`/órdenes), pero como modelo inicial complica la venta |

Sugerencia de estructura: **base + por caja, mensual, con anual prepagado a
descuento**. El dato de cajas y vendedores activos ya está en la base de cada
cliente — la auditoría del cobro es un SELECT, no una visita.

## 3. Por dónde entra el dinero (verificado para Honduras, 2026-08)

**Stripe NO está disponible para comercios en Honduras** — descartado de
entrada (la vía LLC en EE. UU. es overkill para esta escala). Las opciones
reales:

| Método | Rol en el circuito |
|---|---|
| **Transferencia bancaria local** | El estándar B2B hondureño. Cero comisión, todos los clientes la tienen. Contra: conciliación manual. **Es la Fase 1.** |
| **Efectivo / cobrador** | Los clientes YA operan con rutas de cobro — el mismo circuito sirve para cobrarles a ellos. Válido como respaldo. |
| **PixelPay** (fintech hondureña, SPS) | Links de pago con tarjeta, API para automatizar. Candidata natural para la Fase 3 por ser local (soporte, retiro en lempiras). |
| **Tilopay** (Centroamérica) | Similar; sin mensualidad, opera en toda la región — útil si algún día hay clientes fuera de Honduras. |
| Pagadito / Clinpays / gateways bancarios (BAC, Ficohsa) | Alternativas a cotizar en la misma ronda que PixelPay/Tilopay. |
| PayPal | Funciona pero comisiones altas y retiro engorroso; no para recurrente B2B local. |

## 4. El circuito completo (cobro ↔ licencia)

```
factura CAI de Datatec → pago del cliente → conciliación
        → publicar-licencia.sh <cliente> <nueva valida_hasta>
        → el canal firma y publica → las terminales renuevan solas
```

- `licencia.valida_hasta` **es** la fecha de fin del período pagado. Renovar
  la licencia es EL acto administrativo posterior a conciliar el pago.
- Aviso no bloqueante en las terminales N días antes del vencimiento
  ("su licencia vence el …") — el recordatorio de cobro se hace solo.
- Vencida: sin actualizaciones ni soporte; la operación del cliente sigue
  intacta (regla de oro — un POS que no factura por licencia es un daño
  comercial autoinfligido).

## 5. Dogfooding: cobrar con el propio sistema

Datatec ya vende software de facturación con CAI, CxC, estados de cuenta,
recibos y cartera. **La facturación del servicio debe emitirse desde una
empresa "Datatec" en el propio admin_tools**: factura CAI legal al cliente,
el pago se registra como abono en CxC, el estado de cuenta del cliente es el
respaldo de la conciliación. Costo cero de herramientas nuevas, y de paso el
mejor argumento de venta: *nosotros cobramos con esto*.

## 6. Fases

**F1 — Manual (puede empezar este mes):** contrato simple de licencia de uso
y soporte + factura CAI mensual/anual + transferencia + registro en el CxC
propio + renovar `valida_hasta` a mano al conciliar. Sin dependencias
técnicas nuevas: funciona incluso ANTES de que exista el canal (la licencia
se vuelve enforcement cuando el canal entre; mientras tanto es contractual).

**F2 — Semiautomática:** recordatorios de vencimiento por correo/WhatsApp
(la infraestructura del bot de la pastelería sirve de base), script de
conciliación que al marcar el pago regenera y firma el manifiesto del canal.

**F3 — Pasarela:** link de pago (PixelPay o Tilopay, a cotizar ambas) en el
recordatorio; el webhook de pago confirmado dispara la renovación de licencia
sin intervención. Recurrencia con tarjeta para el que la quiera.

## 7. Riesgos y cómo mitigarlos

- **Clientes acostumbrados a no pagar recurrente.** Transición con
  grandfathering: el precio nuevo aplica a clientes nuevos; los existentes
  entran con período de gracia y el argumento de valor es concreto — POS
  nuevo, app de pedidos, actualizaciones automáticas y soporte (todo lo
  construido este año).
- **Cobrar sin contrato.** El contrato de licencia+soporte es prerequisito
  de F1: una página, qué incluye, qué pasa al no renovar (nunca apagado).
- **Facturación fiscal propia.** Emitir CAI como Datatec exige la empresa
  dada de alta con su rango fiscal — el sistema propio ya lo maneja.
- **No mezclar**: el precio y sus montos NO van en documentos del repo (los
  clientes podrían verlos); acá solo la estructura.

## 8. Primeros pasos (cuando el negocio decida)

1. Definir tarifa base + por caja (decisión del usuario, fuera del repo).
2. Redactar el contrato de una página de licencia + soporte.
3. Dar de alta la empresa Datatec en admin_tools con su CAI para facturar.
4. Arrancar F1 con UN cliente piloto (el próximo cliente nuevo, p. ej. Wyc —
   entra ya con el esquema; sin grandfathering que negociar).
5. Cotizar PixelPay y Tilopay en paralelo (comisiones reales, plazos de
   retiro) para tener F3 lista cuando el volumen lo amerite.
