package ec.edu.monster.banco.modelos;

/** Resumen de un credito de un cliente (para listarlos). Viaja por SOAP. */
public class ResumenCredito {

    private int codCredito;
    private double montoCredito;
    private int plazoMeses;
    private double cuotaFija;
    private String fechaInicio;
    private String estado;

    public ResumenCredito() {
    }

    public int getCodCredito() {
        return codCredito;
    }

    public void setCodCredito(int codCredito) {
        this.codCredito = codCredito;
    }

    public double getMontoCredito() {
        return montoCredito;
    }

    public void setMontoCredito(double montoCredito) {
        this.montoCredito = montoCredito;
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(int plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public double getCuotaFija() {
        return cuotaFija;
    }

    public void setCuotaFija(double cuotaFija) {
        this.cuotaFija = cuotaFija;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /** ACTIVO o CANCELADO. */
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
