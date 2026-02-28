package net.datatecsolution;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import java.io.File;

public class DecompileAllJasper {
    public static void main(String[] args) {
        String[] files = {
                "recibo_pago",
                "ReporteExistencia",
                "devoluciones_venta",
                "encabezado_carta",
                "pago_caja",
                "proveedores_cuentas",
                "codigo_barra",
                "salida_caja",
                "cierres_caja",
                "proveedor_cuenta",
                "cierre_categorias"
        };

        try {
            for (String file : files) {
                System.out.println("Processing " + file + "...");
                File jasperFile = new File("src/main/resources/reportes/" + file + ".jasper");
                JasperReport report = (JasperReport) JRLoader.loadObject(jasperFile);
                JRXmlWriter.writeReport(report, "src/main/resources/reportes/" + file + "_recuperado.jrxml", "UTF-8");
            }
            System.out.println("¡Completado exitosamente!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
