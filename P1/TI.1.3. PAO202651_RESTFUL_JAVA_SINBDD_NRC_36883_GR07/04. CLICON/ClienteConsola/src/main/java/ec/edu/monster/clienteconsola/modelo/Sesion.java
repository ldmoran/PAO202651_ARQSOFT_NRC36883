package ec.edu.monster.clienteconsola.modelo;

/** Estado de la sesion del usuario: guarda el token que entrego el servidor. */
public class Sesion {

    private String token;

    public synchronized void iniciar(String token) {
        this.token = token;
    }

    public synchronized void cerrar() {
        this.token = null;
    }

    public synchronized boolean activa() {
        return token != null;
    }

    public synchronized String getToken() {
        return token;
    }
}
