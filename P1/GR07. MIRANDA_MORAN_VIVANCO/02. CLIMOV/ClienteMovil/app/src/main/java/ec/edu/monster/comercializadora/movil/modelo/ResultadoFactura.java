package ec.edu.monster.comercializadora.movil.modelo;

/** Resultado de intentar facturar: si no se pudo, dice por que y no trae factura. */
public class ResultadoFactura {

    private boolean exito;
    private String mensaje;
    private Factura factura;

    public ResultadoFactura() {
    }

    public ResultadoFactura(boolean exito, String mensaje, Factura factura) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.factura = factura;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }
}
