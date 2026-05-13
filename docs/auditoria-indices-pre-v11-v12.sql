-- =====================================================================
-- Auditoria previa a las migraciones V11 (drop indices duplicados) y
-- V12 (indices en columnas de fecha).
--
-- US-005 / US-006 del sprint de remediacion BD.
--
-- Ejecutar en cada esquema productivo (admin_tools, admin_tools_caja_*)
-- ANTES de aplicar las migraciones para confirmar la lista exacta de
-- indices a tocar y descartar falsos positivos.
--
-- Es 100% solo-lectura: solo consulta information_schema. No modifica
-- nada. Seguro de correr contra produccion.
--
-- Uso:
--   mysql -u <user> -p <esquema> < auditoria-indices-pre-v11-v12.sql
-- o conectado al esquema:
--   SET @db := DATABASE();   -- y ejecutar cada bloque
-- =====================================================================

SET @db := DATABASE();

-- ---------------------------------------------------------------------
-- BLOQUE 1 — Indices UNIQUE/KEY redundantes con la PRIMARY KEY
-- ---------------------------------------------------------------------
-- Lista cada tabla cuyo PRIMARY KEY es de UNA sola columna y existe
-- otro indice (UNIQUE o KEY simple) que cubre exactamente esa misma
-- columna. Esos son los candidatos a DROP en V11.
--
-- Columnas:
--   tabla, columna_pk, indice_redundante, es_unique, no_columnas_idx
-- ---------------------------------------------------------------------
SELECT
    pk.TABLE_NAME                                          AS tabla,
    pk.COLUMN_NAME                                         AS columna_pk,
    s.INDEX_NAME                                           AS indice_redundante,
    IF(s.NON_UNIQUE = 0, 'UNIQUE', 'INDEX')                AS tipo,
    (SELECT COUNT(*)
     FROM information_schema.STATISTICS s2
     WHERE s2.TABLE_SCHEMA = s.TABLE_SCHEMA
       AND s2.TABLE_NAME   = s.TABLE_NAME
       AND s2.INDEX_NAME   = s.INDEX_NAME)                 AS no_columnas_idx
FROM information_schema.STATISTICS pk
JOIN information_schema.STATISTICS s
  ON  s.TABLE_SCHEMA = pk.TABLE_SCHEMA
  AND s.TABLE_NAME   = pk.TABLE_NAME
  AND s.COLUMN_NAME  = pk.COLUMN_NAME
  AND s.INDEX_NAME  <> 'PRIMARY'
WHERE pk.TABLE_SCHEMA = @db
  AND pk.INDEX_NAME   = 'PRIMARY'
  AND pk.SEQ_IN_INDEX = 1
  -- la PK es de una sola columna
  AND (SELECT COUNT(*)
       FROM information_schema.STATISTICS pk2
       WHERE pk2.TABLE_SCHEMA = pk.TABLE_SCHEMA
         AND pk2.TABLE_NAME   = pk.TABLE_NAME
         AND pk2.INDEX_NAME   = 'PRIMARY') = 1
  -- el indice candidato es tambien de una sola columna
  AND (SELECT COUNT(*)
       FROM information_schema.STATISTICS s2
       WHERE s2.TABLE_SCHEMA = s.TABLE_SCHEMA
         AND s2.TABLE_NAME   = s.TABLE_NAME
         AND s2.INDEX_NAME   = s.INDEX_NAME) = 1
ORDER BY tabla, indice_redundante;


-- ---------------------------------------------------------------------
-- BLOQUE 2 — Columnas de fecha sin indice
-- ---------------------------------------------------------------------
-- Lista columnas date/datetime/timestamp cuyo nombre empieza con
-- "fecha" y que NO tienen ningun indice que las cubra como primera
-- columna. Esos son los candidatos a CREATE INDEX en V12.
--
-- Columnas: tabla, columna, tipo_dato, filas_aprox
-- ---------------------------------------------------------------------
SELECT
    c.TABLE_NAME                AS tabla,
    c.COLUMN_NAME               AS columna,
    c.DATA_TYPE                 AS tipo_dato,
    t.TABLE_ROWS                AS filas_aprox
FROM information_schema.COLUMNS c
JOIN information_schema.TABLES  t
  ON t.TABLE_SCHEMA = c.TABLE_SCHEMA
 AND t.TABLE_NAME   = c.TABLE_NAME
WHERE c.TABLE_SCHEMA = @db
  AND c.COLUMN_NAME LIKE 'fecha%'
  AND c.DATA_TYPE IN ('date','datetime','timestamp')
  AND NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS s
        WHERE s.TABLE_SCHEMA = c.TABLE_SCHEMA
          AND s.TABLE_NAME   = c.TABLE_NAME
          AND s.COLUMN_NAME  = c.COLUMN_NAME
          AND s.SEQ_IN_INDEX = 1
  )
ORDER BY filas_aprox DESC, tabla;


-- ---------------------------------------------------------------------
-- BLOQUE 3 — Columnas de fecha que SI tienen indice (referencia)
-- ---------------------------------------------------------------------
-- Para confirmar que no estamos por duplicar indices ya existentes.
-- ---------------------------------------------------------------------
SELECT
    c.TABLE_NAME            AS tabla,
    c.COLUMN_NAME           AS columna,
    s.INDEX_NAME            AS indice_existente,
    IF(s.NON_UNIQUE = 0, 'UNIQUE', 'INDEX') AS tipo
FROM information_schema.COLUMNS c
JOIN information_schema.STATISTICS s
  ON  s.TABLE_SCHEMA = c.TABLE_SCHEMA
  AND s.TABLE_NAME   = c.TABLE_NAME
  AND s.COLUMN_NAME  = c.COLUMN_NAME
  AND s.SEQ_IN_INDEX = 1
WHERE c.TABLE_SCHEMA = @db
  AND c.COLUMN_NAME LIKE 'fecha%'
  AND c.DATA_TYPE IN ('date','datetime','timestamp')
ORDER BY tabla, columna;
