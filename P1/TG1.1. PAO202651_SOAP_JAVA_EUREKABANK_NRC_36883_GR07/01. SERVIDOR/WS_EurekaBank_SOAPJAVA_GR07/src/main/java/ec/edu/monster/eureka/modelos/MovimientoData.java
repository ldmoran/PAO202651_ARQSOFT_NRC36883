package ec.edu.monster.eureka.modelos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/** Un movimiento de una cuenta, con el saldo que quedo despues de aplicarlo. */
@XmlRootElement(name = "MovimientoData")
@XmlAccessorType(XmlAccessType.FIELD)
public class MovimientoData {

    @XmlElement(name = "CodigoCuenta")
    private String codigoCuenta;
    @XmlElement(name = "Numero")
    private int numero;
    /** Fecha en formato AAAA-MM-DD (texto, para que cualquier cliente la lea igual). */
    @XmlElement(name = "Fecha")
    private String fecha;
    @XmlElement(name = "Tipo")
    private String tipo;
    @XmlElement(name = "Importe")
    private BigDecimal importe;
    @XmlElement(name = "Referencia")
    private String referencia;
    @XmlElement(name = "SaldoActual")
    private BigDecimal saldoActual;
    @XmlElement(name = "NombreCliente")
    private String nombreCliente;

    public MovimientoData() {
    }

    public String getCodigoCuenta() { return codigoCuenta; }
    public void setCodigoCuenta(String codigoCuenta) { this.codigoCuenta = codigoCuenta; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public BigDecimal getSaldoActual() { return saldoActual; }
    public void setSaldoActual(BigDecimal saldoActual) { this.saldoActual = saldoActual; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
