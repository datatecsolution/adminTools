-- =====================================================================
-- V18 — Tabla de saldos materializada (INV-2 estructura + backfill)
--
-- Crea `existencia_articulo_bodega`, el cache transaccional del saldo
-- actual por (artículo, bodega). El kardex (`movimiento_kardex`) sigue
-- siendo el libro mayor inmutable (auditoría); esta tabla es la "última
-- foto" para lecturas rápidas O(1) — patrón ledger + balance.
--
-- Por qué hace falta:
--  - La función `f_can_saldo_kardex(art, bodega)` (que usa la vista
--    `articulo_view`) hace 3 joins + ORDER BY + LIMIT por cada llamada.
--    Funciona bien para búsquedas chicas; lento para listados grandes
--    cuando el kardex crece a diario (compras todos los días).
--  - La vista hardcodea bodega=1; el nuevo subsistema es multi-bodega.
--
-- Esta migración SOLO crea la estructura y la siembra. El mantenimiento
-- transaccional (UPSERT en cada movimiento) lo agrega V19, junto con
-- el fix de concurrencia del kardex (INV-CC). Por eso V18 NO cambia
-- comportamiento — los SPs siguen igual, los reads del API tampoco
-- aún la usan; la tabla queda lista para que V19 la mantenga y para
-- que el API la consuma después.
--
-- Idempotente:
--  - CREATE TABLE IF NOT EXISTS.
--  - Backfill con INSERT ... ON DUPLICATE KEY UPDATE (no-op si ya hay
--    filas → respeta datos vivos si V19 ya está poblando la tabla).
-- =====================================================================

CREATE TABLE IF NOT EXISTS existencia_articulo_bodega (
    codigo_articulo     INT NOT NULL,
    codigo_bodega       INT NOT NULL,
    cantidad            DECIMAL(14,2) NOT NULL DEFAULT 0,
    fecha_actualizacion DATETIME      NOT NULL
                                       DEFAULT CURRENT_TIMESTAMP
                                       ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (codigo_articulo, codigo_bodega),
    KEY idx_bodega (codigo_bodega)
) ENGINE=InnoDB
  COMMENT='Saldo actual por (articulo, bodega). Mantenida transaccionalmente por triggers/SPs del kardex desde V19. Ver docs/inventario-api-design.md §3 D2.';

-- Backfill desde el último saldo tipo 3 del kardex (vía función existente).
-- ON DUPLICATE KEY UPDATE cantidad=cantidad → no-op si la fila ya está
-- (caso re-aplicación tras V19 corriendo en producción: no pisar live data).
INSERT INTO existencia_articulo_bodega (codigo_articulo, codigo_bodega, cantidad)
SELECT
    ak.codigo_articulo,
    ak.codigo_bodega,
    IFNULL(f_can_saldo_kardex(ak.codigo_articulo, ak.codigo_bodega), 0)
FROM articulo_kardex ak
ON DUPLICATE KEY UPDATE cantidad = cantidad;
