package net.datatecsolution.admin_tools.config;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Properties;

public class Cifrado {
    private static final String CLAVE = "AdminTools2024@Sec";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ARCHIVO = "connection.dat";

    private final SecretKeySpec secretKey;
    private final IvParameterSpec iv;

    public Cifrado() {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(CLAVE.getBytes("UTF-8"));
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);

            byte[] ivBytes = Arrays.copyOfRange(sha.digest(key), 0, 16);
            iv = new IvParameterSpec(ivBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar cifrado", e);
        }
    }

    public static String getArchivo() {
        return resolverArchivo().getAbsolutePath();
    }

    public static boolean existeArchivo() {
        return resolverArchivo().exists();
    }

    private static File resolverArchivo() {
        try {
            File location = new File(Cifrado.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File dir = location.isFile() ? location.getParentFile() : location;
            File junto = new File(dir, ARCHIVO);
            File legacy = new File(ARCHIVO);
            if (!junto.exists() && legacy.exists()) {
                return legacy;
            }
            return junto;
        } catch (Exception e) {
            return new File(ARCHIVO);
        }
    }

    public void guardar(Properties datos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        datos.store(new OutputStreamWriter(baos, "UTF-8"), null);
        byte[] datosPlanos = baos.toByteArray();

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] datosCifrados = cipher.doFinal(datosPlanos);

        FileOutputStream fos = new FileOutputStream(resolverArchivo());
        try {
            fos.write(datosCifrados);
            fos.flush();
        } finally {
            fos.close();
        }
    }

    public Properties cargar() throws Exception {
        File archivo = resolverArchivo();
        if (!archivo.exists()) {
            return null;
        }

        byte[] datosCifrados = new byte[(int) archivo.length()];
        FileInputStream fis = new FileInputStream(archivo);
        try {
            int leidos = 0;
            while (leidos < datosCifrados.length) {
                int n = fis.read(datosCifrados, leidos, datosCifrados.length - leidos);
                if (n == -1) break;
                leidos += n;
            }
        } finally {
            fis.close();
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] datosPlanos = cipher.doFinal(datosCifrados);

        Properties props = new Properties();
        props.load(new InputStreamReader(new ByteArrayInputStream(datosPlanos), "UTF-8"));
        return props;
    }
}
