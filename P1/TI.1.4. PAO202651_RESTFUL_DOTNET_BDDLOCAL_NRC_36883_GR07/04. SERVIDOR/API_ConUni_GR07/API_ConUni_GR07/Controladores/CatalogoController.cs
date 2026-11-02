using ec.edu.monster.Controladores.Manejadores;
using ec.edu.monster.Modelos.Utilidades;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.Controladores
{
    /// <summary>CU-02 y CU-03: consulta del catalogo de categorias y unidades. Reemplaza a WSCatalogo.</summary>
    [ApiController]
    [Route("api/catalogo")]
    public class CatalogoController : ApiControllerBase
    {
        private readonly ManejadorCatalogo manejador = new();

        [HttpGet]
        public ActionResult<RespuestaServicio> ObtenerCategorias() =>
            Ok(manejador.ObtenerCategorias(Token()));

        [HttpGet("{categoria}")]
        public ActionResult<RespuestaServicio> ObtenerUnidades(string categoria) =>
            Ok(manejador.ObtenerUnidades(Token(), categoria));
    }
}
