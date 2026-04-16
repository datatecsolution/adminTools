package net.datatecsolution.admin_tools.modelo;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lee la conexion activa de ConexionStatic y genera
 * {@code src/main/resources/db/seed/common.sql} con los datos base que deben
 * quedar cargados cuando se crea una instalacion nueva.
 *
 * Uso:
 *   ./gradlew dumpSeed
 *
 * El archivo resultante lo consume DatabaseBootstrap despues de aplicar el
 * baseline Flyway. NUNCA se ejecuta contra clientes en produccion.
 */
public class SeedDumper {

    private static final String COMMON_DB = "admin_tools";
    private static final Path OUTPUT = Paths.get("src/main/resources/db/seed/common.sql");

    /** Tabla (hint) -> limite de filas; null significa "todas las filas". */
    private static final Object[][] TABLES = {
            {"banco", 2},
            {"bodega", null},
            {"cliente", 1},
            {"config_app", null},
            {"config_user_facturacion", null},
            {"datos_empresa", null},
            {"departamentos", null},
            {"empleados", 1},
            {"impuestos", null},
            {"marcas", 1},
            {"precios", null},
            {"proveedores", 1},
            {"tipo_articulo", null},
            {"tipo_cuenta_banco", null},
            {"tipo_empleado", null},
            {"tipo_factura", null},
            {"tipo_movimiento_banco", null},
            {"tipo_movimiento_kardex", null},
            {"tipo_pago", null},
            {"usuarios", null},
    };

    public static void main(String[] args) throws Exception {
        String user = ConexionStatic.getLogin();
        String password = ConexionStatic.getPassword();
        String driver = ConexionStatic.getDriver();
        String url = String.format(ConexionStatic.getUrlTemplate(), COMMON_DB);

        System.out.println("SeedDumper leyendo de " + url);

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);

        Files.createDirectories(OUTPUT.getParent());

        try (Connection conn = ds.getConnection();
             BufferedWriter out = Files.newBufferedWriter(OUTPUT)) {

            Set<String> existing = loadTableNames(conn);

            out.write("-- Seed de datos base para `admin_tools`.\n");
            out.write("-- Generado por SeedDumper desde la conexion activa de ConexionStatic.\n");
            out.write("-- NO aplicar contra clientes en produccion: lo consume DatabaseBootstrap\n");
            out.write("-- unicamente en instalaciones nuevas, despues de common/V1__baseline.sql.\n\n");
            out.write("SET FOREIGN_KEY_CHECKS=0;\n");
            out.write("SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';\n\n");

            for (Object[] entry : TABLES) {
                String hint = (String) entry[0];
                Integer limit = (Integer) entry[1];
                String table = resolveTable(hint, existing);
                if (table == null) {
                    System.err.println("  [skip] no se encontro tabla para '" + hint + "'");
                    out.write("-- [skip] tabla '" + hint + "' no existe en esta base\n\n");
                    continue;
                }
                dumpTable(conn, out, table, limit);
            }

            out.write("SET FOREIGN_KEY_CHECKS=1;\n");
        } finally {
            ds.close();
        }

        System.out.println("Seed escrito en " + OUTPUT.toAbsolutePath());
    }

    private static Set<String> loadTableNames(Connection conn) throws SQLException {
        Set<String> names = new LinkedHashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = ? AND table_type = 'BASE TABLE'")) {
            ps.setString(1, COMMON_DB);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }
        return names;
    }

    private static String resolveTable(String hint, Set<String> existing) {
        if (existing.contains(hint)) return hint;
        if (hint.endsWith("s")) {
            String sing = hint.substring(0, hint.length() - 1);
            if (existing.contains(sing)) return sing;
            if (hint.endsWith("es")) {
                String sing2 = hint.substring(0, hint.length() - 2);
                if (existing.contains(sing2)) return sing2;
            }
        }
        if (existing.contains(hint + "s")) return hint + "s";
        if (existing.contains(hint + "es")) return hint + "es";
        for (String t : existing) {
            if (t.equalsIgnoreCase(hint)) return t;
        }
        return null;
    }

    private static void dumpTable(Connection conn, BufferedWriter out,
                                  String table, Integer limit) throws Exception {
        String sql = "SELECT * FROM `" + table + "`" + (limit != null ? " LIMIT " + limit : "");
        int rows = 0;
        out.write("-- ============================================\n");
        out.write("-- " + table + (limit != null ? " (limit " + limit + ")" : " (todas las filas)") + "\n");
        out.write("-- ============================================\n");
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            StringBuilder cols = new StringBuilder();
            for (int i = 1; i <= n; i++) {
                if (i > 1) cols.append(',');
                cols.append('`').append(md.getColumnName(i)).append('`');
            }
            while (rs.next()) {
                StringBuilder vals = new StringBuilder();
                for (int i = 1; i <= n; i++) {
                    if (i > 1) vals.append(',');
                    vals.append(formatValue(rs, i, md.getColumnType(i)));
                }
                out.write("INSERT INTO `" + table + "` (" + cols + ") VALUES (" + vals + ");\n");
                rows++;
            }
        }
        if (rows == 0) {
            out.write("-- (sin filas)\n");
        }
        out.write("\n");
        System.out.println("  " + table + " -> " + rows + " filas");
    }

    private static String formatValue(ResultSet rs, int i, int sqlType) throws SQLException {
        Object v = rs.getObject(i);
        if (v == null || rs.wasNull()) return "NULL";
        switch (sqlType) {
            case Types.BIT:
            case Types.BOOLEAN:
                return rs.getBoolean(i) ? "1" : "0";
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return v.toString();
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB: {
                byte[] bytes = rs.getBytes(i);
                StringBuilder sb = new StringBuilder("X'");
                for (byte b : bytes) sb.append(String.format("%02X", b));
                return sb.append("'").toString();
            }
            default:
                return "'" + escape(v.toString()) + "'";
        }
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\'': sb.append("''"); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\0': sb.append("\\0"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
