using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.Modelos;
using ec.edu.monster.eureka.Servicios;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.eureka.Controladores
{
    /// <summary>Reemplaza la operacion SOAP "traerCuentasConClientes".</summary>
    [ApiController]
    [Route("api/cuentas")]
    public class CuentasController : ControllerBase
    {
        private readonly ServicioEureka servicio = new(new EurekaRepositorio(BaseDatos.Fuente));

        [HttpGet]
        public ActionResult<List<DatosCuenta>> TraerCuentasConClientes()
        {
            try
            {
                return Ok(servicio.Cuentas());
            }
            catch (Exception)
            {
                return Ok(new List<DatosCuenta>());
            }
        }
    }
}
