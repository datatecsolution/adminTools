-- =====================================================================
-- V12 — Convertir tablas MyISAM a InnoDB
--
-- US-013 del sprint de remediacion BD.
--
-- Contexto: el baseline V1 dejo 3 tablas en MyISAM (articulo_bodega,
-- pagos_creditos, salida_productos) por razones historicas. MyISAM
-- carece de:
--   - Transacciones
--   - Integridad referencial (FKs)
--   - Crash recovery (corrupcion ante shutdown abrupto)
--
-- Todo el resto del esquema esta en InnoDB. Unificar a InnoDB es
-- pre-requisito para US-022 (FKs) y reduce riesgo operacional.
--
-- Estrategia: stored procedure que itera information_schema.TABLES y
-- ALTERa cualquier MyISAM detectado. Esto es:
--   - Idempotente: si ya estan en InnoDB, no hace nada.
--   - Robusto contra drift: convierte tambien tablas que no estan en
--     el baseline (creadas dinamicamente o agregadas en migrations
--     Java intermedias).
--   - Self-cleanup: la procedure se destruye al final, no queda residuo.
--
-- Riesgo: ALTER TABLE bloquea la tabla durante la conversion. Las 3
-- tablas conocidas son chicas. Si en runtime aparece una tabla MyISAM
-- grande (ej. log de auditoria), la conversion puede tardar minutos —
-- coordinarlo en ventana de mantenimiento.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS convert_myisam_to_innodb$$

CREATE PROCEDURE convert_myisam_to_innodb()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tname VARCHAR(64);
    DECLARE cur CURSOR FOR
        SELECT TABLE_NAME
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_TYPE = 'BASE TABLE'
          AND ENGINE = 'MyISAM';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    convert_loop: LOOP
        FETCH cur INTO tname;
        IF done THEN
            LEAVE convert_loop;
        END IF;

        SET @sql = CONCAT('ALTER TABLE `', tname, '` ENGINE=InnoDB');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    CLOSE cur;
END$$

DELIMITER ;

CALL convert_myisam_to_innodb();

DROP PROCEDURE convert_myisam_to_innodb;
