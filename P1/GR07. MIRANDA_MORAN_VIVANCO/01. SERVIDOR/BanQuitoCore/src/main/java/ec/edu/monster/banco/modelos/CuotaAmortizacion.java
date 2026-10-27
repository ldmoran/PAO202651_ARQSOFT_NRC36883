package ec.edu.monster.banco.modelos;

/**
 * Una fila de la tabla de amortizacion: # cuota, valor cuota, interes pagado, capital
 * pagado y saldo. La cuota 0 es el saldo inicial. Viaja por SOAP (fechas como texto AAAA-MM-DD).
 */
public class CuotaAmortizacion {

    private int numCuota;
    private String fechaPago;
    private double valorCuota;
    private double interesPagado;
    private double capitalPagado;
    private double saldo;

    public CuotaAmortizacion() {
    }

    public CuotaAmortizacion(int numCuota, String fechaPago, double valorCuota, double interesPagado,
            double capitalPagado, double saldo) {
        this.numCuota = numCuota;
        this.fechaPago = fechaPago;
        this.valorCuota = valorCuota;
        this.interesPagado = interesPagado;
        this.capitalPagado = capitalPagado;
        this.saldo = saldo;
    }

    public int getNumCuota() {
        return numCuota;
    }

    public void setNumCuota(int numCuota) {
        this.numCuota = numCuota;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getValorCuota() {
        return valorCuota;
    }

    public void setValorCuota(double valorCuota) {
        this.valorCuota = valorCuota;
    }

    public double getInteresPagado() {
        return interesPagado;
    }

    public void setInteresPagado(double interesPagado) {
        this.interesPagado = interesPagado;
    }

    public double getCapitalPagado() {
        return capitalPagado;
    }

    public void setCapitalPagado(double capitalPagado) {
        this.capitalPagado = capitalPagado;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}
