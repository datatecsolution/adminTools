-- =====================================================================
-- V36 (common) — US-072: float → DECIMAL(15,2) en precio de artículo
--
-- Fase 1 del cierre Hito 1. Retype de las columnas de precio "legacy" del
-- master de producto. El precio real de venta ya vive en `precios_articulos`
-- (multi-precio, Sprint 4.5) que nació DECIMAL(38,2); esta columna es el
-- fallback histórico. Se migra para dejar CERO float monetario en el esquema.
--
-- SOLO retype (ALTER ... MODIFY), sin renombrar ni dropear → Swing viejo
-- sigue arrancando (JDBC setDouble/getDouble contra DECIMAL es transparente).
-- Preserva NOT NULL y defaults exactos. Idempotente. Precisión 15,2
-- (auditoría local: MAX precio_articulo = 14.000,00; sobra de largo).
--
-- Alcance (tablas base con float de precio):
--   articulo            (precio_articulo)  -- double(10,2) → DECIMAL(15,2)
--   precios_programados (nuevo_precio)     -- float        → DECIMAL(15,2)
--
-- Las vistas articulo_view / v_articulos / v_articulo_codigo_barra heredan
-- el tipo de la base y quedan DECIMAL automáticamente (no se tocan).
--
-- API (espejo): AÑADIR @JdbcTypeCode(SqlTypes.DECIMAL) a los campos Double
--   `Articulo.precioVenta` (vista articulo_view) y
--   `ArticuloMaster.precioArticulo` (tabla articulo). Aquí NO se quita REAL
--   (eran Double sin anotación → mapeaban DOUBLE); hay que AGREGAR DECIMAL.
--   `precios_programados` no está mapeada como entidad (solo la usa el Swing).
-- =====================================================================

ALTER TABLE `articulo`
    MODIFY `precio_articulo` DECIMAL(15,2) NOT NULL DEFAULT 0.00;

ALTER TABLE `precios_programados`
    MODIFY `nuevo_precio` DECIMAL(15,2) NOT NULL;
