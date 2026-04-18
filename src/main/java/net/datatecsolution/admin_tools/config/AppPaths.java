package net.datatecsolution.admin_tools.config;

import java.io.File;
import java.net.URISyntaxException;

public final class AppPaths {

    private static final File BASE_DIR;

    static {
        File dir;
        try {
            File jarFile = new File(AppPaths.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            dir = jarFile.isFile() ? jarFile.getParentFile() : jarFile;
        } catch (URISyntaxException e) {
            dir = new File(".");
        }
        BASE_DIR = dir;
    }

    private AppPaths() {}

    public static File resolve(String relativePath) {
        return new File(BASE_DIR, relativePath);
    }

    public static File getBaseDir() {
        return BASE_DIR;
    }
}
