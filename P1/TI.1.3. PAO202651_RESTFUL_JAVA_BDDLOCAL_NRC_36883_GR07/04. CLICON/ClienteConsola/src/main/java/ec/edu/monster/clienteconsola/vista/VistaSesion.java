package ec.edu.monster.clienteconsola.vista;

import ec.edu.monster.clienteconsola.controlador.ControladorSesion;
import ec.edu.monster.clienteconsola.modelo.RespuestaServicio;

/** Pantalla de inicio y cierre de sesion (CU-01). */
public class VistaSesion {

    private final ControladorSesion controlador;
    private final ConsolaIO io;

    public VistaSesion(ControladorSesion controlador, ConsolaIO io) {
        this.controlador = controlador;
        this.io = io;
    }

    public void iniciarSesion() {
        io.seccion("Iniciar sesión");
        if (controlador.haySesion()) {
            io.linea("  Ya hay una sesión activa.");
            return;
        }
        String usuario = io.leer("Usuario: ");
        String clave = io.leer("Clave: ");
        if (usuario == null || clave == null) {
            return;
        }
        RespuestaServicio r = controlador.iniciarSesion(usuario, clave);
        if (r.esExitosa()) {
            io.ok(r.getMensaje());
        } else {
            io.error(r);
        }
    }

    public void cerrarSesion() {
        controlador.cerrarSesion();
        io.ok("Sesión cerrada.");
    }
}
