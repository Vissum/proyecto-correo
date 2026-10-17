package com.mycompany.backend.correo.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 210000;
    private static final int LONGITUD_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    // Formato: pbkdf2$iteraciones$sal$hash
    public static String hash(String contrasena) {
        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);
        byte[] hash = derivar(contrasena.toCharArray(), sal, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean esHashPbkdf2(String almacenado) {
        return almacenado != null && almacenado.startsWith(PREFIJO + "$");
    }

    public static boolean verificar(String contrasena, String almacenado) {
        if (contrasena == null || almacenado == null) {
            return false;
        }
        // Cuentas creadas antes de este cambio: texto plano. Se migran al iniciar sesión.
        if (!esHashPbkdf2(almacenado)) {
            return MessageDigest.isEqual(
                    contrasena.getBytes(StandardCharsets.UTF_8),
                    almacenado.getBytes(StandardCharsets.UTF_8));
        }
        try {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 4) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(contrasena.toCharArray(), sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] contrasena, byte[] sal, int iteraciones) {
        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return fabrica.generateSecret(
                    new PBEKeySpec(contrasena, sal, iteraciones, LONGITUD_BITS)).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", e);
        }
    }
}