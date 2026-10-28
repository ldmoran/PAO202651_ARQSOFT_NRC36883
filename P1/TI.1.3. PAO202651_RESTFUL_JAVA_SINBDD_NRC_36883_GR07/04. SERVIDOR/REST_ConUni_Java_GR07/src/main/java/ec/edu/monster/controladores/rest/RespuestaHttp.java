package ec.edu.monster.controladores.rest;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import jakarta.ws.rs.core.Response;

final class RespuestaHttp {

    private RespuestaHttp() {
    }

    static Response desde(RespuestaServicio respuesta) {
        return Response.status(estadoHttp(respuesta))
                .entity(respuesta)
                .build();
    }

    private static int estadoHttp(RespuestaServicio respuesta) {
        if (respuesta.esExitosa()) {
            return Response.Status.OK.getStatusCode();
        }
        switch (respuesta.getCodigo()) {
            case "AUTH-001":
            case "AUTH-002":
            case "AUTH-003":
                return Response.Status.UNAUTHORIZED.getStatusCode();
            case "UNIT-001":
                return 422;
            case "DATA-001":
                return Response.Status.BAD_REQUEST.getStatusCode();
            default:
                return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        }
    }
}
