using ec.edu.monster.Controladores.Manejadores;
using ec.edu.monster.Modelos.Peticiones;
using ec.edu.monster.Modelos.Utilidades;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.Controladores
{
    /// <summary>
    /// CU-04: realizar conversiones de longitud, masa y temperatura. Reemplaza a los
    /// tres servicios SOAP WSLongitud, WSMasa y WSTemperatura con una sola clase.
    /// </summary>
    [ApiController]
    [Route("api/conversiones")]
    public class ConversionesController : ApiControllerBase
    {
        private readonly ManejadorConversion manejador = new();

        [HttpPost("longitud")]
        public ActionResult<RespuestaServicio> Longitud([FromBody] ConversionRequest body) => Convertir("Longitud", body);

        [HttpPost("masa")]
        public ActionResult<RespuestaServicio> Masa([FromBody] ConversionRequest body) => Convertir("Masa", body);

        [HttpPost("temperatura")]
        public ActionResult<RespuestaServicio> Temperatura([FromBody] ConversionRequest body) => Convertir("Temperatura", body);

        private ActionResult<RespuestaServicio> Convertir(string categoria, ConversionRequest? body) =>
            Ok(manejador.Convertir(categoria, body?.Valor ?? double.NaN, body?.UnidadOrigen, body?.UnidadDestino, Token()));
    }
}
