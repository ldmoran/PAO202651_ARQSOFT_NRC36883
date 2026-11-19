package ec.edu.monster.banco.modelos;

/** Respuesta de "otorgar credito": si se aprobo, con que codigo y con que cuota. Viaja por SOAP. */
public class ResultadoCredito {

    private boolean aprobado;
    private String mensaje;
    private int codCredito;
    private double montoMaximo;
    private double montoCredito;
    private int plazoMeses;
    private double cuotaFija;

    public ResultadoCredito() {
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /** Codigo del credito creado; 0 si no se aprobo. */
    public int getCodCredito() {
        return codCredito;
    }

    public void setCodCredito(int codCredito) {
        this.codCredito = codCredito;
    }

    public double getMontoMaximo() {
        return montoMaximo;
    }

    public void setMontoMaximo(double montoMaximo) {
        this.montoMaximo = montoMaximo;
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
}
