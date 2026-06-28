-- =====================================================================
-- V32 — Acta de Toma Física (registro de inventarios físicos)
--
-- Persiste el "acta" de cada toma física: quién contó, cuándo, en qué
-- bodega, cuántos faltantes/sobrantes/negativos y el dinero que
-- representan (encabezado) + el detalle por artículo (sistema/físico/
-- diferencia/costo/estado). Antes solo quedaban los MOVIMIENTOS del cierre
-- (requisición de faltantes + compras de ajuste de sobrantes/negativos),
-- pero NO el conteo en sí — gap #2 del módulo de inventario.
--
-- Aditivo: tablas nuevas, no toca los flujos de ajuste existentes
-- (sobrante=compra V25, faltante=requisición V17). La API solo valida
-- (ddl-auto=validate); estas tablas las crea esta migración.
--
-- CREATE TABLE IF NOT EXISTS: idempotente (se puede aplicar a mano en la
-- BD local de prueba y que Flyway la registre después sin chocar).
-- =====================================================================

CREATE TABLE IF NOT EXISTS `inventory_count` (
    `codigo_inventario_count` INT NOT NULL AUTO_INCREMENT,
    `fecha`            DATETIME       NOT NULL,
    `usuario`          VARCHAR(100)   NOT NULL,
    `codigo_bodega`    INT            NOT NULL,
    `contadas`         INT            NOT NULL DEFAULT 0,
    `faltantes`        INT            NOT NULL DEFAULT 0,
    `sobrantes`        INT            NOT NULL DEFAULT 0,
    `negativos`        INT            NOT NULL DEFAULT 0,
    `valor_ajuste`     DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    `valor_negativos`  DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    `motivo`           VARCHAR(255)   NULL,
    `estado`           VARCHAR(10)    NOT NULL DEFAULT 'ACT',
    PRIMARY KEY (`codigo_inventario_count`),
    KEY `idx_inv_count_bodega_fecha` (`codigo_bodega`, `fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

CREATE TABLE IF NOT EXISTS `inventory_count_line` (
    `id_detalle_inventario_count` INT NOT NULL AUTO_INCREMENT,
    `codigo_inventario_count` INT          NOT NULL,
    `codigo_articulo`         INT          NOT NULL,
    `sistema`                 DECIMAL(18,4) NOT NULL,
    `fisico`                  DECIMAL(18,4) NOT NULL,
    `diferencia`              DECIMAL(18,4) NOT NULL,
    `costo`                   DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    `estado_linea`            VARCHAR(10)  NOT NULL,
    PRIMARY KEY (`id_detalle_inventario_count`),
    KEY `idx_inv_count_line_header` (`codigo_inventario_count`),
    CONSTRAINT `fk_inv_count_line_header`
        FOREIGN KEY (`codigo_inventario_count`)
        REFERENCES `inventory_count` (`codigo_inventario_count`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
