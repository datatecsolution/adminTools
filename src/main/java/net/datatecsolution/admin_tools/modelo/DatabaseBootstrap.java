package net.datatecsolution.admin_tools.modelo;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper standalone para fresh install. Crea la base común {@code admin_tools},
 * aplica su baseline Flyway y carga el seed de datos base (si existe).
 *
 * Las bases por caja ({@code admin_tools_caja_*}) NO se crean aquí: el app las crea
 * dinámicamente al registrar cada caja desde la UI (ver {@code CajaDao.registrar}),
 * y en ese mismo paso aplica la baseline Flyway parametrizada.
 *
 * Uso:
 *   ./gradlew bootstrapDatabases
 *
 * Después de correrlo: arrancar el app y registrar al menos una caja desde la UI.
 */
public class DatabaseBootstrap {

    private static final String COMMON_DB = "admin_tools";
    private static final String SEED_RESOURCE = "/db/seed/common.sql";

    public static void main(String[] args) throws Exception {
        String user = ConexionStatic.getLogin();
        String password = ConexionStatic.getPassword();
        String driver = ConexionStatic.getDriver();
        String urlTemplate = ConexionStatic.getUrlTemplate();
        String serverUrl = String.format(urlTemplate, "mysql");
        String dbUrl = String.format(urlTemplate, COMMON_DB);

        System.out.println("Bootstrap local en " + serverUrl);

        createDatabase(serverUrl, user, password, driver, COMMON_DB);

        System.out.println("\nAplicando baseline Flyway a " + COMMON_DB + "...");
        boolean wasFreshInstall = migrateCommon(dbUrl, user, password, driver);

        if (wasFreshInstall) {
            System.out.println("\nAplicando seed de datos base...");
            applySeed(dbUrl, user, password, driver);
        } else {
            System.out.println("\nBase con datos pre-existentes: seed omitido.");
        }

        System.out.println("\nBootstrap completado.");
        System.out.println("Siguiente paso: arrancar el app y registrar al menos una caja");
        System.out.println("                desde la UI. El app creará la base admin_tools_caja_N");
        System.out.println("                y aplicará su baseline Flyway automáticamente.");
    }

    private static void createDatabase(String serverUrl, String user, String password, String driver,
                                       String dbName) throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(serverUrl);
        ds.setUsername(user);
        ds.setPassword(password);
        try (Connection conn = ds.getConnection();
             Statement s = conn.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName
                    + "` CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci");
            System.out.println("  CREATE DATABASE IF NOT EXISTS " + dbName);
        } finally {
            ds.close();
        }
    }

    /**
     * Aplica common/V1__baseline.sql. Devuelve true si la base estaba vacia y
     * Flyway realmente ejecuto V1 (fresh install), false si ya existia schema y
     * Flyway solo marco el baseline.
     */
    private static boolean migrateCommon(String dbUrl, String user, String password, String driver)
            throws Exception {
        boolean hadTablesBefore = countTables(dbUrl, user, password, driver) > 0;

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(dbUrl);
        ds.setUsername(user);
        ds.setPassword(password);
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource((DataSource) ds)
                    .locations("classpath:db/migration/common")
                    .table("schema_version")
                    .baselineOnMigrate(true)
                    .baselineVersion("1")
                    .baselineDescription("baseline")
                    .load();
            flyway.migrate();
        } finally {
            try { ds.close(); } catch (Exception ignored) {}
        }
        return !hadTablesBefore;
    }

    private static int countTables(String dbUrl, String user, String password, String driver)
            throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(dbUrl);
        ds.setUsername(user);
        ds.setPassword(password);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM information_schema.tables " +
                             "WHERE table_schema = ? AND table_type = 'BASE TABLE'")) {
            ps.setString(1, COMMON_DB);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } finally {
            ds.close();
        }
    }

    private static void applySeed(String dbUrl, String user, String password, String driver)
            throws Exception {
        InputStream is = DatabaseBootstrap.class.getResourceAsStream(SEED_RESOURCE);
        if (is == null) {
            System.out.println("  (sin " + SEED_RESOURCE + " en el classpath, seed omitido)");
            return;
        }
        List<String> statements;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            statements = splitStatements(br);
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(dbUrl);
        ds.setUsername(user);
        ds.setPassword(password);
        int applied = 0;
        try (Connection c = ds.getConnection();
             Statement s = c.createStatement()) {
            for (String stmt : statements) {
                s.execute(stmt);
                applied++;
            }
        } finally {
            ds.close();
        }
        System.out.println("  seed aplicado: " + applied + " sentencias.");
    }

    /**
     * Split simple del archivo de seed por ';' al final de linea. El seed generado
     * por SeedDumper es una lista de INSERT de una sola linea + SET de una sola
     * linea, asi que alcanza con este split. Las lineas de comentario ('--') se
     * descartan.
     */
    private static List<String> splitStatements(BufferedReader br) throws Exception {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String stmt = current.toString().trim();
                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length() - 1);
                }
                if (!stmt.isEmpty()) {
                    out.add(stmt);
                }
                current.setLength(0);
            }
        }
        return out;
    }
}
