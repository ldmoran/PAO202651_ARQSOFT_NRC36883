package ec.edu.monster.eureka.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Hash de claves: SHA-256 de (sal + clave), en hexadecimal minuscula. */
public final class Claves {

    private Claves() {
    }

    public static String hash(String sal, String clave) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest((sal + clave).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Comparacion en tiempo constante. */
    public static boolean coincide(String sal, String clave, String hashGuardado) {
        return MessageDigest.isEqual(hash(sal, clave).getBytes(StandardCharsets.UTF_8),
                hashGuardado.getBytes(StandardCharsets.UTF_8));
    }
}
