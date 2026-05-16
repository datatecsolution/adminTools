-- =====================================================================
-- Auditoria de tablas MyISAM previa a la migracion V12 (US-013).
--
-- Ejecutar en cada esquema antes de aplicar V12, para saber que se va
-- a convertir. Re-ejecutar despues para confirmar cero MyISAM
-- restantes.
--
-- Es 100% solo-lectura: consulta information_schema.
--
-- Uso:
--   mysql -u <user> -p <esquema> < auditoria-myisam-pre-v12.sql
-- =====================================================================

SET @db := DATABASE();

-- ---------------------------------------------------------------------
-- Bloque A — Conteo por engine
-- ---------------------------------------------------------------------
SELECT
    ENGINE                     AS engine,
    COUNT(*)                   AS cant_tablas
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @db
  AND TABLE_TYPE = 'BASE TABLE'
GROUP BY ENGINE
ORDER BY cant_tablas DESC;


-- ---------------------------------------------------------------------
-- Bloque B — Detalle de tablas MyISAM (candidatas a convertir)
-- ---------------------------------------------------------------------
SELECT
    TABLE_NAME                                     AS tabla,
    ENGINE                                         AS engine,
    TABLE_ROWS                                     AS filas_aprox,
    ROUND(DATA_LENGTH / 1024 / 1024, 2)            AS data_mb,
    ROUND(INDEX_LENGTH / 1024 / 1024, 2)           AS index_mb,
    ROW_FORMAT                                     AS row_format
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = @db
  AND TABLE_TYPE = 'BASE TABLE'
  AND ENGINE = 'MyISAM'
ORDER BY DATA_LENGTH DESC;


-- ---------------------------------------------------------------------
-- Bloque C — Riesgos: tablas MyISAM con indices FULLTEXT
-- ---------------------------------------------------------------------
-- FULLTEXT en MyISAM no se preserva al convertir a InnoDB en MySQL <5.6
-- ni en MariaDB <10.0.5. En versiones modernas (MySQL 5.6+ / MariaDB
-- 10.0.5+) InnoDB soporta FULLTEXT pero el indice se debe recrear con
-- la sintaxis InnoDB. Si hay matches aqui, V12 puede perder el indice.
SELECT
    s.TABLE_NAME    AS tabla,
    s.INDEX_NAME    AS indice,
    s.COLUMN_NAME   AS columna,
    s.INDEX_TYPE    AS tipo
FROM information_schema.STATISTICS s
JOIN information_schema.TABLES t
  ON  t.TABLE_SCHEMA = s.TABLE_SCHEMA
  AND t.TABLE_NAME   = s.TABLE_NAME
WHERE s.TABLE_SCHEMA = @db
  AND t.ENGINE = 'MyISAM'
  AND s.INDEX_TYPE = 'FULLTEXT';
