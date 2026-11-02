package ec.edu.monster.clientemovil.controlador;

/**
 * Guarda en memoria el controlador de la sesion para compartirlo entre pantallas.
 * El token vive solo mientras la aplicacion esta abierta.
 */
public final class SesionApp {

    private static ControladorConversiones controlador;

    private SesionApp() {
    }

    /** Crea un controlador nuevo apuntando a la URL indicada. */
    public static synchronized ControladorConversiones iniciar(String url) {
        controlador = ControladorConversiones.conectar(url);
        return controlador;
    }

    public static synchronized ControladorConversiones actual() {
        return controlador;
    }

    public static synchronized boolean haySesion() {
        return controlador != null && controlador.haySesion();
    }

    public static synchronized void cerrar() {
        if (controlador != null) {
            controlador.cerrarSesion();
        }
        controlador = null;
    }
}
