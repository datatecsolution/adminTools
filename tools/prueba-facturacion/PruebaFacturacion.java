import net.datatecsolution.admin_tools.modelo.*;
import net.datatecsolution.admin_tools.modelo.dao.FacturaDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Prueba de integracion del guardado de facturas (US-142 / US-144).
 *
 * POR QUE EXISTE: los tests de JUnit cubren logica pura y las pruebas SQL
 * cubren el trigger, pero el bug que motivo estas US vivia justo en el medio
 * — el SQL fallaba correctamente y el codigo Java se tragaba el error. Ese
 * pegamento no se puede ejercitar sin una base real, y esto lo hace sin
 * levantar la interfaz grafica.
 *
 * Ejercita FacturaDao.registrar() de verdad: transaccion, commit, rollback y
 * la liberacion del pedido de US-144.
 *
 * NO reemplaza a la suite: no corre en `gradlew build` a proposito. Necesita
 * una base viva y ESCRIBE en ella (aunque limpia lo que crea).
 *
 * Ver README.md para como ejecutarlo.
 */
public class PruebaFacturacion {

    /** Marca de todo lo que crea, para poder limpiarlo despues. */
    static final String MARCA = "PRUEBA_FACT";
    static final int PEDIDO_PRUEBA = 999501;

    static Connection admin;
    static int art, codigoCaja, bodega;
    static String cajaDb;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ConexionStatic.conectarBD();
        admin = ConexionStatic.getPoolConexion().getConnection();

        verificarDestino();
        resolverEntorno();
        ConexionStatic.setUsuarioLogin(usuarioDePrueba());

        System.out.println("entorno: caja=" + codigoCaja + " (" + cajaDb + ")  bodega=" + bodega
                + "  articulo=" + art);
        System.out.println();

        boolean ok = casoFacturaValida() & casoSinDisponible() & casoPedidoSeLibera();

