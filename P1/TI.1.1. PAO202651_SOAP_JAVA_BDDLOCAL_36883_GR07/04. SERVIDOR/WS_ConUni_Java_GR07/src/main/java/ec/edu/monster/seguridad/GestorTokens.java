package ec.edu.monster.seguridad;

import ec.edu.monster.datos.Datos;
import ec.edu.monster.datos.SesionRepositorio;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Tokens de sesion guardados en la tabla SESION. Cubre CU-05 (validar token),
 * CU-08 (token invalido) y CU-09 (sesion expirada).
 *
 * La vigencia por defecto es de 30 minutos y se puede cambiar con la propiedad
 * del sistema "conuni.token.minutos" (acepta decimales, por ejemplo 0.01).
 */
public final class GestorTokens {

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

    /** Genera un token unico para el usuario y lo guarda con su fecha de expiracion. */
    public static String generarToken(int idUsuario) {
        String token = UUID.randomUUID().toString();
        Instant ahora = Datos.reloj().instant();
        new SesionRepositorio(Datos.fuente()).crear(token, idUsuario, ahora, ahora.plusMillis(vigenciaMs()));
        return token;
    }

    /**
     * Valida un token.
     *
     * @return null si es valido; TOKEN_INVALIDO (AUTH-001) si es nulo, inexistente,
     *         alterado o ya cerrado; SESION_EXPIRADA (AUTH-002) si existia pero ya
     *         expiro (y la cierra en la base).
     */
    public static CodigoResultado validar(String token) {
        if (token == null || token.isBlank()) {
            return CodigoResultado.TOKEN_INVALIDO;
        }
        String limpio = token.trim();
        SesionRepositorio sesiones = new SesionRepositorio(Datos.fuente());
        Optional<SesionRepositorio.Sesion> sesion = sesiones.buscar(limpio);
        if (sesion.isEmpty() || !sesion.get().activa()) {
            return CodigoResultado.TOKEN_INVALIDO;
        }
        if (Datos.reloj().instant().isAfter(sesion.get().expiracion())) {
            sesiones.desactivar(limpio);
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
