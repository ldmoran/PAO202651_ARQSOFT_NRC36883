package ec.edu.monster.controladores.rest;

public class SolicitudConversion {

    private Double valor;
    private String unidadOrigen;
    private String unidadDestino;

    public SolicitudConversion() {
    }

    public SolicitudConversion(Double valor, String unidadOrigen, String unidadDestino) {
        this.valor = valor;
        this.unidadOrigen = unidadOrigen;
        this.unidadDestino = unidadDestino;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getUnidadOrigen() {
        return unidadOrigen;
    }

    public void setUnidadOrigen(String unidadOrigen) {
        this.unidadOrigen = unidadOrigen;
    }

    public String getUnidadDestino() {
        return unidadDestino;
    }

    public void setUnidadDestino(String unidadDestino) {
        this.unidadDestino = unidadDestino;
    }
}
