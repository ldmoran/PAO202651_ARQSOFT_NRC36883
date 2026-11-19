package ec.edu.monster.comercializadora.movil.modelo;

/**
 * Resultado de verificar si una persona es sujeto de credito (CU: "Verificar si el
 * cliente es sujeto de credito"). Viaja por SOAP.
 */
public class EvaluacionCredito {

    private boolean sujetoCredito;
    private int codCliente;
    private String nombre;
    private String motivo;

    public EvaluacionCredito() {
    }

    public EvaluacionCredito(boolean sujetoCredito, int codCliente, String nombre, String motivo) {
        this.sujetoCredito = sujetoCredito;
        this.codCliente = codCliente;
        this.nombre = nombre;
        this.motivo = motivo;
    }

    public boolean isSujetoCredito() {
        return sujetoCredito;
    }

    public void setSujetoCredito(boolean sujetoCredito) {
        this.sujetoCredito = sujetoCredito;
    }

    /** Codigo del cliente en el Core; 0 si la cedula no pertenece a un cliente del banco. */
    public int getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(int codCliente) {
        this.codCliente = codCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Explica por que es o no sujeto de credito. */
    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
