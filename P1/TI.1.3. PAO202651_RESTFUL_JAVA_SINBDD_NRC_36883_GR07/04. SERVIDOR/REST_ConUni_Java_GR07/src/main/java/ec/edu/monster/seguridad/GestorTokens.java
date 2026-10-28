package ec.edu.monster.seguridad;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tokens activos en memoria (sin base de datos). Cubre CU-05 (validar token),
 * CU-08 (token invalido) y CU-09 (sesion expirada).
 *
 * La vigencia por defecto es de 30 minutos y se puede cambiar con la propiedad
 * del sistema "conuni.token.minutos" (acepta decimales, por ejemplo 0.01).
 */
public final class GestorTokens {

    private static final Map<String, Long> ACTIVOS = new ConcurrentHashMap<>();

    private GestorTokens() {
    }

    private static long vigenciaMs() {
        String valor = System.getProperty("conuni.token.minutos", "30");
        try {
            return (long) (Double.parseDouble(valor) * 60_000L);
        } catch (NumberFormatException e) {
            return 30L * 60_000L;
        }
    }

    /** Genera un token unico y lo guarda con su fecha de expiracion. */
    public static String generarToken() {
        String token = UUID.randomUUID().toString();
        ACTIVOS.put(token, System.currentTimeMillis() + vigenciaMs());
        return token;
    }

    /**
     * Valida un token.
     *
     * @return null si es valido; TOKEN_INVALIDO (AUTH-001) si es nulo,
     *         inexistente o alterado; SESION_EXPIRADA (AUTH-002) si existio
     *         pero ya expiro (y lo elimina).
     */
    public static CodigoResultado validar(String token) {
        if (token == null || token.isBlank()) {
            return CodigoResultado.TOKEN_INVALIDO;
        }
        Long expira = ACTIVOS.get(token.trim());
        if (expira == null) {
            return CodigoResultado.TOKEN_INVALIDO;
        }
        if (System.currentTimeMillis() > expira) {
            ACTIVOS.remove(token.trim());
            return CodigoResultado.SESION_EXPIRADA;
        }
        return null;
    }

    /** Respuesta de error para un token que no sirve, o null si el token es valido. */
    public static RespuestaServicio verificar(String token) {
        CodigoResultado codigo = validar(token);
        if (codigo == null) {
            return null;
        }
        if (codigo == CodigoResultado.SESION_EXPIRADA) {
            return RespuestaMapeador.error(codigo, "Sesión expirada");
        }
        return RespuestaMapeador.error(codigo, "Token inválido");
    }
}
