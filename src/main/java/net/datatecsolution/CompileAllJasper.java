package net.datatecsolution;

import net.sf.jasperreports.engine.JasperCompileManager;
import java.io.File;
import java.io.FilenameFilter;

public class CompileAllJasper {
    public static void main(String[] args) {
        File folder = new File("src/main/resources/reportes");
        File[] listOfFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jrxml") && !name.contains("_recuperado");
            }
        });

        if (listOfFiles != null) {
            for (File jrxmlFile : listOfFiles) {
                String base = jrxmlFile.getName().replace(".jrxml", "");
                String destPath = "src/main/resources/reportes/" + base + ".jasper";
                System.out.println("Processing " + base + "...");
                try {
                    JasperCompileManager.compileReportToFile(jrxmlFile.getAbsolutePath(), destPath);
                } catch (Exception e) {
                    System.out.println("Error compiling " + base + ": " + e.getMessage());
                }
            }
            System.out.println("¡Completado exitosamente!");
        }
    }
}
