using CoreWCF;
using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.Modelos;
using ec.edu.monster.eureka.Servicios;

namespace ec.edu.monster.eureka.WS
{
    /// <summary>
    /// Contrato SOAP de EurekaBank. Es el mismo que publica el servidor Java (mismo espacio de nombres y operaciones),
    /// asi que cualquier cliente funciona con los dos servidores cambiando solo la URL.
    /// </summary>
    [ServiceContract(Name = "WSEurekaBank", Namespace = Contrato.Namespace)]
    public interface IWSEurekaBank
    {
        [OperationContract(Name = "iniciarSesion")]
        [return: MessageParameter(Name = "resultado")]
        bool IniciarSesion(string usuario, string contrasena);

        [OperationContract(Name = "traerCuentasConClientes")]
        List<DatosCuenta> TraerCuentasConClientes();

        [OperationContract(Name = "traerMovimientos")]
        List<MovimientoData> TraerMovimientos(string cuenta);

        [OperationContract(Name = "tipoMovimientosPermitidos")]
        List<string> TipoMovimientosPermitidos();

        [OperationContract(Name = "regMovimiento")]
        ResultadoOperacion RegMovimiento(string cuentaOrigen, string cuentaDestino, string tipo, double importe);
    }

    public class WSEurekaBank : IWSEurekaBank
    {
        private readonly ServicioEureka servicio = new(new EurekaRepositorio(BaseDatos.Fuente));

        public bool IniciarSesion(string usuario, string contrasena)
        {
            try { return servicio.IniciarSesion(usuario, contrasena); } catch (Exception) { return false; }
        }

        public List<DatosCuenta> TraerCuentasConClientes()
        {
            try { return servicio.Cuentas(); } catch (Exception) { return new(); }
        }

        public List<MovimientoData> TraerMovimientos(string cuenta)
        {
            try { return servicio.Movimientos(cuenta); } catch (Exception) { return new(); }
        }

        public List<string> TipoMovimientosPermitidos() => ServicioEureka.TiposPermitidos.ToList();

        public ResultadoOperacion RegMovimiento(string cuentaOrigen, string cuentaDestino, string tipo, double importe) =>
            servicio.Registrar(cuentaOrigen, cuentaDestino, tipo, importe);
    }
}
