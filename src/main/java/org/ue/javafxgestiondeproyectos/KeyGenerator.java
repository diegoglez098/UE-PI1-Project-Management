package org.ue.javafxgestiondeproyectos;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Clave de entrada con longitud arbitraria
        String claveEntrada = "proyectoUE2024@";

        // Generar una clave válida para AES utilizando PBKDF2
        byte[] claveAES = generarClaveAES(claveEntrada);
        String claveAESBase64 = Base64.getEncoder().encodeToString(claveAES);
        System.out.println("Clave AES generada: " + claveAESBase64);
    }

    // Método para generar una clave válida para AES utilizando PBKDF2
    public static byte[] generarClaveAES(String claveEntrada) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algoritmoPBKDF2 = "PBKDF2WithHmacSHA256";
        int longitudClaveAES = 16; // Longitud de la clave AES en bytes (para AES-128)

        // Parámetros para PBKDF2
        int iteraciones = 10000; // Número de iteraciones
        byte[] salt = new byte[16]; // Salt (sal)

        // Generar clave utilizando PBKDF2
        KeySpec spec = new PBEKeySpec(claveEntrada.toCharArray(), salt, iteraciones, longitudClaveAES * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algoritmoPBKDF2);
        return factory.generateSecret(spec).getEncoded();
    }
}
