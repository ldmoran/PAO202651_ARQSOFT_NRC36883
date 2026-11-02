package ec.edu.monster.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Guarda y compara claves como SHA-256(sal + clave) en hexadecimal; la clave en claro nunca se almacena. */
public final class Claves {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Claves() {
    }

    /** Sal nueva de 16 bytes (32 caracteres hexadecimales). */
    public static String generarSal() {
        byte[] bytes = new byte[16];
        ALEATORIO.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String hash(String sal, String clave) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest((sal + clave).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no esta disponible", e);
        }
    }

    /** Compara en tiempo constante el hash guardado con el de la clave escrita. */
    public static boolean coincide(String clave, String sal, String hashGuardado) {
        return MessageDigest.isEqual(
                hash(sal, clave).getBytes(StandardCharsets.UTF_8),
                hashGuardado.getBytes(StandardCharsets.UTF_8));
    }
}
