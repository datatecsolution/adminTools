-- US-150: idempotencia en la creación de órdenes (app de pedidos).
--
-- Bug de producción (Sharon, jun-sep 2026): POST /orders/save no es
-- idempotente; cuando la respuesta se pierde en la red móvil ("Load failed"
-- en iOS) el vendedor re-guarda y nace una orden duplicada, que caja factura
-- dos veces (6 dobles cobros activos detectados el 2026-09-04).
--
-- La app enviará un client_ref (UUID generado en el teléfono, conservado
-- entre reintentos); el API devuelve la orden existente si el ref se repite.
-- Columna NULL: las filas históricas y las órdenes del Swing/POS (que no
-- mandan ref) no se ven afectadas. El UNIQUE cierra la carrera de dos POST
-- simultáneos con el mismo ref (NULL no participa del unique en MySQL).
ALTER TABLE encabezado_factura_temp
    ADD COLUMN client_ref VARCHAR(36) NULL,
    ADD UNIQUE KEY uk_encabezado_temp_client_ref (client_ref);
