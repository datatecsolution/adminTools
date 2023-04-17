package net.datatecsolution.admin_tools.config;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Properties;

public class Cifrado {
    private static final String clave = "Jfmp123."; // Clave secreta para cifrar/descifrar
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final byte[] IV = new byte[16]; // Vector de inicialización, puede ser aleatorio
    private static final String ARCHIVO_CLAVE = System.getProperty("user.dir") + File.separator + "config.cfg";;

    private SecretKey secretKey;
    private IvParameterSpec ivParameterSpec;

    public Cifrado() {
        try {
            // Generamos la clave secreta a partir de la clave proporcionada
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(clave.getBytes("UTF-8"));
            key = Arrays.copyOf(key, 16); // Solo necesitamos los primeros 128 bits
            secretKey = new SecretKeySpec(key, ALGORITHM);

            // Creamos el vector de inicialización
            ivParameterSpec = new IvParameterSpec(IV);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void guardarDatosCifrados(Properties datos) throws Exception{
        // Crear el archivo de clave si no existe
        File archivo = new File(ARCHIVO_CLAVE);
        if (!archivo.exists()) {
            archivo.createNewFile();
        }

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            FileOutputStream fos = new FileOutputStream(archivo);
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            datos.store(cos, "Datos de conexión a la base de datos cifrados");
            cos.close();
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Método para recuperar los datos cifrados desde un archivo y decodificarlos
    public Properties recuperarDatosCifrados()throws Exception {
        // Verificar si el archivo de clave existe
        File archivo = new File(ARCHIVO_CLAVE);
        if (!archivo.exists()) {
            return null;
        }
        Properties datos = new Properties();
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            FileInputStream fis = new FileInputStream(archivo);
            CipherInputStream cis = new CipherInputStream(fis, cipher);
            datos.load(cis);
            cis.close();
            fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return datos;
    }
}
