using System.Text.Json.Serialization;

namespace ec.edu.monster.Cliente.Modelos
{
    /// <summary>
    /// Respuesta estructurada del servicio REST (estado, codigo, mensaje, resultado,
    /// unidadOrigen, unidadDestino; mas token en el login y datos en el catalogo).
    /// </summary>
    public class RespuestaServicio
    {
        public const string CodTokenInvalido = "AUTH-001";
        public const string CodSesionExpirada = "AUTH-002";

        /// <summary>Codigo propio del cliente: no se pudo hablar con el servidor.</summary>
        public const string CodRed = "NET-001";

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

        public bool EsExitosa => Estado == "OK";

        /// <summary>true si el servidor rechazo el token (hay que volver a iniciar sesion).</summary>
        public bool RequiereNuevoLogin => Codigo == CodTokenInvalido || Codigo == CodSesionExpirada;

        public static RespuestaServicio ErrorLocal(string codigo, string mensaje) =>
            new() { Estado = "ERROR", Codigo = codigo, Mensaje = mensaje };
    }
}
