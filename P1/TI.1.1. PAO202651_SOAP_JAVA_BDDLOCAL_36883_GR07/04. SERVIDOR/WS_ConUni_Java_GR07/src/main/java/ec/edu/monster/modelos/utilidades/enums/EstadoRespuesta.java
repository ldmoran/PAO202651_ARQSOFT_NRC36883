package ec.edu.monster.modelos.utilidades.enums;

/** Estado general de una respuesta: la operacion salio bien o fallo. */
public enum EstadoRespuesta {

    OK("OK"),
    ERROR("ERROR");

    private final String texto;

    EstadoRespuesta(String texto) {
        this.texto = texto;
    }

    /** Texto que viaja en el mensaje SOAP: "OK" o "ERROR". */
    public String getTexto() {
        return texto;
    }
}
