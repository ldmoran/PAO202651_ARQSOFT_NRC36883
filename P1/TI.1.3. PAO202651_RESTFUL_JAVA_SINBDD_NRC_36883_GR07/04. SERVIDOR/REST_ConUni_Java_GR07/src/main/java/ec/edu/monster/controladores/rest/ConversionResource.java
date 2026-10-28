package ec.edu.monster.controladores.rest;

import ec.edu.monster.controladores.manejadoresws.ManejadorConversion;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("conversiones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConversionResource {

    private final ManejadorConversion manejador = new ManejadorConversion();

    @POST
    @Path("{categoria}")
    public Response convertir(
            @PathParam("categoria") String categoria,
            SolicitudConversion solicitud,
            @HeaderParam("Authorization") String authorization) {
        if (solicitud == null || solicitud.getValor() == null) {
            RespuestaServicio error = RespuestaMapeador.error(
                    CodigoResultado.DATOS_INCONSISTENTES, "El valor es obligatorio y debe ser numérico");
            return RespuestaHttp.desde(error);
        }
        return RespuestaHttp.desde(manejador.convertir(
                categoria,
                solicitud.getValor(),
                solicitud.getUnidadOrigen(),
                solicitud.getUnidadDestino(),
                AutorizacionHttp.extraerToken(authorization)));
    }
}
