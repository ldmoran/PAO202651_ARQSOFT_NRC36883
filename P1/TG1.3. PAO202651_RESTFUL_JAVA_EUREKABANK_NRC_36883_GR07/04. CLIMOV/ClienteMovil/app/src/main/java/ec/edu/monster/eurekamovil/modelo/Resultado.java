package ec.edu.monster.eurekamovil.modelo;

/** Resultado de iniciar sesion o de registrar un movimiento: ok y el mensaje que se muestra al usuario. */
public class Resultado {

    public final boolean ok;
    public final String mensaje;

    public Resultado(boolean ok, String mensaje) {
        this.ok = ok;
        this.mensaje = mensaje;
    }
}
