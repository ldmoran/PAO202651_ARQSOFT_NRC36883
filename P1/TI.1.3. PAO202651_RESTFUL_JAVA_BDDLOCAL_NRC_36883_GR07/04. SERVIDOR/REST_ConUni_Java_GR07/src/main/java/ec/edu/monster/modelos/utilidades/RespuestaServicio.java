package ec.edu.monster.modelos.utilidades;

import ec.edu.monster.modelos.utilidades.enums.EstadoRespuesta;
import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta estructurada que devuelven todas las operaciones del servicio
 * (CU-07). Se serializa como JSON en el cuerpo de la respuesta REST. Se arma con
 * {@code RespuestaMapeador}.
 *
 * estado: OK o ERROR.
 * codigo: codigo de resultado (OK-200, AUTH-001, AUTH-002, AUTH-003, UNIT-001, DATA-001, SRV-500).
 * mensaje: texto informativo para el usuario.
 * resultado: valor convertido (nulo si hay error o si la operacion no calcula).
 * unidadOrigen / unidadDestino: unidades de la solicitud (conversiones).
 * token: token generado (solo en el login).
 * datos: listas de texto (categorias o unidades del catalogo).
 */
public class RespuestaServicio {

    private String estado;
    private String codigo;
    private String mensaje;
    private Double resultado;
    private String unidadOrigen;
    private String unidadDestino;
    private String token;
    private List<String> datos = new ArrayList<>();

    public RespuestaServicio() {
    }

    public boolean esExitosa() {
        return EstadoRespuesta.OK.getTexto().equals(estado);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Double getResultado() {
        return resultado;
    }

    public void setResultado(Double resultado) {
        this.resultado = resultado;
    }

    public String getUnidadOrigen() {
        return unidadOrigen;
    }

    public void setUnidadOrigen(String unidadOrigen) {
        this.unidadOrigen = unidadOrigen;
    }

    public String getUnidadDestino() {
        return unidadDestino;
    }

    public void setUnidadDestino(String unidadDestino) {
        this.unidadDestino = unidadDestino;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getDatos() {
        return datos;
    }

    public void setDatos(List<String> datos) {
        this.datos = datos;
    }
}
