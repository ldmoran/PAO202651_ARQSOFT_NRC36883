package ec.edu.monster.controladores.ws;

import ec.edu.monster.controladores.manejadoresws.ManejadorConversion;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/** CU-04: realizar conversion de la categoria Longitud. */
@WebService(serviceName = "WSLongitud", targetNamespace = "http://WebServices.monster.edu.ec/")
public class WSLongitud {

    private final ManejadorConversion manejador = new ManejadorConversion();

    @WebMethod(operationName = "convertirLongitud")
    public RespuestaServicio convertirLongitud(
            @WebParam(name = "valor") double valor,
            @WebParam(name = "unidadOrigen") String unidadOrigen,
            @WebParam(name = "unidadDestino") String unidadDestino,
            @WebParam(name = "token") String token) {
        return manejador.convertir("Longitud", valor, unidadOrigen, unidadDestino, token);
    }
}