        limpiar();
        System.out.println();
        System.out.println("RESULTADO: " + (ok ? "TODOS OK" : "HAY FALLOS"));
        System.exit(ok ? 0 : 1);
    }

    /**
     * Aborta si no estamos donde creemos estar.
     *
     * Si la lectura de connection.dat falla, ConexionStatic cae a
     * 127.0.0.1:3306 EN SILENCIO — sin error ni aviso. Sin esta guarda es
     * facil terminar escribiendo en la base equivocada (ya paso).
     *
     * OJO: el PUERTO no sirve para distinguir. Dentro de un contenedor MySQL
     * escucha en 3306 igual que una instalacion local; el 3307 (o el que sea)
     * es solo el extremo local del tunel SSH.
     *
     * Criterio: rechaza cualquier host que parezca una maquina de escritorio y
     * exige un volumen minimo de facturas (una base de pruebas real tiene
     * historia; una recien creada, no). El minimo se ajusta con
     * -Dprueba.minFacturas=N (default 1000).
     */
    static void verificarDestino() throws Exception {
        String host;
        long facturas;
        try (Statement s = admin.createStatement()) {
            ResultSet r = s.executeQuery("SELECT @@hostname");
            r.next();
            host = r.getString(1);
            r = s.executeQuery("SELECT COUNT(*) FROM admin_tools_caja_1.encabezado_factura");
            r.next();
            facturas = r.getLong(1);
        }
        long minimo = Long.getLong("prueba.minFacturas", 1000L);
        System.out.println("destino: " + host + "   facturas en caja_1: " + facturas
                + "   (minimo exigido: " + minimo + ")");

        String h = host.toLowerCase();
        boolean pareceEscritorio = h.contains("macbook") || h.contains("imac")
                || h.contains("laptop") || h.contains("pc-") || h.endsWith(".local");
        if (pareceEscritorio || facturas < minimo) {
            System.out.println();
            System.out.println("ABORTADO: no parece una base de pruebas.");
            System.out.println("  " + (pareceEscritorio
                    ? "el host parece una maquina de escritorio"
                    : "tiene menos de " + minimo + " facturas"));
            System.out.println("  NO se escribio nada.");
            System.exit(2);
        }
        System.out.println("(destino confirmado)");
        System.out.println();
    }

    // ------------------------------------------------------------------ casos

    /** US-142: la factura entra completa y el encabezado cuadra con el detalle. */
    static boolean casoFacturaValida() throws Exception {
        System.out.println("== CASO 1: factura valida (debe guardarse completa) ==");
        ponerExistencia(50);
        bloquearSobreventa();

        Factura f = facturaDe(2, 3);
        boolean res = new FacturaDao().registrar(f);
        int id = f.getIdFactura();

        int lineas = contar("SELECT COUNT(*) FROM " + cajaDb + ".detalle_factura WHERE numero_factura=" + id);
        BigDecimal enc = decimal("SELECT total FROM " + cajaDb + ".encabezado_factura WHERE numero_factura=" + id);
        BigDecimal det = decimal("SELECT IFNULL(SUM(total),0) FROM " + cajaDb + ".detalle_factura WHERE numero_factura=" + id);

        System.out.println("   registrar()=" + res + "   lineas=" + lineas + " (esperado 2)");
        System.out.println("   encabezado=" + enc + "   detalle=" + det);
        boolean ok = res && lineas == 2 && enc != null && enc.compareTo(det) == 0;
        System.out.println("   -> " + (ok ? "OK" : "FALLO"));
        borrarFactura(id);
        return ok;
    }

    /** US-142: si una linea es rechazada, no debe quedar NI el encabezado. */
    static boolean casoSinDisponible() throws Exception {
        System.out.println("== CASO 2: sin disponible (no debe quedar nada) ==");
        ponerExistencia(1);
        bloquearSobreventa();

        int antes = contar("SELECT COUNT(*) FROM " + cajaDb + ".encabezado_factura");
        Factura f = facturaDe(1, 5);   // pide 5, hay 1
        boolean res;
        try {
            res = new FacturaDao().registrar(f);
        } catch (java.awt.HeadlessException e) {
            // el rollback ya ocurrio; el JOptionPane no puede abrirse headless
            res = false;
            System.out.println("   (JOptionPane no disponible en headless: esperado)");
        }
        int despues = contar("SELECT COUNT(*) FROM " + cajaDb + ".encabezado_factura");

        System.out.println("   registrar()=" + res + "   encabezados antes=" + antes + " despues=" + despues);
        boolean ok = !res && antes == despues;
        System.out.println("   -> " + (ok ? "OK (rollback completo)" : "FALLO: quedo basura"));
        return ok;
    }

    /** US-144: el pedido se libera dentro de la transaccion y no se bloquea solo. */
    static boolean casoPedidoSeLibera() throws Exception {
        System.out.println("== CASO 3: pedido propio, todo el stock reservado por el ==");
        ponerExistencia(5);
        bloquearSobreventa();
        crearPedido(5);

        BigDecimal reservado = decimal("SELECT IFNULL(reservado,0) FROM v_reservado_por_articulo "
                + "WHERE codigo_articulo=" + art + " AND codigo_bodega=" + bodega);
        System.out.println("   reservado por el propio pedido: " + reservado + " (disponible quedaria en 0)");

        Factura f = facturaDe(1, 5);
        f.setPedidoOrigen(PEDIDO_PRUEBA);
        boolean res;
        try {
            res = new FacturaDao().registrar(f);
        } catch (java.awt.HeadlessException e) {
            res = false;
        }
        int estado = contar("SELECT estado FROM encabezado_factura_temp WHERE numero_factura=" + PEDIDO_PRUEBA);
        int lineas = f.getIdFactura() == null ? 0
                : contar("SELECT COUNT(*) FROM " + cajaDb + ".detalle_factura WHERE numero_factura=" + f.getIdFactura());

        System.out.println("   registrar()=" + res + "   estado del pedido=" + estado + " (3=facturado)"
                + "   lineas=" + lineas);
        boolean ok = res && estado == 3 && lineas == 1;
        System.out.println("   -> " + (ok ? "OK (no se bloqueo a si mismo)" : "FALLO"));
        if (f.getIdFactura() != null) borrarFactura(f.getIdFactura());
        return ok;
    }

    // -------------------------------------------------------------- utilidades

    static void resolverEntorno() throws Exception {
        try (Statement s = admin.createStatement();
             ResultSet r = s.executeQuery(
                     "SELECT codigo, nombre_db, codigo_bodega FROM cajas ORDER BY codigo LIMIT 1")) {
            r.next();
            codigoCaja = r.getInt(1);
            cajaDb = r.getString(2);
            bodega = r.getInt(3);
        }
        try (Statement s = admin.createStatement();
             ResultSet r = s.executeQuery("SELECT ak.codigo_articulo FROM articulo_kardex ak "
                     + "JOIN articulo a ON a.codigo_articulo=ak.codigo_articulo "
                     + "WHERE ak.codigo_bodega=" + bodega + " AND a.tipo_articulo=1 LIMIT 1")) {
            r.next();
            art = r.getInt(1);
        }
    }

    static Usuario usuarioDePrueba() {
        Departamento d = new Departamento();
        d.setId(bodega);
        d.setDescripcion("Bodega " + bodega);
        Caja c = new Caja();
        c.setCodigo(codigoCaja);
        c.setNombreBd(cajaDb);
        c.setDetartamento(d);
        c.setCodigoBodega(bodega);
        c.setActiva(true);
        Usuario u = new Usuario();
        u.setUser(MARCA);
        u.setCodigo(1);
        u.setCodigoEmpleado(1);
        u.setCajas(new ArrayList<>(Arrays.asList(c)));
        return u;
    }

    static Factura facturaDe(int lineas, double cantidad) throws Exception {
        Factura f = new Factura();
        Cliente cli = new Cliente();
        cli.setId(contar("SELECT MIN(codigo_cliente) FROM cliente"));
        cli.setNombre("PRUEBA");
        f.setCliente(cli);
        f.setTipoFactura(1);
        f.setTipoPago(1);
        f.setObservacion(MARCA);
        List<DetalleFactura> ds = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < lineas; i++) {
            Articulo a = new Articulo();
            a.setId(art);
            a.setArticulo("ART PRUEBA");
            a.setPrecioVenta(10);
            a.setTipoArticulo(1);
            DetalleFactura d = new DetalleFactura();
            d.setListArticulos(a);
            d.setCantidad(new BigDecimal(cantidad));
            d.setImpuesto(BigDecimal.ZERO);
            d.setSubTotal(new BigDecimal(10 * cantidad));
            d.setDescuentoItem(BigDecimal.ZERO);
            d.setTotal(new BigDecimal(10 * cantidad));
            ds.add(d);
            total = total.add(new BigDecimal(10 * cantidad));
        }
        f.setDetalles(ds);
        f.setSubTotal(total);
        f.setSubTotalExcento(total);
        f.setTotalImpuesto(BigDecimal.ZERO);
        f.setTotalImpuesto18(BigDecimal.ZERO);
        f.setTotal(total);
        f.setPago(total);
        f.setTotalDescuento(BigDecimal.ZERO);
        f.setCobroEfectivo(total);
        f.setCobroTarjeta(BigDecimal.ZERO);
        return f;
    }

    static void crearPedido(double cantidad) throws Exception {
        int cli = contar("SELECT MIN(codigo_cliente) FROM cliente");
        ejecutar("INSERT INTO encabezado_factura_temp(numero_factura,fecha,subtotal_excento,subtotal15,"
                + "subtotal18,subtotal,impuesto,total,codigo_cliente,estado_factura,usuario,tipo_factura,"
                + "descuento,codigo_vendedor,codigo_caja,estado) VALUES (" + PEDIDO_PRUEBA
                + ",NOW(),0,0,0,50,0,50," + cli + ",'ACT','" + MARCA + "',1,0,1," + codigoCaja + ",1)");
        ejecutar("INSERT INTO detalle_factura_temp(numero_factura,codigo_articulo,precio,cantidad,impuesto,"
                + "subtotal,descuento,total) VALUES (" + PEDIDO_PRUEBA + "," + art + ",10," + cantidad + ",0,50,0,50)");
    }

    /** Fija la existencia Y el ultimo saldo del kardex, que es lo que lee el SP. */
    static void ponerExistencia(double n) throws Exception {
        ejecutar("INSERT INTO existencia_articulo_bodega(codigo_articulo,codigo_bodega,cantidad) VALUES ("
                + art + "," + bodega + "," + n + ") ON DUPLICATE KEY UPDATE cantidad=" + n);
        int kdx = contar("SELECT codigo_kardex FROM articulo_kardex WHERE codigo_articulo=" + art
                + " AND codigo_bodega=" + bodega);
        ejecutar("INSERT INTO detalle_movimiento_kardex(codigo_kardex,fecha,descripcion,no_documento) VALUES ("
                + kdx + ",NOW(),'ajuste prueba','" + MARCA + "')");
        int mov = contar("SELECT LAST_INSERT_ID()");
        ejecutar("INSERT INTO movimiento_kardex(codigo_movimiento,codigo_tipo_movimiento,cantidad,precio_unidad,total) "
                + "VALUES (" + mov + ",3," + n + ",1," + n + ")");
    }

    static void bloquearSobreventa() throws Exception {
        ejecutar("INSERT INTO config_user_facturacion(usuario,facturar_sin_inventario) VALUES ('"
                + MARCA + "',0) ON DUPLICATE KEY UPDATE facturar_sin_inventario=0");
    }

    static void borrarFactura(int id) throws Exception {
        ejecutar("DELETE FROM " + cajaDb + ".detalle_factura WHERE numero_factura=" + id);
        ejecutar("DELETE FROM " + cajaDb + ".encabezado_factura WHERE numero_factura=" + id);
    }

    static void limpiar() throws Exception {
        ejecutar("DELETE FROM detalle_factura_temp WHERE numero_factura=" + PEDIDO_PRUEBA);
        ejecutar("DELETE FROM encabezado_factura_temp WHERE numero_factura=" + PEDIDO_PRUEBA);
        ejecutar("DELETE FROM config_user_facturacion WHERE usuario='" + MARCA + "'");
        ejecutar("DELETE mk FROM movimiento_kardex mk JOIN detalle_movimiento_kardex dmk "
                + "ON dmk.codigo_movimiento=mk.codigo_movimiento WHERE dmk.no_documento='" + MARCA + "'");
        ejecutar("DELETE FROM detalle_movimiento_kardex WHERE no_documento='" + MARCA + "'");
        System.out.println();
        System.out.println("(datos de prueba eliminados)");
    }

    static void ejecutar(String sql) throws Exception {
        try (Statement s = admin.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    static int contar(String sql) throws Exception {
        try (Statement s = admin.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getInt(1) : -1;
        }
    }

    static BigDecimal decimal(String sql) throws Exception {
        try (Statement s = admin.createStatement(); ResultSet r = s.executeQuery(sql)) {
            return r.next() ? r.getBigDecimal(1) : null;
        }
    }
}
