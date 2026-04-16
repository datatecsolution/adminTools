package db.migration.common;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Fase 4: Eliminar indices UNIQUE redundantes que duplican la PK.
 * Cada PK ya garantiza unicidad; el indice extra penaliza escrituras.
 */
public class V4__drop_duplicate_indexes extends BaseJavaMigration {

    private static final String[][] DUPLICATES = {
        {"articulo",                  "codigo"},
        {"bodega",                    "codigo"},
        {"cierre_caja",              "idCierre"},
        {"cliente",                   "codigo"},
        {"cuentas_por_cobrar",       "codigo"},
        {"cuentas_por_pagar",        "codigo"},
        {"detalle_factura",          "codigo"},
        {"detalle_movimiento_kardex","codigo_movimiento"},
        {"empleados",                "id"},
        {"encabezado_cotizacion",    "numero_factura"},
        {"encabezado_factura",       "numero_factura"},
        {"encabezado_factura_compra","numero_factura"},
        {"encabezado_factura_temp",  "numero_factura"},
        {"impuesto",                 "codigo"},
        {"marcas",                   "codigo_marca"},
        {"recibo_pago",              "codigo"},
        {"recibo_pago_proveedores",  "codigo"},
        {"tipo_factura",             "id_tipo_factura"},
        {"tipo_pago",                "id"},
    };

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        for (String[] pair : DUPLICATES) {
            dropIndexIfExists(conn, pair[0], pair[1]);
        }
    }

    private void dropIndexIfExists(Connection conn, String table, String indexName) throws Exception {
        if (indexExists(conn, table, indexName)) {
            try (Statement s = conn.createStatement()) {
                s.execute("ALTER TABLE `" + table + "` DROP INDEX `" + indexName + "`");
            }
        }
    }

    private boolean indexExists(Connection conn, String table, String indexName) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.statistics "
                + "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?")) {
            ps.setString(1, table);
            ps.setString(2, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
