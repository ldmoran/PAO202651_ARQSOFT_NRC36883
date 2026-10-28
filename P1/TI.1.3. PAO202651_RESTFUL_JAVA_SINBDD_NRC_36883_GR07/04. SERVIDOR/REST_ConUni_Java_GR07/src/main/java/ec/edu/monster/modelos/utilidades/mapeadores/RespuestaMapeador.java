package ec.edu.monster.modelos.utilidades.mapeadores;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.enums.EstadoRespuesta;
import java.util.List;

/**
 * Convierte los resultados de la logica (tokens, listas, valores convertidos y
 * errores) en la respuesta estructurada {@link RespuestaServicio} (CU-07).
 * Es el unico lugar donde se arma la respuesta, para que todas las operaciones
 * respondan con el mismo formato.
 */
public final class RespuestaMapeador {

    private RespuestaMapeador() {
    }

    /** Respuesta correcta con un mensaje. */
    public static RespuestaServicio exito(String mensaje) {
        RespuestaServicio r = new RespuestaServicio();
        r.setEstado(EstadoRespuesta.OK.getTexto());
        r.setCodigo(CodigoResultado.OK_200.getCodigo());
        r.setMensaje(mensaje);
        return r;
    }

    /** Login correcto: lleva el token de la sesion. */
    public static RespuestaServicio exitoLogin(String token) {
        RespuestaServicio r = exito("Autenticación exitosa");
        r.setToken(token);
        return r;
    }

    /** Consulta del catalogo: lleva la lista de categorias o de unidades. */
    public static RespuestaServicio exitoLista(List<String> datos) {
        RespuestaServicio r = exito("Consulta realizada correctamente");
        r.setDatos(datos);
        return r;
    }

    /** Conversion correcta: lleva el resultado y las unidades usadas. */
    public static RespuestaServicio exitoConversion(double resultado, String unidadOrigen, String unidadDestino) {
        RespuestaServicio r = exito("Conversión realizada correctamente");
        r.setResultado(resultado);
        r.setUnidadOrigen(unidadOrigen);
        r.setUnidadDestino(unidadDestino);
        return r;
    }

    /** Respuesta de error con su codigo y mensaje; el resultado queda nulo. */
    public static RespuestaServicio error(CodigoResultado codigo, String mensaje) {
        RespuestaServicio r = new RespuestaServicio();
        r.setEstado(EstadoRespuesta.ERROR.getTexto());
        r.setCodigo(codigo.getCodigo());
        r.setMensaje(mensaje);
        return r;
    }
}
