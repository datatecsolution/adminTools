package net.datatecsolution.admin_tools.modelo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Redirige System.err a un archivo de log para capturar todos los
 * printStackTrace() existentes sin modificar los ~1100 catch blocks.
 * Tambien captura excepciones no manejadas (uncaught).
 *
 * Uso: llamar AppLogger.init() al inicio de main(), antes de cualquier otra cosa.
 * Los errores se escriben en: logs/admin_tools_YYYY-MM-DD.log
 */
public final class AppLogger {

    private static final String LOG_DIR = "logs";
    private static boolean initialized = false;

    private AppLogger() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            File logFile = new File(dir, "admin_tools_" + fecha + ".log");

            FileOutputStream fos = new FileOutputStream(logFile, true);

            PrintStream logStream = new PrintStream(new TeeOutputStream(System.err, fos), true);
            System.setErr(logStream);

            Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                System.err.println("=== UNCAUGHT EXCEPTION en thread [" + thread.getName() + "] ===");
                ex.printStackTrace(System.err);
                System.err.println("=== FIN UNCAUGHT ===");
            });

            System.err.println("=== LOG INICIADO: " + new Date() + " ===");
            System.err.println("=== Archivo: " + logFile.getAbsolutePath() + " ===");

        } catch (IOException e) {
            System.err.println("No se pudo inicializar el log a archivo: " + e.getMessage());
        }
    }

    /**
     * OutputStream que escribe a dos destinos simultaneamente:
     * la consola original (para que el dev siga viendo errores)
     * y el archivo de log.
     */
    private static class TeeOutputStream extends OutputStream {
        private final OutputStream console;
        private final OutputStream file;

        TeeOutputStream(OutputStream console, OutputStream file) {
            this.console = console;
            this.file = file;
        }

        @Override
        public void write(int b) throws IOException {
            console.write(b);
            file.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            console.write(b, off, len);
            file.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            console.flush();
            file.flush();
        }

        @Override
        public void close() throws IOException {
            file.flush();
            file.close();
        }
    }
}
