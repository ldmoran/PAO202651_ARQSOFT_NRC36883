package ec.edu.monster.eureka.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash de claves: SHA-256 de (sal + clave) en hexadecimal minuscula. La clave en claro nunca se guarda. */
public final class Claves {

    private Claves() {
    }

    public static String hash(String sal, String clave) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest((sal + clave).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean coincide(String sal, String clave, String hashGuardado) {
        return MessageDigest.isEqual(hash(sal, clave).getBytes(StandardCharsets.UTF_8), hashGuardado.getBytes(StandardCharsets.UTF_8));
    }
}
