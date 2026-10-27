package ec.edu.monster.cliente.modelos;

/** Factura de venta de un celular. Viaja por SOAP. */
public class Factura {

    private int numFactura;
    private String fecha;
    private String cedula;
    private String nombreCliente;
    private int codTelefono;
    private String telefono;
    private String formaPago;
    private double precioLista;
    private double descuento;
    private double total;
    private int numCuotas;
    private int codCredito;

    public Factura() {
    }

    public int getNumFactura() {
        return numFactura;
    }

    public void setNumFactura(int numFactura) {
        this.numFactura = numFactura;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getCodTelefono() {
        return codTelefono;
    }

    public void setCodTelefono(int codTelefono) {
        this.codTelefono = codTelefono;
    }

    /** Marca y modelo del celular vendido. */
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /** EFECTIVO o CREDITO. */
    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(double precioLista) {
        this.precioLista = precioLista;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    /** 0 si fue de contado. */
    public int getNumCuotas() {
        return numCuotas;
    }

    public void setNumCuotas(int numCuotas) {
        this.numCuotas = numCuotas;
    }

    /** Codigo del credito en el banco; 0 si fue de contado. */
    public int getCodCredito() {
        return codCredito;
    }

    public void setCodCredito(int codCredito) {
        this.codCredito = codCredito;
    }
}
