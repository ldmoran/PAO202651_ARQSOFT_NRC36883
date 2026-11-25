using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.Modelos;
using ec.edu.monster.eureka.Servicios;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.eureka.Controladores
{
    /// <summary>
    /// Inicio de sesion de EurekaBank. Reemplaza la operacion SOAP "iniciarSesion": mismas reglas de
    /// negocio (ServicioEureka), ahora expuestas como un recurso REST.
    /// </summary>
    [ApiController]
    [Route("api/sesiones")]
    public class SesionesController : ControllerBase
    {
        private readonly ServicioEureka servicio = new(new EurekaRepositorio(BaseDatos.Fuente));

        [HttpPost]
        public ActionResult<bool> IniciarSesion([FromBody] LoginRequest? body)
        {
            try
            {
                return Ok(servicio.IniciarSesion(body?.Usuario, body?.Contrasena));
            }
            catch (Exception)
            {
                return Ok(false);
            }
        }
    }
}
