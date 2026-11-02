package ec.edu.monster.controladores.rest;

import ec.edu.monster.controladores.manejadoresws.ManejadorCatalogo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("catalogo")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoResource {

    private final ManejadorCatalogo manejador = new ManejadorCatalogo();

    @GET
    @Path("categorias")
    public Response obtenerCategorias(@HeaderParam("Authorization") String authorization) {
        return RespuestaHttp.desde(manejador.obtenerCategorias(AutorizacionHttp.extraerToken(authorization)));
    }

    @GET
    @Path("{categoria}/unidades")
    public Response obtenerUnidades(
            @PathParam("categoria") String categoria,
            @HeaderParam("Authorization") String authorization) {
        return RespuestaHttp.desde(
                manejador.obtenerUnidades(AutorizacionHttp.extraerToken(authorization), categoria));
    }
}
