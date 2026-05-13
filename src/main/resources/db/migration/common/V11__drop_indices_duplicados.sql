-- =====================================================================
-- V11 — Drop de indices duplicados con la PRIMARY KEY
--
-- US-005 del sprint de remediacion BD.
--
-- Contexto: auditoria contra produccion (docs/auditoria-indices-pre-v11-v12.sql)
-- detecto solo 2 indices redundantes que siguen vivos en admin_tools:
--   1. articulo_kardex.codigo_kardex  - PK + INDEX `codigo_kardex` (misma col)
--   2. cuentas_por_cobrar_facturas    - PK sobre codigo_reguistro + INDEX `codigo`
--
-- El resto de candidatos detectados en V1__baseline ya habian sido limpiados
-- por V4__drop_duplicate_indexes.java en migraciones previas.
--
-- Riesgo: muy bajo. La PRIMARY KEY ya garantiza unicidad y cobertura para
-- lookups por la columna. Los DROP son condicionales via information_schema
-- para que la migracion sea idempotente en bases que ya tengan el indice
-- eliminado.
-- =====================================================================

-- 1. articulo_kardex: drop INDEX `codigo_kardex` (redundante con PK)
SET @stmt := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'articulo_kardex'
       AND INDEX_NAME = 'codigo_kardex') > 0,
    'ALTER TABLE `articulo_kardex` DROP INDEX `codigo_kardex`',
    'SELECT ''V11: indice codigo_kardex no existe en articulo_kardex, skip'' AS info'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 2. cuentas_por_cobrar_facturas: drop INDEX `codigo` (redundante con PK)
SET @stmt := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'cuentas_por_cobrar_facturas'
       AND INDEX_NAME = 'codigo') > 0,
    'ALTER TABLE `cuentas_por_cobrar_facturas` DROP INDEX `codigo`',
    'SELECT ''V11: indice codigo no existe en cuentas_por_cobrar_facturas, skip'' AS info'
);
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
