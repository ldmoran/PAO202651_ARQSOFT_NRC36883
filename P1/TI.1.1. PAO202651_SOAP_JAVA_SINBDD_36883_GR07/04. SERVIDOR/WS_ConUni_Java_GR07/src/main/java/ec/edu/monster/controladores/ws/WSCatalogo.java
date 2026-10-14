package ec.edu.monster.controladores.ws;

import ec.edu.monster.controladores.manejadoresws.ManejadorCatalogo;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/** CU-02 y CU-03: consulta del catalogo de categorias y unidades. */
@WebService(serviceName = "WSCatalogo", targetNamespace = "http://WebServices.monster.edu.ec/")
public class WSCatalogo {

    private final ManejadorCatalogo manejador = new ManejadorCatalogo();

    @WebMethod(operationName = "obtenerCategorias")
    public RespuestaServicio obtenerCategorias(@WebParam(name = "token") String token) {
        return manejador.obtenerCategorias(token);
    }

    @WebMethod(operationName = "obtenerUnidades")
    public RespuestaServicio obtenerUnidades(
            @WebParam(name = "token") String token,
            @WebParam(name = "categoria") String categoria) {
        return manejador.obtenerUnidades(token, categoria);
    }
}
