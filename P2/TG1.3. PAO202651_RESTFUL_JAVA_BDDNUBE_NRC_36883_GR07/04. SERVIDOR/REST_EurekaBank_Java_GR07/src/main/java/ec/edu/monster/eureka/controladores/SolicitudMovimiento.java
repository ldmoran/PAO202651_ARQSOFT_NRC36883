package ec.edu.monster.eureka.controladores;

/** Cuerpo JSON de POST /api/movimientos. */
public class SolicitudMovimiento {

    private String cuentaOrigen;
    private String cuentaDestino;
    private String tipo;
    private Double importe;

    public SolicitudMovimiento() {
    }

    public SolicitudMovimiento(String cuentaOrigen, String cuentaDestino, String tipo, Double importe) {
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.tipo = tipo;
        this.importe = importe;
    }

    public String getCuentaOrigen() {
        return cuentaOrigen;
    }

    public void setCuentaOrigen(String cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(String cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getImporte() {
        return importe;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }
}
