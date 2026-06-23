import net.datatecsolution.admin_tools.modelo.SchemaMigrator;

/**
 * Runner de ensayo: aplica las migraciones Flyway a la copia efímera reusando el
 * MISMO {@link SchemaMigrator} de producción (baselineOnMigrate=true,
 * baselineVersion=1, table=schema_version, outOfOrder=true, repair()+migrate()).
 * Descubre las cajas desde {@code admin_tools.cajas} igual que el arranque real.
 *
 * Credenciales por variable de entorno (nunca a disco):
 *   ENS_HOST ENS_PORT ENS_USER ENS_PASS
 *
 * Classpath: el fat-jar del Swing (build/libs/AdminTools-1.0.jar) — trae
 * SchemaMigrator + Flyway 6.5.7 + driver MySQL + las migraciones .sql y las
 * Java (V3/V4/V5). Por eso NO se usa Flyway CLI.
 */
public class EnsayoMigrate {
    public static void main(String[] args) {
        String host = req("ENS_HOST");
        String port = req("ENS_PORT");
        String user = req("ENS_USER");
        String pass = req("ENS_PASS");
        String urlTpl = "jdbc:mysql://" + host + ":" + port
                + "/%s?serverTimezone=GMT-6&useSSL=false&allowPublicKeyRetrieval=true";
        long t0 = System.currentTimeMillis();
        new SchemaMigrator(urlTpl, user, pass, "com.mysql.cj.jdbc.Driver").migrateAll();
        System.out.printf("ENSAYO_MIGRATE_OK (%.1fs)%n", (System.currentTimeMillis() - t0) / 1000.0);
    }

    private static String req(String k) {
        String v = System.getenv(k);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Falta la variable de entorno " + k);
        }
        return v;
    }
}
