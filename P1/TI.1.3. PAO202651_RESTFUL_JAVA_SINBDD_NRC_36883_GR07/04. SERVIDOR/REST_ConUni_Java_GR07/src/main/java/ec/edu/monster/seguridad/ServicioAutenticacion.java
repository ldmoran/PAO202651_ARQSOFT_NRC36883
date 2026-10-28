package ec.edu.monster.seguridad;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;

/** CU-01: Autenticarse. Un unico usuario, con credenciales en memoria. */
public final class ServicioAutenticacion {

    private static final String USUARIO = "monster";
    private static final String CLAVE = "monster9";

    private ServicioAutenticacion() {
    }

    public static RespuestaServicio autenticar(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES, "Usuario y clave son obligatorios");
        }
        if (USUARIO.equals(usuario.trim()) && CLAVE.equals(clave)) {
            return RespuestaMapeador.exitoLogin(GestorTokens.generarToken());
        }
        return RespuestaMapeador.error(CodigoResultado.CREDENCIALES_INCORRECTAS, "Usuario o clave incorrectos");
    }
}
