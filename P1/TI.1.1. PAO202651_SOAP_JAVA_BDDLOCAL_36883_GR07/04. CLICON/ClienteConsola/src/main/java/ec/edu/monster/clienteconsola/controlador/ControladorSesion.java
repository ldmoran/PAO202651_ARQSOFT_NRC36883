package ec.edu.monster.clienteconsola.controlador;

import ec.edu.monster.clienteconsola.modelo.RespuestaServicio;
import ec.edu.monster.clienteconsola.modelo.Sesion;
import ec.edu.monster.clienteconsola.modelo.SoapClient;

/** Controlador de la sesion (CU-01): iniciar y cerrar sesion contra WSLogin. */
public class ControladorSesion {

    private final SoapClient cliente;
    private final Sesion sesion;

    public ControladorSesion(SoapClient cliente, Sesion sesion) {
        this.cliente = cliente;
        this.sesion = sesion;
    }

    public String getUrl() {
        return cliente.getBaseUrl();
    }

    public boolean haySesion() {
        return sesion.activa();
    }

    public RespuestaServicio iniciarSesion(String usuario, String clave) {
        RespuestaServicio r = cliente.login(usuario, clave);
        if (r.esExitosa() && r.getToken() != null) {
            sesion.iniciar(r.getToken());
        } else {
            sesion.cerrar();
        }
        return r;
    }

    public void cerrarSesion() {
        sesion.cerrar();
    }
}
