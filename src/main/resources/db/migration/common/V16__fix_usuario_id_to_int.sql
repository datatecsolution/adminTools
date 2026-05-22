-- =====================================================================
-- V16 — Migrar usuario.id de BIGINT a INT
--
-- Detectado al correr API admintools con ddl-auto=validate contra Ronal
-- el 2026-05-21. La entidad Usuario tiene:
--   @JdbcTypeCode(SqlTypes.INTEGER) private Long idUsuario;
-- Es decir: el field Java es Long (por legacy) pero la BD debe ser INT.
-- En Ronal quedo en BIGINT.
--
-- Pre-verificado en Ronal:
--   - 57 filas, max(id) = 59 → cabe en INT (max 2.147.483.647)
--   - 0 FKs apuntando a usuario.id (la unica, authorities.username,
--     fue dropeada por V14)
-- Por lo tanto: ALTER COLUMN directo, sin drops de FK ni UPDATEs.
--
-- Cliente A (192.168.1.23) ya tiene INT → procedure idempotente skipea.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS fix_usuario_id_to_int$$

CREATE PROCEDURE fix_usuario_id_to_int()
BEGIN
    DECLARE v_type VARCHAR(20) DEFAULT NULL;
    DECLARE v_max  BIGINT      DEFAULT 0;

    SELECT DATA_TYPE INTO v_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'usuario'
      AND COLUMN_NAME  = 'id';

    IF v_type IS NOT NULL AND v_type <> 'int' THEN
        -- Sanity check: verificar que ningun id excede INT max
        SELECT IFNULL(MAX(id), 0) INTO v_max FROM usuario;
        IF v_max > 2147483647 THEN
            SIGNAL SQLSTATE '45000'
              SET MESSAGE_TEXT = 'V16: usuario.id tiene valores > INT max. No se puede migrar a INT.';
        END IF;

        ALTER TABLE usuario MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT;
    END IF;
END$$

DELIMITER ;

CALL fix_usuario_id_to_int();
DROP PROCEDURE fix_usuario_id_to_int;
