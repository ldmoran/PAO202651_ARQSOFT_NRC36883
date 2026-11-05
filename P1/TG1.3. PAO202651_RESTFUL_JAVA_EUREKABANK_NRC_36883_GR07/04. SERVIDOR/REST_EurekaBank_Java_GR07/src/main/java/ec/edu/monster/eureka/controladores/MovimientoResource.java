package ec.edu.monster.eureka.controladores;

import ec.edu.monster.eureka.datos.Datos;
import ec.edu.monster.eureka.datos.EurekaRepositorio;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Movimientos: GET /api/movimientos?cuenta=... (traerMovimientos), GET /api/movimientos/tipos
 * (tipoMovimientosPermitidos) y POST /api/movimientos (regMovimiento).
 */
@Path("movimientos")
@Produces(MediaType.APPLICATION_JSON)
public class MovimientoResource {

    private static ServicioEureka servicio() {
        return new ServicioEureka(new EurekaRepositorio(Datos.fuente()));
    }

    @GET
    public Response movimientos(@QueryParam("cuenta") String cuenta) {
        try {
            return Response.ok(servicio().movimientos(cuenta)).build();
        } catch (Exception e) {
            return Response.status(500).entity(ResultadoOperacion.errorInterno()).build();
        }
    }

    @GET
    @Path("tipos")
    public Response tipos() {
        return Response.ok(ServicioEureka.TIPOS_PERMITIDOS).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrar(SolicitudMovimiento s) {
        if (s == null) {
            return Response.status(400).entity(ResultadoOperacion.invalido("La solicitud es obligatoria")).build();
        }
        ResultadoOperacion r = servicio().registrar(s.getCuentaOrigen(), s.getCuentaDestino(), s.getTipo(), s.getImporte());
        return Response.status(r.estadoHttp()).entity(r).build();
    }
}
