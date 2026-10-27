package ec.edu.monster.comercializadora.movil.modelo;

/** Como se obtuvo el monto maximo de credito de un cliente. Viaja por SOAP. */
public class DetalleMontoMaximo {

    private int codCliente;
    private String nombre;
    private double promedioDepositos;
    private double promedioRetiros;
    private double montoMaximo;
    private String mensaje;

    public DetalleMontoMaximo() {
    }

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

    /** Promedio de los depositos de los ultimos 3 meses. */
    public double getPromedioDepositos() {
        return promedioDepositos;
    }

    public void setPromedioDepositos(double promedioDepositos) {
        this.promedioDepositos = promedioDepositos;
    }

    /** Promedio de los retiros de los ultimos 3 meses. */
    public double getPromedioRetiros() {
        return promedioRetiros;
    }

    public void setPromedioRetiros(double promedioRetiros) {
        this.promedioRetiros = promedioRetiros;
    }

    /** ((promedio depositos - promedio retiros) x 30 %) x 6; nunca negativo. */
    public double getMontoMaximo() {
        return montoMaximo;
    }

    public void setMontoMaximo(double montoMaximo) {
        this.montoMaximo = montoMaximo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
