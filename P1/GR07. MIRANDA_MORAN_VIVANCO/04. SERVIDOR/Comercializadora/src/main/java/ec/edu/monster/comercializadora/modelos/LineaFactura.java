package ec.edu.monster.comercializadora.modelos;

/** Una linea de la factura: un modelo de celular con su cantidad. Viaja por SOAP dentro de {@link Factura}. */
public class LineaFactura {

    private int codTelefono;
    private String descripcion;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public LineaFactura() {
    }

    public LineaFactura(int codTelefono, String descripcion, int cantidad, double precioUnitario) {
        this.codTelefono = codTelefono;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = Math.round(precioUnitario * cantidad * 100.0) / 100.0;
    }

    public int getCodTelefono() {
        return codTelefono;
    }

    public void setCodTelefono(int codTelefono) {
        this.codTelefono = codTelefono;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
