package net.datatecsolution.admin_tools.config;

import java.util.Properties;

public class pruebaCifrado {
    public static void main(String[] args) {
        String clave = "clave_secreta";

        // Creamos el objeto Cifrado con la clave proporcionada
        Cifrado cifrado = new Cifrado();
/*
        // Creamos un objeto Properties para guardar los datos de conexión
        Properties datos = new Properties();
        datos.setProperty("host", "localhost");
        datos.setProperty("usuario", "root");
        datos.setProperty("pwd", "123456");

        // Guardamos los datos cifrados en un archivo
        try {
            cifrado.guardarDatosCifrados(datos);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        // Recuperamos los datos cifrados desde el archivo
        Properties datosRecuperados = null;
        try {
            datosRecuperados = cifrado.recuperarDatosCifrados();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Imprimimos los datos recuperados
        System.out.println("Host: " + datosRecuperados.getProperty("host"));
        System.out.println("Usuario: " + datosRecuperados.getProperty("usuario"));
        System.out.println("Contraseña: " + datosRecuperados.getProperty("pwd"));
        System.out.println("Base de datos: " + datosRecuperados.getProperty("dataBase"));

    }
}
