package ec.edu.monster.eurekamovil.modelo;

/** Falla al consultar el servidor (sin conexion o respuesta de error). El mensaje se muestra al usuario. */
public class ErrorServicio extends RuntimeException {

    public ErrorServicio(String mensaje) {
        super(mensaje);
    }
}
