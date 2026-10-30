using ec.edu.monster.Controladores.Manejadores;
using ec.edu.monster.Modelos.Peticiones;
using ec.edu.monster.Modelos.Utilidades;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.Controladores
{
    /// <summary>CU-01 y CU-01b: autenticarse y cerrar sesion. Reemplaza al servicio SOAP WSLogin.</summary>
    [ApiController]
    [Route("api/auth")]
    public class AuthController : ApiControllerBase
    {
        private readonly ManejadorLogin manejador = new();

        [HttpPost("login")]
        public ActionResult<RespuestaServicio> Login([FromBody] LoginRequest? body) =>
            Ok(manejador.Login(body?.Usuario, body?.Clave));

        [HttpPost("logout")]
        public ActionResult<RespuestaServicio> Logout() =>
            Ok(manejador.Logout(Token()));
    }
}
