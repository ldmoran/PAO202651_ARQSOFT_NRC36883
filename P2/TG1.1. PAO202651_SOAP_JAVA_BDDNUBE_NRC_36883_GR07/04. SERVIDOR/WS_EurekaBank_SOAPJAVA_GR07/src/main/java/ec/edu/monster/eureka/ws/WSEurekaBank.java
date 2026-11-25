package ec.edu.monster.eureka.ws;

import ec.edu.monster.eureka.db.ConexionMySql;
import ec.edu.monster.eureka.modelos.DatosCuenta;
import ec.edu.monster.eureka.modelos.MovimientoData;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import ec.edu.monster.eureka.repositorios.EurekaRepositorio;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import java.util.ArrayList;
import java.util.List;

/** Servicio SOAP de EurekaBank. Solo delega en ServicioEureka (que usa MySQL). */
@WebService(serviceName = "WSEurekaBank", targetNamespace = "http://ws.eurekabank.monster.edu.ec/")
public class WSEurekaBank {

    private final ServicioEureka servicio;

    /** Constructor que usa el servidor de aplicaciones (MySQL). */
    public WSEurekaBank() {
        this(new ServicioEureka(new EurekaRepositorio(new ConexionMySql())));
    }

    /** Constructor para pruebas: permite inyectar otro servicio. */
    public WSEurekaBank(ServicioEureka servicio) {
        this.servicio = servicio;
    }

    @WebMethod(operationName = "traerMovimientos")
    @WebResult(name = "MovimientoData")
    public List<MovimientoData> traerMovimientos(@WebParam(name = "cuenta") String cuenta) {
        try {
            return servicio.movimientos(cuenta);
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    @WebMethod(operationName = "regMovimiento")
    @WebResult(name = "resultado")
    public ResultadoOperacion regMovimiento(@WebParam(name = "cuentaOrigen") String cuentaOrigen,
            @WebParam(name = "cuentaDestino") String cuentaDestino, @WebParam(name = "tipo") String tipo,
            @WebParam(name = "importe") double importe) {
        return servicio.registrar(cuentaOrigen, cuentaDestino, tipo, importe);
    }

    @WebMethod(operationName = "iniciarSesion")
    @WebResult(name = "resultado")
    public boolean iniciarSesion(@WebParam(name = "usuario") String usuario,
            @WebParam(name = "contrasena") String contrasena) {
        try {
            return servicio.iniciarSesion(usuario, contrasena);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @WebMethod(operationName = "traerCuentasConClientes")
    @WebResult(name = "GetDatosCuentasResult")
    public List<DatosCuenta> traerCuentasConClientes() {
        try {
            return servicio.cuentas();
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    @WebMethod(operationName = "tipoMovimientosPermitidos")
    @WebResult(name = "tipoMovimiento")
    public List<String> tipoMovimientosPermitidos() {
        return new ArrayList<>(servicio.tiposPermitidos());
    }
}
