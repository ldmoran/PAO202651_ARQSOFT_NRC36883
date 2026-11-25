package ec.edu.monster.eureka.controladores;

import ec.edu.monster.eureka.datos.Datos;
import ec.edu.monster.eureka.datos.EurekaRepositorio;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** GET /api/cuentas: cuentas activas con los datos de su cliente (equivale a traerCuentasConClientes). */
@Path("cuentas")
@Produces(MediaType.APPLICATION_JSON)
public class CuentaResource {

    @GET
    public Response cuentas() {
        try {
            return Response.ok(new ServicioEureka(new EurekaRepositorio(Datos.fuente())).cuentas()).build();
        } catch (Exception e) {
            return Response.status(500).entity(ResultadoOperacion.errorInterno()).build();
        }
    }
}
