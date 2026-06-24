import net.datatecsolution.admin_tools.modelo.NumberToLetterConverter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Backfill de `total_letras` para las filas que tienen el bug de los CENTAVOS
 * (el viejo código redondeaba el total a entero antes de convertir, así que el
 * total en letras quedaba sin "CON XX CENTAVOS" y a veces con el entero mal).
 *
 * NO INVASIVO:
 *  - Solo toca las filas con el bug, vía filtro:
 *      total <> FLOOR(total)                    (el total tiene centavos)
 *      AND total_letras LIKE '%LEMPIRAS%'       (fue generado por la app)
 *      AND total_letras NOT LIKE '%CENTAVO%'    (pero le faltan los centavos)
 *    Las filas con solo el problema de ESPACIADO (que ya tienen sus centavos)
 *    NO se tocan.
 *  - UPDATE por PK (sin lock de tabla) en lotes con commit cada N → el sistema
 *    sigue facturando online.
 *  - Recalcula con el MISMO NumberToLetterConverter ya corregido.
 *  - DRY-RUN por defecto; escribe solo con APPLY=1.
 *
 * Tablas: encabezado_factura (cada caja registrada + común si tuviera filas),
 * encabezado_cotizacion / recibo_pago / recibo_pago_proveedores (común).
 *
 * Credenciales por env: BF_HOST BF_PORT(=3306) BF_USER BF_PASS
 */
public class BackfillTotalLetras {

    static final String FILTER =
            "total <> FLOOR(total) AND total_letras LIKE '%LEMPIRAS%' AND total_letras NOT LIKE '%CENTAVO%'";
    static final boolean APPLY = "1".equals(System.getenv("APPLY"));
    static final int BATCH = 200;

    public static void main(String[] args) throws Exception {
        String host = req("BF_HOST"), port = envOr("BF_PORT", "3306"),
               user = req("BF_USER"), pass = req("BF_PASS");
        String base = "jdbc:mysql://" + host + ":" + port + "/";
        String tail = "?serverTimezone=GMT-6&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
        Class.forName("com.mysql.cj.jdbc.Driver");

        System.out.println(APPLY
                ? "== MODO APLICAR (escribe en la BD) =="
                : "== DRY-RUN (no escribe; pasá APPLY=1 para aplicar) ==");

        int total = 0;
        try (Connection c = DriverManager.getConnection(base + "admin_tools" + tail, user, pass)) {
            // Tablas comunes
            total += fix(c, "admin_tools", "encabezado_factura", "numero_factura");
            total += fix(c, "admin_tools", "encabezado_cotizacion", "numero_cotizacion");
            total += fix(c, "admin_tools", "recibo_pago", "no_recibo");
            total += fix(c, "admin_tools", "recibo_pago_proveedores", "no_recibo");

            // encabezado_factura de cada caja REGISTRADA (las viejas sin registrar no se tocan)
            List<String> cajas = new ArrayList<>();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre_db FROM cajas")) {
                while (rs.next()) {
                    String n = rs.getString(1);
                    if (n != null && !n.isEmpty()) cajas.add(n);
                }
            }
            for (String caja : cajas) {
                try (Connection cc = DriverManager.getConnection(base + caja + tail, user, pass)) {
                    total += fix(cc, caja, "encabezado_factura", "numero_factura");
                }
            }
        }
        System.out.println("TOTAL filas " + (APPLY ? "actualizadas" : "a actualizar") + ": " + total);
    }

    static int fix(Connection c, String db, String table, String pk) throws SQLException {
        List<long[]> ids = new ArrayList<>();         // pk
        List<BigDecimal> totals = new ArrayList<>();  // total
        String sel = "SELECT `" + pk + "`, total FROM `" + table + "` WHERE " + FILTER;
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sel)) {
            while (rs.next()) {
                ids.add(new long[]{ rs.getLong(1) });
                totals.add(rs.getBigDecimal(2));
            }
        }
        if (ids.isEmpty()) { return 0; }

        // muestra (1 fila): total + lo nuevo
        String ej = totals.get(0).toPlainString() + " -> \""
                + NumberToLetterConverter.convertNumberToLetter(totals.get(0).doubleValue()) + "\"";

        if (!APPLY) {
            System.out.printf("  %-26s %d filas a corregir.  ej: %s%n", db + "." + table, ids.size(), ej);
            return ids.size();
        }

        String upd = "UPDATE `" + table + "` SET total_letras = ? WHERE `" + pk + "` = ?";
        int done = 0, inBatch = 0;
        boolean prev = c.getAutoCommit();
        c.setAutoCommit(false);
        try (PreparedStatement ps = c.prepareStatement(upd)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setString(1, NumberToLetterConverter.convertNumberToLetter(totals.get(i).doubleValue()));
                ps.setLong(2, ids.get(i)[0]);
                ps.addBatch();
                if (++inBatch >= BATCH) { ps.executeBatch(); c.commit(); done += inBatch; inBatch = 0; }
            }
            if (inBatch > 0) { ps.executeBatch(); c.commit(); done += inBatch; }
        } catch (SQLException e) {
            c.rollback(); throw e;
        } finally {
            c.setAutoCommit(prev);
        }
        System.out.printf("  %-26s %d actualizadas%n", db + "." + table, done);
        return done;
    }

    static String req(String k) {
        String v = System.getenv(k);
        if (v == null || v.isEmpty()) throw new IllegalStateException("Falta env " + k);
        return v;
    }
    static String envOr(String k, String d) {
        String v = System.getenv(k);
        return (v == null || v.isEmpty()) ? d : v;
    }
}
