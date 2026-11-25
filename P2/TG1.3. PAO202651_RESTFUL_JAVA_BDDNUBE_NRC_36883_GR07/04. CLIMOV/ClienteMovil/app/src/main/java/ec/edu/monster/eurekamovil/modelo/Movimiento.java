package ec.edu.monster.eurekamovil.modelo;

/** Un movimiento de una cuenta (GET /api/movimientos). */
public class Movimiento {

    public String codigoCuenta;
    public int numero;
    public String fecha;
    public String tipo;
    public double importe;
    public String referencia;
    public double saldoActual;
    public String nombreCliente;
}
