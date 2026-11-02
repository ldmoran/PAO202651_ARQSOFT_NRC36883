package ec.edu.monster.controladores.rest;

import ec.edu.monster.controladores.manejadoresws.ManejadorLogin;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {

    private final ManejadorLogin manejador = new ManejadorLogin();

    @POST
    @Path("login")
    public Response login(SolicitudLogin solicitud) {
        if (solicitud == null) {
            RespuestaServicio error = RespuestaMapeador.error(
                    CodigoResultado.DATOS_INCONSISTENTES, "Usuario y clave son obligatorios");
            return RespuestaHttp.desde(error);
        }
        return RespuestaHttp.desde(manejador.login(solicitud.getUsuario(), solicitud.getClave()));
    }
}
