-- =====================================================================
-- V44 / US-132 — f_can_saldo_kardex lee el saldo materializado
--
-- Continuacion directa de US-131 (V43): la misma cirugia sobre la
-- funcion HERMANA. f_existencia_y_ordenes quedo optimizada, pero el
-- SWING no la usa: todo su calculo de stock pasa por f_can_saldo_kardex
-- — incrustada POR FILA en las consultas de listado de ArticuloDao
-- (lineas 50/74) y en getDisponible / getDisponibleVenta /
-- getExistencia. Cada pantalla de catalogo del Swing paga el paseo del
-- kardex (3 joins + ORDER BY DESC + LIMIT 1 sobre 1.6M de filas en
-- Sharon) una vez por articulo mostrado.
--
-- Cambio: saldo por SELECT de PK sobre existencia_articulo_bodega, con
-- fallback al paseo del kardex si la ficha no tiene fila. Los SPs del
-- kardex NO usan esta funcion (verificado) — sus lecturas FOR UPDATE
-- quedan intactas.
--
-- CONTRATO, con un matiz documentado:
--  - Misma firma y mismo RETURNS double(11,2).
--  - La version vieja devolvia NULL para una ficha sin ningun saldo
--    tipo 3. Como el backfill de V18 creo fila con 0 para TODAS las
--    fichas (IFNULL(f_can_saldo,0)), la nueva devuelve 0.00 en ese
--    caso. Diferencia auditada: todos los callers Java leen getDouble()
--    (NULL ya coercionaba a 0.0) y los callers SQL envuelven en
--    IFNULL(...,0) — ningun caller distingue NULL de 0. El fallback
--    conserva el NULL para fichas sin fila NI saldo (paridad exacta
--    donde la tabla no alcanza).
--
-- Evidencia (misma base que US-131, docs/us131 y docs/us132):
--  - Reconciliacion completa en Sharon: 5.141 fichas tabla-vs-paseo,
--    0 mismatches, 0 faltantes — comparadas justamente contra ESTA
--    funcion.
--  - Arnes de verificacion en docs/us132/verificacion-us132.sql.
-- =====================================================================

DROP FUNCTION IF EXISTS `f_can_saldo_kardex`;

DELIMITER $$
CREATE FUNCTION `f_can_saldo_kardex`(p_cod_articulo int(11), p_cod_bodega int(11)) RETURNS double(11,2)
    DETERMINISTIC
BEGIN

    DECLARE v_saldo DOUBLE(11,2) DEFAULT NULL;

    -- US-132: saldo desde la tabla materializada — SELECT por PK.
    SELECT `cantidad` INTO v_saldo
    FROM `existencia_articulo_bodega`
    WHERE `codigo_articulo` = p_cod_articulo
      AND `codigo_bodega`   = p_cod_bodega;

    -- Fallback defensivo (mismo diseno que V43): sin fila en la tabla,
    -- se camina el kardex como siempre. Conserva el NULL historico si
    -- tampoco hay saldo tipo 3.
    IF v_saldo IS NULL THEN
        SET v_saldo = (SELECT `saldos`.`cantidad`
            FROM `articulo_kardex`
            JOIN `detalle_movimiento_kardex`
                ON `articulo_kardex`.`codigo_kardex` = `detalle_movimiento_kardex`.`codigo_kardex`
            JOIN `movimiento_kardex` `saldos`
                ON `detalle_movimiento_kardex`.`codigo_movimiento` = `saldos`.`codigo_movimiento`
                AND `saldos`.`codigo_tipo_movimiento` = 3
            WHERE `articulo_kardex`.`codigo_articulo` = p_cod_articulo
              AND `articulo_kardex`.`codigo_bodega` = p_cod_bodega
            ORDER BY `detalle_movimiento_kardex`.`codigo_movimiento` DESC
            LIMIT 1);
    END IF;

    RETURN v_saldo;
end$$
DELIMITER ;
