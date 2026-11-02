using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;

namespace ec.edu.monster.ClienteConsola.Modelos
{
    /// <summary>
    /// Cliente REST del servicio "Conversion de Unidades Monster". Arma el cuerpo JSON,
    /// lo envia por HTTP con el token en la cabecera Authorization y lee la respuesta
    /// estructurada. No contiene logica de conversion: todo el calculo ocurre en el servidor.
    /// </summary>
    public class RestClient
    {
        public const string UrlPorDefecto = "http://localhost:5080/";

        private readonly HttpClient http;

        public string UrlBase { get; }

        public RestClient(string? urlBase, HttpClient? cliente = null)
        {
            var u = string.IsNullOrWhiteSpace(urlBase) ? UrlPorDefecto : urlBase.Trim();
            UrlBase = u.EndsWith('/') ? u : u + "/";
            http = cliente ?? new HttpClient { Timeout = TimeSpan.FromSeconds(15) };
        }

        /// <summary>URL base: variable de entorno CONUNI_URL o la URL por defecto.</summary>
        public static string UrlConfigurada()
        {
            var p = Environment.GetEnvironmentVariable("CONUNI_URL");
            return string.IsNullOrWhiteSpace(p) ? UrlPorDefecto : p;
        }

        // ---------------- operaciones ----------------

        public Task<RespuestaServicio> Login(string usuario, string clave) =>
            Enviar(HttpMethod.Post, "api/auth/login", new { usuario, clave }, null);

        public Task<RespuestaServicio> ObtenerCategorias(string? token) =>
            Enviar(HttpMethod.Get, "api/catalogo", null, token);

        public Task<RespuestaServicio> ObtenerUnidades(string? token, string categoria) =>
            Enviar(HttpMethod.Get, $"api/catalogo/{Uri.EscapeDataString(categoria)}", null, token);

        /// <summary>Invoca la ruta de conversion de la categoria elegida (Longitud, Masa o Temperatura).</summary>
        public Task<RespuestaServicio> Convertir(string categoria, double valor, string origen, string destino, string? token)
        {
            var ruta = (categoria ?? "").Trim().ToLowerInvariant() switch
            {
                "longitud" => "api/conversiones/longitud",
                "masa" => "api/conversiones/masa",
                "temperatura" => "api/conversiones/temperatura",
                _ => ""
            };
            if (ruta == "")
            {
                return Task.FromResult(RespuestaServicio.ErrorLocal("DATA-001", "Categoría no soportada"));
            }
            return Enviar(HttpMethod.Post, ruta, new { valor, unidadOrigen = origen, unidadDestino = destino }, token);
        }

        // ---------------- HTTP + JSON ----------------

        private async Task<RespuestaServicio> Enviar(HttpMethod metodo, string ruta, object? cuerpo, string? token)
        {
            using var peticion = new HttpRequestMessage(metodo, UrlBase + ruta);
            if (cuerpo != null)
            {
                peticion.Content = JsonContent.Create(cuerpo);
            }
            if (!string.IsNullOrWhiteSpace(token))
            {
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }
            try
            {
                using var respuesta = await http.SendAsync(peticion);
                var json = await respuesta.Content.ReadAsStringAsync();
                return Interpretar(json, (int)respuesta.StatusCode);
            }
            catch (HttpRequestException)
            {
                return ErrorDeRed();
            }
            catch (TaskCanceledException)
            {
                return ErrorDeRed();
            }
        }

        private RespuestaServicio ErrorDeRed() => RespuestaServicio.ErrorLocal(RespuestaServicio.CodRed,
            $"No se pudo conectar con el servidor ({UrlBase}). Verifique que esté en ejecución.");

        /// <summary>Convierte el JSON de respuesta en un <see cref="RespuestaServicio"/>.</summary>
        public static RespuestaServicio Interpretar(string json, int estadoHttp)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(json))
                {
                    return RespuestaServicio.ErrorLocal("SRV-500", $"Respuesta sin contenido (HTTP {estadoHttp})");
                }
                var opciones = new JsonSerializerOptions { PropertyNameCaseInsensitive = true };
                var r = JsonSerializer.Deserialize<RespuestaServicio>(json, opciones);
                return r ?? RespuestaServicio.ErrorLocal("SRV-500", $"Respuesta sin contenido (HTTP {estadoHttp})");
            }
            catch (Exception)
            {
                return RespuestaServicio.ErrorLocal("SRV-500", "No se pudo leer la respuesta del servidor");
            }
        }
    }
}
