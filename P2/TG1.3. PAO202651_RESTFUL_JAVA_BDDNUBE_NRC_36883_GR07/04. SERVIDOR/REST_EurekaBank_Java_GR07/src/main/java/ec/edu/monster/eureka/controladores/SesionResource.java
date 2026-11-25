package ec.edu.monster.eureka.controladores;

import ec.edu.monster.eureka.datos.Datos;
import ec.edu.monster.eureka.datos.EurekaRepositorio;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** POST /api/sesion/login: valida usuario y contrasena (equivale a iniciarSesion del servicio SOAP). */
@Path("sesion")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SesionResource {

    @POST
    @Path("login")
    public Response login(SolicitudLogin solicitud) {
        if (solicitud == null) {
            return Response.status(400).entity(ResultadoOperacion.invalido("Usuario y contraseña son obligatorios")).build();
        }
        boolean ok;
        try {
            ok = new ServicioEureka(new EurekaRepositorio(Datos.fuente())).iniciarSesion(solicitud.getUsuario(), solicitud.getContrasena());
        } catch (Exception e) {
            return Response.status(500).entity(ResultadoOperacion.errorInterno()).build();
        }
        return ok
                ? Response.ok(new RespuestaSesion(true, "Inicio de sesión exitoso")).build()
                : Response.status(401).entity(new RespuestaSesion(false, "Usuario o contraseña incorrectos")).build();
    }
}
