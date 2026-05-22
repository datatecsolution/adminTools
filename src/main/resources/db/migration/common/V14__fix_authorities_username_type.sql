-- =====================================================================
-- V14 — Corregir tipo de authorities.username (BIGINT → VARCHAR(255))
--
-- Detectado en Ronal (10.10.0.1) el 2026-05-21 con
-- spring.jpa.hibernate.ddl-auto=validate de la API admintools:
--
--   Schema-validation: wrong column type encountered in column
--   [username] in table [authorities]; found [bigint (Types#BIGINT)],
--   but expecting [varchar(255) (Types#VARCHAR)]
--
-- La entidad Authority (admintools-api, Spring Security) define:
--   @JoinColumn(name = "username", referencedColumnName = "usuario")
-- Es decir: authorities.username debe ser VARCHAR(255) y referenciar
-- usuario.usuario (string), NO usuario.id (BIGINT).
--
-- Cliente A (192.168.1.23) ya tenía VARCHAR (probablemente ALTER manual
-- historico). Ronal todavia tiene BIGINT del diseno original.
--
-- Idempotencia: procedure verifica el tipo actual. Si ya es VARCHAR,
-- no hace nada. Asi V14 es seguro de aplicar en cualquier cliente
-- futuro sin importar en qué estado venga.
--
-- Pre-requisito verificado: tabla authorities VACIA en Ronal (0 filas),
-- asi que no hay perdida de datos al cambiar tipo. Si en otro cliente
-- estuviera con datos, los valores BIGINT (que son usuario.id) NO se
-- preservarian — habria que hacer un UPDATE previo con JOIN. Por ahora
-- ningun cliente productivo conocido tiene esa tabla con datos.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS fix_authorities_username_type$$

CREATE PROCEDURE fix_authorities_username_type()
BEGIN
    DECLARE v_data_type  VARCHAR(20) DEFAULT NULL;
    DECLARE v_fk_name    VARCHAR(64) DEFAULT NULL;
    DECLARE v_row_count  INT         DEFAULT 0;

    -- 1. Tipo actual de la columna
    SELECT DATA_TYPE
      INTO v_data_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'authorities'
      AND COLUMN_NAME  = 'username';

    -- Si la tabla authorities no existe en este schema (caso common-only),
    -- DATA_TYPE viene NULL. Skip silencioso.
    IF v_data_type IS NULL THEN
        -- Nada que hacer
        SET @noop = 1;
    ELSEIF v_data_type = 'bigint' THEN
        -- Drift detectado: hay que migrar de BIGINT a VARCHAR.

        -- 1a. Verificar que la tabla este vacia. Si tiene datos, ABORTAR
        --     para no perder valores que un fix puro de tipo no preserva.
        SELECT COUNT(*) INTO v_row_count FROM authorities;
        IF v_row_count > 0 THEN
            SIGNAL SQLSTATE '45000'
              SET MESSAGE_TEXT = 'V14: authorities tiene filas. Migracion necesita UPDATE previo (JOIN con usuario por id). Abortando.';
        END IF;

        -- 1b. Buscar y dropear FK existente (nombre varia entre BDs)
        SELECT CONSTRAINT_NAME
          INTO v_fk_name
        FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA        = DATABASE()
          AND TABLE_NAME          = 'authorities'
          AND COLUMN_NAME         = 'username'
          AND REFERENCED_TABLE_NAME IS NOT NULL
        LIMIT 1;

        IF v_fk_name IS NOT NULL THEN
            SET @sql = CONCAT('ALTER TABLE authorities DROP FOREIGN KEY `', v_fk_name, '`');
            PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

            -- El index que la FK creo queda. Tirarlo tambien (lo recreamos abajo).
            SET @sql = CONCAT('ALTER TABLE authorities DROP INDEX `', v_fk_name, '`');
            PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
        END IF;

        -- 1c. Modificar el tipo
        ALTER TABLE authorities MODIFY COLUMN username VARCHAR(255) DEFAULT NULL;

        -- 1d. Index para lookups por username (la API consulta authorities
        --     por username string al cargar UserDetails).
        ALTER TABLE authorities ADD INDEX idx_authorities_username (username);
    END IF;
    -- Si v_data_type = 'varchar' → ya esta bien, no hacer nada.
END$$

DELIMITER ;

CALL fix_authorities_username_type();

DROP PROCEDURE fix_authorities_username_type;
