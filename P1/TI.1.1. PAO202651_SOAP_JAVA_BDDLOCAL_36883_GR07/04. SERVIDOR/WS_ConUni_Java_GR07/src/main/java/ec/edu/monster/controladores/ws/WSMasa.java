package ec.edu.monster.controladores.ws;

import ec.edu.monster.controladores.manejadoresws.ManejadorConversion;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/** CU-04: realizar conversion de la categoria Masa. */
@WebService(serviceName = "WSMasa", targetNamespace = "http://WebServices.monster.edu.ec/")
public class WSMasa {

    private final ManejadorConversion manejador = new ManejadorConversion();

    @WebMethod(operationName = "convertirMasa")
    public RespuestaServicio convertirMasa(
            @WebParam(name = "valor") double valor,
            @WebParam(name = "unidadOrigen") String unidadOrigen,
            @WebParam(name = "unidadDestino") String unidadDestino,
            @WebParam(name = "token") String token) {
        return manejador.convertir("Masa", valor, unidadOrigen, unidadDestino, token);
    }
}
