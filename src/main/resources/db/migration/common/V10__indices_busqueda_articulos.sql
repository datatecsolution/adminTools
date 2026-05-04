-- Indices compuestos para acelerar las busquedas de articulos.
--
-- Contexto: ArticuloDao incluye en el SELECT base la funcion
-- f_can_saldo_kardex(codigo_articulo, codigo_bodega), que se ejecuta una vez
-- por cada fila del resultset. Esa funcion filtra articulo_kardex por
-- (codigo_articulo, codigo_bodega) y movimiento_kardex por
-- (codigo_movimiento, codigo_tipo_movimiento). Con los KEY simples actuales
-- MySQL puede usar solo uno y termina filtrando el resto leyendo filas.
--
-- Adicionalmente, las busquedas por codigo de barra escanean codigos_articulos
-- sin indice en codigo_barra.

ALTER TABLE `articulo_kardex`
    ADD INDEX `idx_articulo_kardex_articulo_bodega` (`codigo_articulo`, `codigo_bodega`);

ALTER TABLE `movimiento_kardex`
    ADD INDEX `idx_movimiento_kardex_mov_tipo` (`codigo_movimiento`, `codigo_tipo_movimiento`);

ALTER TABLE `codigos_articulos`
    ADD INDEX `idx_codigos_articulos_codigo_barra` (`codigo_barra`);
