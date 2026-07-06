# Integración bot WhatsApp (pasteleria-bot) como canal del ERP — referencia

> Proyecto complementario: `~/Sites/pasteleria-bot` (bot de pedidos por WhatsApp
> de Dulce Morena, Node.js + whatsapp-web.js + agente IA). Su
> `PLAN-INTEGRACION-ERP.md` propone integrarlo como **otro canal** del ERP
> adminTools, al estilo de la app de pedidos `at-ordenes-ventas`.

**Análisis completo de ese plan (2026-07-05):**
`~/Sites/pasteleria-bot/REVISION-PLAN-INTEGRACION.md`

## Resumen para este lado (qué le tocaría al ecosistema adminTools)

- **Dirección aprobable**: bot = canal del ERP; el ERP es dueño de catálogo,
  clientes y pedidos; single-tenant por deploy (igual que venecia/Ronal).
- **El contrato OpenAPI del bot se escribió sin conocer la API real** (asume
  Postgres, `/api/v1`, API keys, 11 endpoints nuevos). Hay que reescribirlo
  partiendo de admintools-api.
- **Precedente a seguir**: `at-ordenes-ventas` ya crea pedidos vía
  `POST /orders/save` con JWT de usuario vendedor → el bot debería usar un
  usuario "bot" rol SELLER, no API keys (MVP sin tocar seguridad).
- **Trabajo chico en la API para el MVP**: find-or-create de cliente por
  teléfono. Nada más es imprescindible.
- **Gaps grandes que NO son MVP** (viven en el bot por ahora): personalización
  de productos (tamaño/sabor/relleno con extras — `articulo` no lo modela),
  agenda/capacidad/fecha de entrega, anticipos con comprobante (CxC es
  post-factura; un pago sobre pedido es un concepto nuevo), webhook ERP→bot.
- **El bot no factura**: crea pedidos; la factura sale del POS/Swing (caja, CAI,
  kardex) — así el bot no necesita tenant.
- **Sinergia con el plan de cierre H1**: la US-074 (lock anti-sobreventa en
  `OrderService.save`, Fase 3) protege también a este canal.
