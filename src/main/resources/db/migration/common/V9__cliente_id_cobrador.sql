-- Separa el rol de cobrador del vendedor en clientes a credito.
-- Antes: cliente.id_vendedor se usaba como cobrador para tipo_cliente=2.
-- Ahora: id_cobrador es el campo dedicado y id_vendedor queda para
-- comision real de venta (default 0 para clientes a credito).

ALTER TABLE `cliente`
    ADD COLUMN `id_cobrador` int NOT NULL DEFAULT 0
    AFTER `id_vendedor`;

UPDATE `cliente`
SET `id_cobrador` = `id_vendedor`
WHERE `tipo_cliente` = 2;
