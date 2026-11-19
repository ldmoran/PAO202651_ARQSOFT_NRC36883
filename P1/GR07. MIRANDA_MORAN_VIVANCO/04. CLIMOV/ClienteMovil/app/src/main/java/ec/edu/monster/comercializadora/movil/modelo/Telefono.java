package ec.edu.monster.comercializadora.movil.modelo;

/** Celular del catalogo. Viaja por SOAP. */
public class Telefono {

    private int codTelefono;
    private String marca;
    private String modelo;
    private double precio;
    private boolean disponible;

    public Telefono() {
    }

    public Telefono(int codTelefono, String marca, String modelo, double precio, boolean disponible) {
        this.codTelefono = codTelefono;
        this.marca = marca;
        this.modelo = modelo;
        this.precio = precio;
        this.disponible = disponible;
    }

    public int getCodTelefono() {
        return codTelefono;
    }

    public void setCodTelefono(int codTelefono) {
        this.codTelefono = codTelefono;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}
