package ec.edu.monster.seguridad;

import ec.edu.monster.datos.AccesoRepositorio;
import ec.edu.monster.datos.Datos;
import ec.edu.monster.datos.UsuarioRepositorio;
import ec.edu.monster.modelos.entidades.Usuario;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import java.util.Optional;

/**
 * CU-01: Autenticarse. Las credenciales viven en la tabla USUARIO (clave con sal y SHA-256) y
 * cada intento, correcto o no, queda en la bitacora INTENTO_ACCESO.
 */
public final class ServicioAutenticacion {

    private ServicioAutenticacion() {
    }

    public static RespuestaServicio autenticar(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES, "Usuario y clave son obligatorios");
        }
        String nombre = usuario.trim();
        Optional<Usuario> encontrado = new UsuarioRepositorio(Datos.fuente()).buscarActivo(nombre)
                .filter(u -> u.nombre().equals(nombre));
        AccesoRepositorio bitacora = new AccesoRepositorio(Datos.fuente());
        if (encontrado.isEmpty()) {
            bitacora.registrar(null, nombre, false, "Usuario inexistente", Datos.reloj().instant());
            return RespuestaMapeador.error(CodigoResultado.CREDENCIALES_INCORRECTAS, "Usuario o clave incorrectos");
        }
        Usuario u = encontrado.get();
        if (!Claves.coincide(clave, u.sal(), u.hashClave())) {
            bitacora.registrar(u.id(), nombre, false, "Clave incorrecta", Datos.reloj().instant());
            return RespuestaMapeador.error(CodigoResultado.CREDENCIALES_INCORRECTAS, "Usuario o clave incorrectos");
        }
        bitacora.registrar(u.id(), nombre, true, "Inicio de sesion correcto", Datos.reloj().instant());
        return RespuestaMapeador.exitoLogin(GestorTokens.generarToken(u.id()));
    }
}
