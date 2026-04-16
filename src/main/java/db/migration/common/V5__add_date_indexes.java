package db.migration.common;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Fase 5: Agregar indices en columnas de fecha usadas por reportes y consultas
 * con BETWEEN. Solo tablas donde hay queries reales por rango de fechas.
 */
public class V5__add_date_indexes extends BaseJavaMigration {

    private static final String[][] INDEXES = {
        // tabla,                       columna,             nombre_indice
        {"encabezado_factura",          "fecha",             "idx_enc_fact_fecha"},
        {"encabezado_factura_compra",   "fecha",             "idx_enc_fact_compra_fecha"},
        {"encabezado_factura_compra",   "fecha_vencimiento", "idx_enc_fact_compra_fecha_venc"},
        {"encabezado_factura_temp",     "fecha",             "idx_enc_fact_temp_fecha"},
        {"encabezado_cotizacion",       "fecha",             "idx_enc_cotiz_fecha"},
        {"encabezado_requisicion",      "fecha",             "idx_enc_requi_fecha"},
        {"cierre_caja",                 "fecha",             "idx_cierre_fecha"},
        {"cuentas_facturas",            "fecha",             "idx_cuentas_fact_fecha"},
        {"cuentas_facturas",            "fecha_vencimiento", "idx_cuentas_fact_fecha_venc"},
        {"cuentas_por_cobrar",          "fecha",             "idx_cxc_fecha"},
        {"cuentas_por_pagar",           "fecha",             "idx_cxp_fecha"},
        {"recibo_pago",                 "fecha",             "idx_recibo_pago_fecha"},
        {"recibo_pago_proveedores",     "fecha",             "idx_recibo_pago_prov_fecha"},
        {"detalle_devoluciones",        "fecha",             "idx_det_devol_fecha"},
        {"movimientos_bancos",          "fecha",             "idx_mov_bancos_fecha"},
        {"entradas_caja",              "fecha",             "idx_entradas_caja_fecha"},
        {"salidas_caja",               "fecha",             "idx_salidas_caja_fecha"},
    };

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        for (String[] idx : INDEXES) {
            addIndexIfNotExists(conn, idx[0], idx[1], idx[2]);
        }
    }

    private void addIndexIfNotExists(Connection conn, String table, String column, String indexName)
            throws Exception {
        if (!tableExists(conn, table)) return;
        if (!columnExists(conn, table, column)) return;
        if (indexExistsOnColumn(conn, table, column)) return;

        try (Statement s = conn.createStatement()) {
            s.execute("ALTER TABLE `" + table + "` ADD INDEX `" + indexName + "` (`" + column + "`)");
        }
    }

    private boolean tableExists(Connection conn, String table) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
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

    private boolean indexExistsOnColumn(Connection conn, String table, String column) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.statistics "
                + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ? AND seq_in_index = 1")) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
