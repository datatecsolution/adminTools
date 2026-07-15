-- =====================================================================
-- V38 (common) — US-081: categorías jerárquicas padre-hijo en `marcas`
--
-- Fase 2 del cierre Hito 1. Agrega `parent_id` (self-FK a codigo_marca)
-- para modelar árbol de categorías. La API expone GET /categories/tree y
-- valida ciclos; el POS muestra el árbol y el selector con path
-- "Padre > Hijo".
--
-- ADITIVA y nullable → un Swing viejo sigue funcionando:
--   - CategoriaDao.registrar usa lista explícita de columnas
--     (descripcion, observacion), no INSERT posicional.
--   - CategoriaDao.actualizar hace SET por columna. No toca parent_id.
--   - El DELETE del Swing sobre una categoría con hijos ahora falla por el
--     FK (RESTRICT) con el diálogo genérico de error de BD — protector:
--     antes de V38 no había forma de tener hijos, así que solo pasa si la
--     jerarquía se creó desde el POS/API.
--
-- Tipo: INT UNSIGNED para matchear codigo_marca (int unsigned, PK
-- auto_increment). NULL = categoría raíz. El FK crea su propio índice.
-- Idempotencia: si se re-aplica sobre una BD que ya tiene la columna, el
-- ALTER falla — pero Flyway solo la ejecuta una vez (schema_version).
-- =====================================================================

ALTER TABLE `marcas`
    ADD COLUMN `parent_id` INT UNSIGNED NULL DEFAULT NULL AFTER `observacion`,
    ADD CONSTRAINT `fk_marcas_parent`
        FOREIGN KEY (`parent_id`) REFERENCES `marcas` (`codigo_marca`);
