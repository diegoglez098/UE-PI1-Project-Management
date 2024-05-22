package org.ue.javafxgestiondeproyectos;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class AES {
    public static void main() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        // Clave secreta para el cifrado y descifrado
        String clave = "proyectoUE2024@";
        byte[] claveAES = KeyGenerator.generarClaveAES(clave);
        String claveAESBase64 = Base64.getEncoder().encodeToString(claveAES);
        // Mensaje a cifrar
        String mensajeOriginal = "Contraseña123@";

        // Cifrado
        String mensajeCifrado = cifrarAES(mensajeOriginal, claveAESBase64);
        System.out.println("Mensaje cifrado: " + mensajeCifrado);

        // Descifrado
        String mensajeDescifrado = descifrarAES(mensajeCifrado, claveAESBase64);
        System.out.println("Mensaje descifrado: " + mensajeDescifrado);
    }

    // Método para cifrar un mensaje utilizando AES
    private static String cifrarAES(String mensaje, String clave) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifrador = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec claveSecreta = new SecretKeySpec(clave.getBytes(), "AES");
        cifrador.init(Cipher.ENCRYPT_MODE, claveSecreta);
        byte[] bytesCifrados = cifrador.doFinal(mensaje.getBytes());
        return Base64.getEncoder().encodeToString(bytesCifrados);
    }

    // Método para descifrar un mensaje utilizando AES
    private static String descifrarAES(String mensajeCifrado, String clave) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifrador = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec claveSecreta = new SecretKeySpec(clave.getBytes(), "AES");
        cifrador.init(Cipher.DECRYPT_MODE, claveSecreta);
        byte[] bytesDescifrados = cifrador.doFinal(Base64.getDecoder().decode(mensajeCifrado));
        return new String(bytesDescifrados);
    }
}
