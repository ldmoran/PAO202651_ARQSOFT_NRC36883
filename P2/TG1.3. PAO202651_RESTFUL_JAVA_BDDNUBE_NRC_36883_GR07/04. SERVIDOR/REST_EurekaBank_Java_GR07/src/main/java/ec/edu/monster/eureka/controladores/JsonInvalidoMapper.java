package ec.edu.monster.eureka.controladores;

import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Si el cuerpo de la solicitud no es un JSON valido, responde 400 con el mismo formato de siempre. */
@Provider
public class JsonInvalidoMapper implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException excepcion) {
        return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ResultadoOperacion.invalido("El cuerpo de la solicitud no es un JSON válido")).build();
    }
}
