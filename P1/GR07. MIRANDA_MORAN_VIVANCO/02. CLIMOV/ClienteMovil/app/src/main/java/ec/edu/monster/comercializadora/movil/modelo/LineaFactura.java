package ec.edu.monster.comercializadora.movil.modelo;

/** Una linea de la factura: un modelo de celular con su cantidad. */
public class LineaFactura {

    private int codTelefono;
    private String descripcion;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public LineaFactura() {
    }

    public LineaFactura(int codTelefono, String descripcion, int cantidad, double precioUnitario, double subtotal) {
        this.codTelefono = codTelefono;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getCodTelefono() {
        return codTelefono;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
