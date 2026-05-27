-- =====================================================================
-- V17 — Crear bodega/departamento "Pérdidas"
--
-- Prerequisito del subsistema de inventario del API (epic INV, historia
-- INV-0). El modelo acordado es: los ajustes de inventario son DOCUMENTOS
-- auditables, no "setear stock" arbitrario.
--   - Sobrante (físico > sistema) → entrada (compra / inventario inicial).
--   - Faltante (sistema > físico, el caso común) → salida vía REQUISICION
--     desde la bodega de trabajo hacia esta bodega "Pérdidas".
--
-- bodega y departamento son tablas espejo (sus códigos deben coincidir
-- porque los triggers de requisicion usan codigo_depart_origen/destino
-- como codigo_bodega del kardex).
--
-- Idempotente: si ya existe "Pérdidas" en ambas tablas con códigos que
-- coinciden, no hace nada. Si existe inconsistente (en una sola, o con
-- códigos distintos), SIGNAL para que un humano lo resuelva manualmente.
-- =====================================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS v17_crear_bodega_perdidas$$

CREATE PROCEDURE v17_crear_bodega_perdidas()
BEGIN
    DECLARE v_cod_bodega        INT DEFAULT NULL;
    DECLARE v_cod_departamento  INT DEFAULT NULL;
    DECLARE v_nuevo_codigo      INT;

    -- ¿ya existe "Pérdidas" en bodega? (case-insensitive)
    SELECT codigo_bodega INTO v_cod_bodega
    FROM bodega
    WHERE descripcion_bodega = 'Pérdidas'
    LIMIT 1;

    -- ¿ya existe "Pérdidas" en departamento?
    SELECT codigo_departamento INTO v_cod_departamento
    FROM departamento
    WHERE nombre = 'Pérdidas'
    LIMIT 1;

    -- Caso A: ya existen en ambas y con códigos que coinciden → no hacer nada
    IF v_cod_bodega IS NOT NULL
       AND v_cod_departamento IS NOT NULL
       AND v_cod_bodega = v_cod_departamento THEN
        -- idempotente, ya está
        SET @v17_noop = 1;

    -- Caso B: existen en ambas pero con códigos distintos → estado inconsistente
    ELSEIF v_cod_bodega IS NOT NULL
           AND v_cod_departamento IS NOT NULL
           AND v_cod_bodega <> v_cod_departamento THEN
        -- MESSAGE_TEXT limitado a 128 chars por MySQL
        SIGNAL SQLSTATE '45000'
          SET MESSAGE_TEXT = 'V17: bodega/departamento "Perdidas" con codigos distintos; resolver a mano antes de re-aplicar.';

    -- Caso C: existe en una sola tabla → estado parcial, completar con el código existente
    ELSEIF v_cod_bodega IS NOT NULL AND v_cod_departamento IS NULL THEN
        INSERT INTO departamento (codigo_departamento, nombre)
        VALUES (v_cod_bodega, 'Pérdidas');

    ELSEIF v_cod_bodega IS NULL AND v_cod_departamento IS NOT NULL THEN
        INSERT INTO bodega (codigo_bodega, descripcion_bodega)
        VALUES (v_cod_departamento, 'Pérdidas');

    -- Caso D: no existe en ninguna → crear con código nuevo igual en ambas
    ELSE
        -- siguiente código libre que NO colisione con ninguna de las dos secuencias
        SELECT GREATEST(
            IFNULL((SELECT MAX(codigo_bodega)       FROM bodega),       0),
            IFNULL((SELECT MAX(codigo_departamento) FROM departamento), 0)
        ) + 1 INTO v_nuevo_codigo;

        INSERT INTO bodega       (codigo_bodega,       descripcion_bodega) VALUES (v_nuevo_codigo, 'Pérdidas');
        INSERT INTO departamento (codigo_departamento, nombre)             VALUES (v_nuevo_codigo, 'Pérdidas');
    END IF;
END$$

DELIMITER ;

CALL v17_crear_bodega_perdidas();
DROP PROCEDURE v17_crear_bodega_perdidas;
