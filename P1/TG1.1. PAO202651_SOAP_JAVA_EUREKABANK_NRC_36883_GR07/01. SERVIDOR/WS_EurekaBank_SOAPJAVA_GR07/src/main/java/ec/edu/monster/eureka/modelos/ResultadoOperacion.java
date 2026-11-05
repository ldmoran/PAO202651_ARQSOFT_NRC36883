package ec.edu.monster.eureka.modelos;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/** Resultado de registrar un movimiento: codigo 1 = exito, -1 = error. */
@XmlRootElement(name = "resultado")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultadoOperacion {

    private int codigo;
    private String mensaje;

    public ResultadoOperacion() {
    }

    public ResultadoOperacion(int codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public static ResultadoOperacion exito() {
        return new ResultadoOperacion(1, "Operación exitosa");
    }

    public static ResultadoOperacion error(String mensaje) {
        return new ResultadoOperacion(-1, mensaje);
    }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
