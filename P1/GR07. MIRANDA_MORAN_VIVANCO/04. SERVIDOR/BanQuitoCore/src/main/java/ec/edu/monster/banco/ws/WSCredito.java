package ec.edu.monster.banco.ws;

import ec.edu.monster.banco.db.ConexionMySql;
import ec.edu.monster.banco.modelos.CuotaAmortizacion;
import ec.edu.monster.banco.modelos.DetalleMontoMaximo;
import ec.edu.monster.banco.modelos.EvaluacionCredito;
import ec.edu.monster.banco.modelos.ResultadoCredito;
import ec.edu.monster.banco.modelos.ResumenCredito;
import ec.edu.monster.banco.repositorios.CoreRepositorio;
import ec.edu.monster.banco.servicios.ServicioCredito;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.time.Clock;
import java.util.List;

/** Servicio SOAP del Banco Core: modulo de credito. Solo delega en ServicioCredito. */
@WebService(serviceName = "WSCredito", targetNamespace = "http://WebServices.banquito.edu.ec/")
public class WSCredito {

    private final ServicioCredito servicio;

    /** Constructor que usa el servidor de aplicaciones (MySQL y reloj del sistema). */
    public WSCredito() {
        this(new ServicioCredito(new CoreRepositorio(new ConexionMySql()), Clock.systemDefaultZone()));
    }

    /** Constructor para pruebas: permite inyectar otro servicio. */
    public WSCredito(ServicioCredito servicio) {
        this.servicio = servicio;
    }

    @WebMethod(operationName = "verificarSujetoCredito")
    public EvaluacionCredito verificarSujetoCredito(@WebParam(name = "cedula") String cedula) {
        return servicio.evaluar(cedula);
    }

    @WebMethod(operationName = "calcularMontoMaximo")
    public DetalleMontoMaximo calcularMontoMaximo(@WebParam(name = "cedula") String cedula) {
        return servicio.montoMaximo(cedula);
    }

    @WebMethod(operationName = "otorgarCredito")
    public ResultadoCredito otorgarCredito(@WebParam(name = "cedula") String cedula,
            @WebParam(name = "monto") double monto, @WebParam(name = "plazoMeses") int plazoMeses) {
        return servicio.otorgar(cedula, monto, plazoMeses);
    }

    @WebMethod(operationName = "obtenerTablaAmortizacion")
    public List<CuotaAmortizacion> obtenerTablaAmortizacion(@WebParam(name = "codCredito") int codCredito) {
        return servicio.tabla(codCredito);
    }

    @WebMethod(operationName = "listarCreditos")
    public List<ResumenCredito> listarCreditos(@WebParam(name = "cedula") String cedula) {
        return servicio.creditosDe(cedula);
    }
}
