package net.datatecsolution.admin_tools.modelo;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genera un dump de esquema (tablas, vistas, funciones, procedimientos, triggers)
 * de una base MySQL, reutilizando la conexion configurada en ConexionStatic.
 *
 * Uso: ejecutar {@link #main(String[])}. Escribe dos archivos:
 *   - src/main/resources/db/migration/common/V1__baseline.sql (base admin_tools)
 *   - src/main/resources/db/migration/caja/V1__baseline.sql   (primera caja encontrada)
 *
 * La salida queda lista para usarse como baseline de Flyway. Si las bases ya tienen
 * datos, el archivo resultante solo contiene DDL (sin INSERTs) — es lo que queremos
 * para el baseline.
 */
public class SchemaDumper {

    private static final Pattern DEFINER = Pattern.compile("DEFINER=`[^`]*`@`[^`]*`\\s*");
    private static final Pattern AUTO_INCREMENT = Pattern.compile("\\s*AUTO_INCREMENT=\\d+");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        ConexionStatic.conectarBD();
        try (Connection conn = ConexionStatic.getPoolConexion().getConnection()) {
            Path commonOut = Paths.get("src/main/resources/db/migration/common/V1__baseline.sql");
            dump(conn, "admin_tools", commonOut);

            String primeraCaja = findFirstCajaDb(conn);
            if (primeraCaja != null) {
                Path cajaOut = Paths.get("src/main/resources/db/migration/caja/V1__baseline.sql");
                dump(conn, primeraCaja, cajaOut);
            } else {
                System.out.println("No se encontraron cajas en admin_tools.caja; baseline de caja no generado.");
            }
        }
        System.out.println("Dump completado.");
    }

    public static void dump(Connection conn, String dbName, Path out) throws Exception {
        if (out.getParent() != null) Files.createDirectories(out.getParent());
        int tablas, vistas, funciones, procedimientos, triggers;
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write("-- Baseline de `" + dbName + "`\n");
            w.write("-- Generado " + LocalDateTime.now().format(TS) + " por SchemaDumper\n");
            w.write("-- Fuente: conexion activa de ConexionStatic\n\n");
            w.write("SET FOREIGN_KEY_CHECKS=0;\n");
            w.write("SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO';\n");
            w.write("-- Requerido para que CREATE FUNCTION funcione cuando el binlog\n");
            w.write("-- esta activo y las funciones no declaran caracteristicas SQL.\n");
            w.write("SET @ADMIN_TOOLS_ORIG_LBT = @@GLOBAL.log_bin_trust_function_creators;\n");
            w.write("SET GLOBAL log_bin_trust_function_creators = 1;\n\n");

            tablas = dumpTables(conn, dbName, w);
            vistas = dumpViews(conn, dbName, w);
            funciones = dumpRoutines(conn, dbName, w, "FUNCTION");
            procedimientos = dumpRoutines(conn, dbName, w, "PROCEDURE");
            triggers = dumpTriggers(conn, dbName, w);

            w.write("SET GLOBAL log_bin_trust_function_creators = @ADMIN_TOOLS_ORIG_LBT;\n");
            w.write("SET FOREIGN_KEY_CHECKS=1;\n");
        }
        System.out.println("Baseline escrito: " + out.toAbsolutePath());
        System.out.println(String.format(
                "  tablas=%d  vistas=%d  funciones=%d  procedimientos=%d  triggers=%d",
                tablas, vistas, funciones, procedimientos, triggers));
    }

    private static int dumpTables(Connection conn, String dbName, BufferedWriter w) throws Exception {
        List<String> tables = listNames(conn,
                "SELECT table_name FROM information_schema.tables "
                        + "WHERE table_schema=? AND table_type='BASE TABLE' "
                        + "AND table_name NOT IN ('schema_version','flyway_schema_history') "
                        + "ORDER BY table_name",
                dbName);
        if (tables.isEmpty()) return 0;
        writeSection(w, "TABLAS", tables.size());
        for (String t : tables) {
            try (Statement s = conn.createStatement();
                 ResultSet r = s.executeQuery("SHOW CREATE TABLE `" + dbName + "`.`" + t + "`")) {
                if (r.next()) {
                    String sql = AUTO_INCREMENT.matcher(r.getString(2)).replaceAll("");
                    w.write(sql);
                    w.write(";\n\n");
                }
            }
        }
        return tables.size();
    }

    private static int dumpViews(Connection conn, String dbName, BufferedWriter w) throws Exception {
        List<String> views = listNames(conn,
                "SELECT table_name FROM information_schema.views WHERE table_schema=? ORDER BY table_name",
                dbName);
        if (views.isEmpty()) return 0;
        Map<String, String> defs = new LinkedHashMap<>();
        for (String v : views) {
            try (Statement s = conn.createStatement();
                 ResultSet r = s.executeQuery("SHOW CREATE VIEW `" + dbName + "`.`" + v + "`")) {
                if (r.next()) {
                    defs.put(v, DEFINER.matcher(r.getString(2)).replaceAll(""));
                }
            }
        }
        List<String> ordered = topoSort(views, defs);
        writeSection(w, "VISTAS", ordered.size());
        for (String v : ordered) {
            w.write(defs.get(v));
            w.write(";\n\n");
        }
        return ordered.size();
    }

    private static int dumpRoutines(Connection conn, String dbName, BufferedWriter w, String type) throws Exception {
        List<String> routines = listNamesWithParam(conn,
                "SELECT routine_name FROM information_schema.routines "
                        + "WHERE routine_schema=? AND routine_type=? ORDER BY routine_name",
                dbName, type);
        if (routines.isEmpty()) return 0;
        Map<String, String> defs = new LinkedHashMap<>();
        for (String name : routines) {
            try (Statement s = conn.createStatement();
                 ResultSet r = s.executeQuery("SHOW CREATE " + type + " `" + dbName + "`.`" + name + "`")) {
                if (r.next()) {
                    String sql = findCreateColumn(r);
                    if (sql == null) continue;
                    defs.put(name, DEFINER.matcher(sql).replaceAll(""));
                }
            }
        }
        List<String> ordered = topoSort(routines, defs);
        writeSection(w, type + "S", ordered.size());
        for (String name : ordered) {
            w.write("DELIMITER $$\n");
            w.write(defs.get(name));
            w.write("$$\nDELIMITER ;\n\n");
        }
        return ordered.size();
    }

    /**
     * Orden topologico: si la definicion del objeto A referencia el nombre del objeto
     * B (como identificador bounded), B debe crearse antes que A. Rompe ciclos
     * ignorando la arista reversa (MySQL no permite recursion real en views/routines
     * sin WITH RECURSIVE, asi que en la practica no hay ciclos reales).
     */
    private static List<String> topoSort(List<String> names, Map<String, String> defs) {
        Map<String, Set<String>> deps = new LinkedHashMap<>();
        for (String name : names) {
            Set<String> d = new HashSet<>();
            String body = defs.get(name);
            if (body != null) {
                String lower = body.toLowerCase();
                for (String other : names) {
                    if (other.equals(name)) continue;
                    if (containsIdentifier(lower, other.toLowerCase())) {
                        d.add(other);
                    }
                }
            }
            deps.put(name, d);
        }
        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        for (String n : names) {
            visit(n, deps, visited, visiting, result);
        }
        return result;
    }

    private static void visit(String n, Map<String, Set<String>> deps, Set<String> visited,
                              Set<String> visiting, List<String> result) {
        if (visited.contains(n) || visiting.contains(n)) return;
        visiting.add(n);
        Set<String> d = deps.get(n);
        if (d != null) {
            for (String dep : d) {
                if (deps.containsKey(dep)) visit(dep, deps, visited, visiting, result);
            }
        }
        visiting.remove(n);
        visited.add(n);
        result.add(n);
    }

    private static boolean containsIdentifier(String haystack, String needle) {
        Pattern p = Pattern.compile("(?<![\\p{Alnum}_])" + Pattern.quote(needle) + "(?![\\p{Alnum}_])");
        Matcher m = p.matcher(haystack);
        return m.find();
    }

    private static int dumpTriggers(Connection conn, String dbName, BufferedWriter w) throws Exception {
        List<String> triggers = listNames(conn,
                "SELECT trigger_name FROM information_schema.triggers WHERE trigger_schema=? ORDER BY trigger_name",
                dbName);
        if (triggers.isEmpty()) return 0;
        writeSection(w, "TRIGGERS", triggers.size());
        for (String t : triggers) {
            try (Statement s = conn.createStatement();
                 ResultSet r = s.executeQuery("SHOW CREATE TRIGGER `" + dbName + "`.`" + t + "`")) {
                if (r.next()) {
                    String sql = findCreateColumn(r);
                    if (sql == null) continue;
                    sql = DEFINER.matcher(sql).replaceAll("");
                    w.write("DELIMITER $$\n");
                    w.write(sql);
                    w.write("$$\nDELIMITER ;\n\n");
                }
            }
        }
        return triggers.size();
    }

    private static void writeSection(BufferedWriter w, String titulo, int count) throws Exception {
        w.write("-- ============================================\n");
        w.write("-- " + titulo + " (" + count + ")\n");
        w.write("-- ============================================\n\n");
    }

    private static List<String> listNames(Connection conn, String sql, String p1) throws Exception {
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    private static List<String> listNamesWithParam(Connection conn, String sql, String p1, String p2) throws Exception {
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p1);
            ps.setString(2, p2);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    private static String findCreateColumn(ResultSet r) throws Exception {
        ResultSetMetaData md = r.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String label = md.getColumnLabel(i);
            if (label == null) continue;
            String lower = label.toLowerCase();
            if (lower.startsWith("create ") || lower.contains("sql original")) {
                return r.getString(i);
            }
        }
        return null;
    }

    private static String findFirstCajaDb(Connection conn) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT nombre_db FROM cajas WHERE nombre_db IS NOT NULL AND nombre_db <> '' LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        }
        return null;
    }
}
