package ec.edu.monster.clienteescritorio.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta estructurada del servicio SOAP (estado, codigo, mensaje, resultado,
 * unidadOrigen, unidadDestino; mas token en el login y datos en el catalogo).
 */
public class RespuestaServicio {

    public static final String COD_TOKEN_INVALIDO = "AUTH-001";
    public static final String COD_SESION_EXPIRADA = "AUTH-002";
    /** Codigo propio del cliente: no se pudo hablar con el servidor. */
    public static final String COD_RED = "NET-001";

    private String estado;
    private String codigo;
    private String mensaje;
    private Double resultado;
    private String unidadOrigen;
    private String unidadDestino;
    private String token;
    private final List<String> datos = new ArrayList<>();

    public boolean esExitosa() {
        return "OK".equals(estado);
    }

    /** true si el servidor rechazo el token (hay que volver a iniciar sesion). */
    public boolean requiereNuevoLogin() {
        return COD_TOKEN_INVALIDO.equals(codigo) || COD_SESION_EXPIRADA.equals(codigo);
    }

    public static RespuestaServicio errorLocal(String codigo, String mensaje) {
        RespuestaServicio r = new RespuestaServicio();
        r.estado = "ERROR";
        r.codigo = codigo;
        r.mensaje = mensaje;
        return r;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Double getResultado() { return resultado; }
    public void setResultado(Double resultado) { this.resultado = resultado; }
    public String getUnidadOrigen() { return unidadOrigen; }
    public void setUnidadOrigen(String unidadOrigen) { this.unidadOrigen = unidadOrigen; }
    public String getUnidadDestino() { return unidadDestino; }
    public void setUnidadDestino(String unidadDestino) { this.unidadDestino = unidadDestino; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public List<String> getDatos() { return datos; }
}
