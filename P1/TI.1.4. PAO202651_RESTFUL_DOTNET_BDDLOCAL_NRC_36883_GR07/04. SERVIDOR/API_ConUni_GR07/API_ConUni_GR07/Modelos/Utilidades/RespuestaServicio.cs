using System.Text.Json.Serialization;
using ec.edu.monster.Modelos.Utilidades.Enums;

namespace ec.edu.monster.Modelos.Utilidades
{
    /// <summary>
    /// Respuesta estructurada que devuelven todas las operaciones del servicio
    /// (CU-07). Se serializa a JSON en el cuerpo de la respuesta HTTP y se arma
    /// con <see cref="Mapeadores.RespuestaMapeador"/>.
    ///
    /// estado: OK o ERROR.
    /// codigo: OK-200, AUTH-001, AUTH-002, AUTH-003, UNIT-001, DATA-001, SRV-500.
    /// mensaje: texto informativo para el usuario.
    /// resultado: valor convertido (nulo si hay error o si la operacion no calcula).
    /// unidadOrigen / unidadDestino: unidades de la solicitud (conversiones).
    /// token: token generado (solo en el login).
    /// datos: listas de texto (categorias o unidades del catalogo).
    /// </summary>
    public class RespuestaServicio
    {
        [JsonPropertyName("estado")]
        public string? Estado { get; set; }

        [JsonPropertyName("codigo")]
        public string? Codigo { get; set; }

        [JsonPropertyName("mensaje")]
        public string? Mensaje { get; set; }

        [JsonPropertyName("resultado")]
        public double? Resultado { get; set; }

        [JsonPropertyName("unidadOrigen")]
        public string? UnidadOrigen { get; set; }

        [JsonPropertyName("unidadDestino")]
        public string? UnidadDestino { get; set; }

        [JsonPropertyName("token")]
        public string? Token { get; set; }

        [JsonPropertyName("datos")]
        public List<string> Datos { get; set; } = new();

        public bool EsExitosa() => EstadoRespuesta.Ok.Texto() == Estado;
    }
}
