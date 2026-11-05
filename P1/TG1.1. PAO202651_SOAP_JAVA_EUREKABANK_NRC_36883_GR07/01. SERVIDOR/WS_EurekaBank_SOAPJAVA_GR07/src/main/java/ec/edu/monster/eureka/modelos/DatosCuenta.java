package ec.edu.monster.eureka.modelos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/** Una cuenta activa junto con los datos de su cliente. */
@XmlRootElement(name = "DatosCuenta")
@XmlAccessorType(XmlAccessType.FIELD)
public class DatosCuenta {

    @XmlElement(name = "Codigo")
    private String codigo;
    @XmlElement(name = "NombreCliente")
    private String nombreCliente;
    @XmlElement(name = "Saldo")
    private BigDecimal saldo;
    @XmlElement(name = "Estado")
    private String estado;
    @XmlElement(name = "Moneda")
    private String moneda;
    @XmlElement(name = "EmailCliente")
    private String emailCliente;
    @XmlElement(name = "TelefonoCliente")
    private String telefonoCliente;

    public DatosCuenta() {
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }
}
