package ec.edu.monster.controladores.rest;

import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Si el cuerpo de la solicitud no es un JSON valido, responde 400 con la misma respuesta
 * estructurada de siempre (DATA-001) en lugar de la pagina de error HTML del servidor.
 */
@Provider
public class JsonInvalidoMapper implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException excepcion) {
        return RespuestaHttp.desde(RespuestaMapeador.error(
                CodigoResultado.DATOS_INCONSISTENTES, "El cuerpo de la solicitud no es un JSON válido"));
    }
}
