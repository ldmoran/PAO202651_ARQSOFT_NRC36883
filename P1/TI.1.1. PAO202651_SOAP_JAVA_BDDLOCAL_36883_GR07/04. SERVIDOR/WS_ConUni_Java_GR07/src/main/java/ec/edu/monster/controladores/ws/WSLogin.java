package ec.edu.monster.controladores.ws;

import ec.edu.monster.controladores.manejadoresws.ManejadorLogin;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * CU-01: Autenticarse. Devuelve un token temporal si las credenciales son correctas.
 * El espacio de nombres se fija para que no cambie el contrato (WSDL) que usan los clientes.
 */
@WebService(serviceName = "WSLogin", targetNamespace = "http://WebServices.monster.edu.ec/")
public class WSLogin {

    private final ManejadorLogin manejador = new ManejadorLogin();

    @WebMethod(operationName = "login")
    public RespuestaServicio login(
            @WebParam(name = "usuario") String usuario,
            @WebParam(name = "clave") String clave) {
        return manejador.login(usuario, clave);
    }
}
