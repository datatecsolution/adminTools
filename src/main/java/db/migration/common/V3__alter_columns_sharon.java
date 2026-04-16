package db.migration.common;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * V3: ALTERs condicionales para alinear clientes V1 con el esquema Sharon.
 * Se usa Java porque Flyway SQL no soporta DELIMITER ni procedimientos inline.
 */
public class V3__alter_columns_sharon extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();

        // -- config_user_facturacion --
        addColumn(conn, "config_user_facturacion", "agregar_cliente_credito",  "tinyint NOT NULL DEFAULT '0'");
        addColumn(conn, "config_user_facturacion", "formato_factura_credito",  "varchar(50) NOT NULL DEFAULT 'tiket'");
        addColumn(conn, "config_user_facturacion", "pwd_entre_precio",         "tinyint NOT NULL DEFAULT '0'");
        addColumn(conn, "config_user_facturacion", "imp_report_order",         "tinyint DEFAULT '0'");
        addColumn(conn, "config_user_facturacion", "unir_can_item",            "tinyint DEFAULT '0'");
        addColumn(conn, "config_user_facturacion", "delete_item_fact",         "tinyint DEFAULT '0'");
        addColumn(conn, "config_user_facturacion", "cant_facturas_imprimir",   "int DEFAULT '1'");

        // -- encabezado_factura (common) --
        addColumn(conn, "encabezado_factura", "fecha_vencimiento", "date NOT NULL DEFAULT '1990-01-01'");
        addColumn(conn, "encabezado_factura", "cobro_tarjeta",     "float(11,2) NOT NULL DEFAULT '0.00'");
        addColumn(conn, "encabezado_factura", "cobro_efectivo",    "float(11,2) NOT NULL DEFAULT '0.00'");
        addColumn(conn, "encabezado_factura", "codigo_vendedor",   "int NOT NULL DEFAULT '1'");
        addColumn(conn, "encabezado_factura", "estado_pago",       "int NOT NULL DEFAULT '0'");
        addColumn(conn, "encabezado_factura", "cod_rango",         "int NOT NULL DEFAULT '1'");

        modifyColumn(conn, "encabezado_factura", "fecha",          "datetime NOT NULL");
        modifyColumn(conn, "encabezado_factura", "pago",           "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura", "isv18",          "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura", "descuento",      "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura", "cobro_tarjeta",  "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura", "cobro_efectivo", "float(11,2) NOT NULL DEFAULT '0.00'");

        // -- detalle_factura (common) --
        modifyColumn(conn, "detalle_factura", "precio",    "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "detalle_factura", "cantidad",  "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "detalle_factura", "impuesto",  "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "detalle_factura", "subtotal",  "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "detalle_factura", "descuento", "float(11,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "detalle_factura", "total",     "float(11,2) NOT NULL DEFAULT '0.00'");

        // -- datos_factura --
        addColumn(conn, "datos_factura", "observacion", "varchar(255) DEFAULT ''");

        // -- encabezado_factura_compra --
        addColumn(conn, "encabezado_factura_compra", "codigo_bodega", "int NOT NULL DEFAULT '-1'");

        // -- detalle_factura_compra --
        addColumn(conn, "detalle_factura_compra", "fecha_venc", "date DEFAULT '1990-01-01'");

        // -- cliente --
        addColumn(conn, "cliente", "id_ruta_cobro", "int NOT NULL DEFAULT '1'");

        // -- empleados --
        addColumn(conn, "empleados", "usuario", "varchar(150) NOT NULL DEFAULT 'system'");

        // -- recibo_pago --
        addColumn(conn, "recibo_pago", "ref", "varchar(100) NOT NULL DEFAULT 'NA'");

        // -- detalle_devoluciones --
        addColumn(conn, "detalle_devoluciones", "agrega_kardex", "int NOT NULL DEFAULT '0'");
        addColumn(conn, "detalle_devoluciones", "codigo_caja",   "int NOT NULL DEFAULT '-1'");

        // -- detalle_devoluciones_compra --
        addColumn(conn, "detalle_devoluciones_compra", "descuento",     "float(10,2) NOT NULL DEFAULT '0.00'");
        addColumn(conn, "detalle_devoluciones_compra", "agrega_kardex", "int NOT NULL DEFAULT '0'");
        addColumn(conn, "detalle_devoluciones_compra", "codigo_bodega", "int NOT NULL DEFAULT '1'");

        // -- detalle_requisicion --
        addColumn(conn, "detalle_requisicion", "agrega_kardex", "int NOT NULL DEFAULT '0'");

        // -- config_app --
        addColumn(conn, "config_app", "interes_para_facturas_venc", "int NOT NULL DEFAULT '0'");

        // -- usuario --
        addColumn(conn, "usuario", "codigo_caja",    "int NOT NULL DEFAULT '0'");
        addColumn(conn, "usuario", "api_token",       "varchar(60) DEFAULT NULL");
        addColumn(conn, "usuario", "created_at",      "timestamp NULL DEFAULT NULL");
        addColumn(conn, "usuario", "updated_at",      "timestamp NULL DEFAULT NULL");
        addColumn(conn, "usuario", "enabled",         "bit(1) DEFAULT NULL");
        addColumn(conn, "usuario", "codigo_empleado", "int DEFAULT '1'");

        // -- encabezado_factura_temp --
        addColumn(conn, "encabezado_factura_temp", "codigo_caja",     "int NOT NULL DEFAULT '1'");
        addColumn(conn, "encabezado_factura_temp", "codigo_vendedor", "int DEFAULT '1'");
        addColumn(conn, "encabezado_factura_temp", "isv_otros",       "decimal(38,2) DEFAULT '0.00'");
        addColumn(conn, "encabezado_factura_temp", "estado",          "int DEFAULT '1'");
        addColumn(conn, "encabezado_factura_temp", "observacion",     "varchar(255) DEFAULT 'NA'");

        modifyColumn(conn, "encabezado_factura_temp", "fecha",            "datetime(6) DEFAULT NULL");
        modifyColumn(conn, "encabezado_factura_temp", "subtotal_excento", "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "subtotal15",       "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "subtotal18",       "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "subtotal",         "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "impuesto",         "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "total",            "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "estado_factura",   "varchar(255) NOT NULL DEFAULT 'ACT'");
        modifyColumn(conn, "encabezado_factura_temp", "isvOtros",         "float(10,2) NOT NULL DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "isv18",            "decimal(38,2) DEFAULT '0.00'");
        modifyColumn(conn, "encabezado_factura_temp", "pago",             "decimal(38,2) DEFAULT NULL");
        modifyColumn(conn, "encabezado_factura_temp", "descuento",        "decimal(38,2) DEFAULT NULL");
    }

    private void addColumn(Connection conn, String table, String column, String definition) throws Exception {
        if (!columnExists(conn, table, column)) {
            try (Statement s = conn.createStatement()) {
                s.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
            }
        }
    }

    private void modifyColumn(Connection conn, String table, String column, String definition) throws Exception {
        if (columnExists(conn, table, column)) {
            try (Statement s = conn.createStatement()) {
                s.execute("ALTER TABLE `" + table + "` MODIFY COLUMN `" + column + "` " + definition);
            }
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
