using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.Modelos;
using ec.edu.monster.eureka.Servicios;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.eureka.Controladores
{
    /// <summary>
    /// Reemplaza las operaciones SOAP "traerMovimientos", "tipoMovimientosPermitidos" y "regMovimiento".
    /// </summary>
    [ApiController]
    [Route("api/movimientos")]
    public class MovimientosController : ControllerBase
    {
        private readonly ServicioEureka servicio = new(new EurekaRepositorio(BaseDatos.Fuente));

        [HttpGet]
        public ActionResult<List<MovimientoData>> TraerMovimientos([FromQuery] string? cuenta)
        {
            try
            {
                return Ok(servicio.Movimientos(cuenta));
            }
            catch (Exception)
            {
                return Ok(new List<MovimientoData>());
            }
        }

        [HttpGet("tipos")]
        public ActionResult<List<string>> TipoMovimientosPermitidos() => Ok(ServicioEureka.TiposPermitidos.ToList());

        [HttpPost]
        public ActionResult<ResultadoOperacion> RegMovimiento([FromBody] MovimientoRequest? body) =>
            Ok(servicio.Registrar(body?.CuentaOrigen, body?.CuentaDestino, body?.Tipo, body?.Importe ?? double.NaN));
    }
}
