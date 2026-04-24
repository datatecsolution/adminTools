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
 * Los errores se escriben en el directorio estandar por SO para evitar TCC en macOS:
 *   macOS:   ~/Library/Application Support/AdminTools/logs/admin_tools_YYYY-MM-DD.log
 *   Windows: %APPDATA%/AdminTools/logs/admin_tools_YYYY-MM-DD.log
 *   Linux:   $XDG_CONFIG_HOME/AdminTools/logs/ o ~/.config/AdminTools/logs/
 */
public final class AppLogger {

    private static boolean initialized = false;

    private AppLogger() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            File dir = directorioLogs();
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

    private static File directorioLogs() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");
        File base;
        if (os.contains("mac")) {
            base = new File(home, "Library/Application Support/AdminTools");
        } else if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            base = (appdata != null && !appdata.isEmpty())
                    ? new File(appdata, "AdminTools")
                    : new File(home, "AppData/Roaming/AdminTools");
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            base = (xdg != null && !xdg.isEmpty())
                    ? new File(xdg, "AdminTools")
                    : new File(home, ".config/AdminTools");
        }
        return new File(base, "logs");
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
