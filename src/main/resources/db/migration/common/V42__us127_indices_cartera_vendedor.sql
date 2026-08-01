-- US-127: transferencia de cartera de clientes entre vendedores.
--
-- La pantalla nueva lista los clientes de un vendedor con
--   WHERE id_vendedor = ? OR id_cobrador = ?
-- y la tabla `cliente` solo tenia indice por `tipo_cliente` (V1__baseline),
-- asi que esa consulta hacia full scan. Con carteras de miles de clientes
-- eso se siente en la pantalla y bloquea filas de mas durante el UPDATE
-- masivo de la transferencia.
--
-- Se agregan los dos indices por separado (no uno compuesto) porque el OR
-- del WHERE se resuelve mejor con index_merge que con un indice combinado.

ALTER TABLE `cliente`
    ADD INDEX `idx_cliente_vendedor` (`id_vendedor`);

ALTER TABLE `cliente`
    ADD INDEX `idx_cliente_cobrador` (`id_cobrador`);
