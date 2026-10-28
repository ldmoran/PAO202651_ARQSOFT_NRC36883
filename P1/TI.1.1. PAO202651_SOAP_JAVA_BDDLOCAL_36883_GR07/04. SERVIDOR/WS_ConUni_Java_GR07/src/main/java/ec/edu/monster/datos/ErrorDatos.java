package ec.edu.monster.datos;

/** Falla al hablar con la base de datos. Los manejadores la convierten en el codigo SRV-500. */
public class ErrorDatos extends RuntimeException {

    public ErrorDatos(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
