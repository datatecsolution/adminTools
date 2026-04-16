package net.datatecsolution.admin_tools.modelo;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aplica migraciones Flyway a la base común (admin_tools) y a cada base de caja
 * (admin_tools_caja_*). Se invoca una vez al arrancar, desde ConexionStatic.conectarBD().
 *
 * Convenciones:
 *  - Migraciones comunes: classpath:db/migration/common, tabla schema_version en admin_tools.
 *  - Migraciones de caja:  classpath:db/migration/caja,   tabla schema_version en cada admin_tools_caja_N.
 *
 * baselineOnMigrate=true permite adoptar clientes existentes sin re-ejecutar la V1.
 */
public class SchemaMigrator {

    private static final String COMMON_DB = "admin_tools";
    private static final String COMMON_LOCATION = "classpath:db/migration/common";
    private static final String CAJA_LOCATION = "classpath:db/migration/caja";

    private final String jdbcUrlTemplate;
    private final String user;
    private final String password;
    private final String driver;

    public SchemaMigrator(String jdbcUrlTemplate, String user, String password, String driver) {
        this.jdbcUrlTemplate = jdbcUrlTemplate;
        this.user = user;
        this.password = password;
        this.driver = driver;
    }

    public void migrateAll() {
        migrateCommon();
        for (String cajaDb : listCajaDatabases()) {
            migrateCaja(cajaDb);
        }
    }

    private void migrateCommon() {
        DataSource ds = buildDataSource(COMMON_DB);
        try {
            runFlyway(ds, COMMON_LOCATION, new HashMap<String, String>());
        } finally {
            closeQuietly(ds);
        }
    }

    private void migrateCaja(String cajaDb) {
        DataSource ds = buildDataSource(cajaDb);
        try {
            // En el arranque normal V1 ya está baselineada, los placeholders no se
            // evalúan para migraciones ya aplicadas. Pasamos valores por defecto
            // válidos sólo por seguridad ante V2+ que los usen.
            runFlyway(ds, CAJA_LOCATION, placeholders(cajaDb, 1));
        } finally {
            closeQuietly(ds);
        }
    }

    /**
     * Aplica la baseline y migraciones al crear una caja nueva desde la app.
     * Se invoca desde CajaDao.registrar() después de CREATE DATABASE.
     */
    public void migrateNewCajaDatabase(String nombreBd, int codigoBodega) {
        DataSource ds = buildDataSource(nombreBd);
        try {
            runFlyway(ds, CAJA_LOCATION, placeholders(nombreBd, codigoBodega));
        } finally {
            closeQuietly(ds);
        }
    }

    private Map<String, String> placeholders(String cajaDb, int codigoBodega) {
        Map<String, String> p = new HashMap<>();
        p.put("caja_db", cajaDb);
        p.put("codigo_bodega", String.valueOf(codigoBodega));
        return p;
    }

    private void runFlyway(DataSource ds, String location, Map<String, String> placeholders) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations(location)
                .table("schema_version")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .baselineDescription("baseline")
                .placeholders(placeholders)
                .load();
        flyway.repair();
        flyway.migrate();
    }

    private List<String> listCajaDatabases() {
        List<String> result = new ArrayList<>();
        DataSource ds = buildDataSource(COMMON_DB);
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT nombre_db FROM cajas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("nombre_db");
                if (name != null && !name.isEmpty()) {
                    result.add(name);
                }
            }
        } catch (Exception e) {
            System.err.println("SchemaMigrator: no se pudo listar cajas, se omiten migraciones de caja. "
                    + e.getMessage());
        } finally {
            closeQuietly(ds);
        }
        return result;
    }

    private DataSource buildDataSource(String dbName) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(String.format(jdbcUrlTemplate, dbName));
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setInitialSize(1);
        ds.setMaxIdle(2);
        return ds;
    }

    private void closeQuietly(DataSource ds) {
        if (ds instanceof BasicDataSource) {
            try {
                ((BasicDataSource) ds).close();
            } catch (Exception ignored) {
            }
        }
    }
}
