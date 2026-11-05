package ec.edu.monster.eureka.modelos;

/** Error de regla de negocio: su mensaje se muestra tal cual al usuario. */
public class ReglaNegocio extends RuntimeException {

    public ReglaNegocio(String mensaje) {
        super(mensaje);
    }
}
