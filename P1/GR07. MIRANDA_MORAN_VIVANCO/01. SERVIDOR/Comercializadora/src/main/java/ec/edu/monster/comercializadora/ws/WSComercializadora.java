package ec.edu.monster.comercializadora.ws;

import ec.edu.monster.comercializadora.banco.CuotaAmortizacion;
import ec.edu.monster.comercializadora.banco.DetalleMontoMaximo;
import ec.edu.monster.comercializadora.banco.EvaluacionCredito;
import ec.edu.monster.comercializadora.banco.SoapClienteBanco;
import ec.edu.monster.comercializadora.db.ConexionMySql;
import ec.edu.monster.comercializadora.modelos.Factura;
import ec.edu.monster.comercializadora.modelos.ResultadoFactura;
import ec.edu.monster.comercializadora.modelos.Telefono;
import ec.edu.monster.comercializadora.repositorios.ComercioRepositorio;
import ec.edu.monster.comercializadora.servicios.ServicioComercial;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.time.Clock;
import java.util.List;

/** Servicio SOAP de la Comercializadora: lo consumen los 4 clientes. Solo delega en ServicioComercial. */
@WebService(serviceName = "WSComercializadora", targetNamespace = "http://WebServices.comercializadora.edu.ec/")
public class WSComercializadora {

    private final ServicioComercial servicio;

    public WSComercializadora() {
        this(new ServicioComercial(new ComercioRepositorio(new ConexionMySql()), SoapClienteBanco.desdeConfiguracion(),
                Clock.systemDefaultZone()));
    }

    public WSComercializadora(ServicioComercial servicio) {
        this.servicio = servicio;
    }

    @WebMethod(operationName = "listarTelefonos")
    public List<Telefono> listarTelefonos() {
        return servicio.listarTelefonos();
    }

    @WebMethod(operationName = "buscarTelefono")
    public Telefono buscarTelefono(@WebParam(name = "codTelefono") int codTelefono) {
        return servicio.buscarTelefono(codTelefono);
    }

    @WebMethod(operationName = "crearTelefono")
    public int crearTelefono(@WebParam(name = "marca") String marca, @WebParam(name = "modelo") String modelo,
            @WebParam(name = "precio") double precio) {
        return servicio.crearTelefono(marca, modelo, precio);
    }

    @WebMethod(operationName = "actualizarTelefono")
    public boolean actualizarTelefono(@WebParam(name = "codTelefono") int codTelefono,
            @WebParam(name = "marca") String marca, @WebParam(name = "modelo") String modelo,
            @WebParam(name = "precio") double precio, @WebParam(name = "disponible") boolean disponible) {
        return servicio.actualizarTelefono(codTelefono, marca, modelo, precio, disponible);
    }

    @WebMethod(operationName = "eliminarTelefono")
    public boolean eliminarTelefono(@WebParam(name = "codTelefono") int codTelefono) {
        return servicio.eliminarTelefono(codTelefono);
    }

    @WebMethod(operationName = "verificarSujetoCredito")
    public EvaluacionCredito verificarSujetoCredito(@WebParam(name = "cedula") String cedula) {
        return servicio.verificarSujetoCredito(cedula);
    }

    @WebMethod(operationName = "calcularMontoMaximo")
    public DetalleMontoMaximo calcularMontoMaximo(@WebParam(name = "cedula") String cedula) {
        return servicio.calcularMontoMaximo(cedula);
    }

    @WebMethod(operationName = "facturarEfectivo")
    public ResultadoFactura facturarEfectivo(@WebParam(name = "cedula") String cedula,
            @WebParam(name = "nombre") String nombre, @WebParam(name = "codTelefono") int codTelefono) {
        return servicio.facturarEfectivo(cedula, nombre, codTelefono);
    }

    @WebMethod(operationName = "facturarCredito")
    public ResultadoFactura facturarCredito(@WebParam(name = "cedula") String cedula,
            @WebParam(name = "nombre") String nombre, @WebParam(name = "codTelefono") int codTelefono,
            @WebParam(name = "plazoMeses") int plazoMeses) {
        return servicio.facturarCredito(cedula, nombre, codTelefono, plazoMeses);
    }

    @WebMethod(operationName = "consultarFacturas")
    public List<Factura> consultarFacturas(@WebParam(name = "cedula") String cedula) {
        return servicio.consultarFacturas(cedula);
    }

    @WebMethod(operationName = "obtenerAmortizacion")
    public List<CuotaAmortizacion> obtenerAmortizacion(@WebParam(name = "codCredito") int codCredito) {
        return servicio.obtenerAmortizacion(codCredito);
    }
}
