using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace ec.edu.monster.Cliente.Modelos
{
    public record Cuenta(string Codigo, string NombreCliente, decimal Saldo, string Estado, string Moneda, string? Email, string? Telefono);

    public record Movimiento(string CodigoCuenta, int Numero, string Fecha, string Tipo, decimal Importe, string? Referencia,
        decimal SaldoActual, string NombreCliente)
    {
        public bool EsEntrada => Tipo.EndsWith("Entrada", StringComparison.OrdinalIgnoreCase);
    }

    /// <summary>Resultado de una llamada: datos si salio bien, o el mensaje de error.</summary>
    public record Res<T>(bool Ok, T? Datos, string? Error)
    {
        public static Res<T> Bien(T datos) => new(true, datos, null);
        public static Res<T> Mal(string error) => new(false, default, error);
    }

    internal class ResultadoOperacionJson
    {
        [JsonPropertyName("codigo")] public int Codigo { get; set; }
        [JsonPropertyName("mensaje")] public string Mensaje { get; set; } = "";
    }

    internal class CuentaJson
    {
        [JsonPropertyName("codigo")] public string Codigo { get; set; } = "";
        [JsonPropertyName("nombreCliente")] public string NombreCliente { get; set; } = "";
        [JsonPropertyName("saldo")] public decimal Saldo { get; set; }
        [JsonPropertyName("estado")] public string Estado { get; set; } = "";
        [JsonPropertyName("moneda")] public string Moneda { get; set; } = "";
        [JsonPropertyName("emailCliente")] public string? EmailCliente { get; set; }
        [JsonPropertyName("telefonoCliente")] public string? TelefonoCliente { get; set; }
    }

    internal class MovimientoJson
    {
        [JsonPropertyName("codigoCuenta")] public string CodigoCuenta { get; set; } = "";
        [JsonPropertyName("numero")] public int Numero { get; set; }
        [JsonPropertyName("fecha")] public string Fecha { get; set; } = "";
        [JsonPropertyName("tipo")] public string Tipo { get; set; } = "";
        [JsonPropertyName("importe")] public decimal Importe { get; set; }
        [JsonPropertyName("referencia")] public string? Referencia { get; set; }
        [JsonPropertyName("saldoActual")] public decimal SaldoActual { get; set; }
        [JsonPropertyName("nombreCliente")] public string NombreCliente { get; set; } = "";
    }

    /// <summary>
    /// Cliente REST de EurekaBank: arma y envia peticiones HTTP/JSON al servidor ASP.NET Core Web API.
    /// No contiene reglas del banco: todo se valida en el servidor.
    /// </summary>
    public class RestClient
    {
        public const string UrlPorDefecto = "http://localhost:5081";
        private static readonly JsonSerializerOptions Opciones = new() { PropertyNameCaseInsensitive = true };

        private readonly HttpClient http;

        public string Url { get; }

        public RestClient(string? url, HttpClient? cliente = null)
        {
            var u = string.IsNullOrWhiteSpace(url) ? UrlPorDefecto : url.Trim();
            Url = u.EndsWith('/') ? u[..^1] : u;
            http = cliente ?? new HttpClient { Timeout = TimeSpan.FromSeconds(15) };
        }

        /// <summary>URL del servicio: variable de entorno EUREKA_URL o la URL por defecto.</summary>
        public static string UrlConfigurada()
        {
            var p = Environment.GetEnvironmentVariable("EUREKA_URL");
            return string.IsNullOrWhiteSpace(p) ? UrlPorDefecto : p;
        }

        // ---------------- operaciones ----------------

        public async Task<Res<bool>> IniciarSesion(string usuario, string clave)
        {
            var r = await Enviar<bool>(HttpMethod.Post, "api/sesiones", new { usuario, contrasena = clave });
            return r.Error != null ? Res<bool>.Mal(r.Error) : Res<bool>.Bien(r.Valor);
        }

        public async Task<Res<List<Cuenta>>> Cuentas()
        {
            var r = await Enviar<List<CuentaJson>>(HttpMethod.Get, "api/cuentas", null);
            if (r.Error != null) return Res<List<Cuenta>>.Mal(r.Error);
            var lista = (r.Valor ?? new()).Select(c => new Cuenta(c.Codigo, c.NombreCliente, c.Saldo, c.Estado, c.Moneda, c.EmailCliente, c.TelefonoCliente)).ToList();
            return Res<List<Cuenta>>.Bien(lista);
        }

        public async Task<Res<List<Movimiento>>> Movimientos(string? cuenta)
        {
            var ruta = "api/movimientos" + (string.IsNullOrWhiteSpace(cuenta) ? "" : "?cuenta=" + Uri.EscapeDataString(cuenta));
            var r = await Enviar<List<MovimientoJson>>(HttpMethod.Get, ruta, null);
            if (r.Error != null) return Res<List<Movimiento>>.Mal(r.Error);
            var lista = (r.Valor ?? new()).Select(m => new Movimiento(m.CodigoCuenta, m.Numero, m.Fecha, m.Tipo, m.Importe, m.Referencia, m.SaldoActual, m.NombreCliente)).ToList();
            return Res<List<Movimiento>>.Bien(lista);
        }

        public async Task<Res<List<string>>> Tipos()
        {
            var r = await Enviar<List<string>>(HttpMethod.Get, "api/movimientos/tipos", null);
            return r.Error != null ? Res<List<string>>.Mal(r.Error) : Res<List<string>>.Bien(r.Valor ?? new());
        }

        /// <summary>Registra un deposito, retiro o transferencia. Devuelve el mensaje del servidor.</summary>
        public async Task<Res<string>> Registrar(string tipo, string origen, string? destino, decimal importe)
        {
            var cuerpo = new { cuentaOrigen = origen, cuentaDestino = destino, tipo, importe = (double)importe };
            var r = await Enviar<ResultadoOperacionJson>(HttpMethod.Post, "api/movimientos", cuerpo);
            if (r.Error != null) return Res<string>.Mal(r.Error);
            return r.Valor!.Codigo == 1 ? Res<string>.Bien(r.Valor.Mensaje) : Res<string>.Mal(r.Valor.Mensaje);
        }

        // ---------------- HTTP + JSON ----------------

        private record Llamada<T>(T? Valor, string? Error);

        private async Task<Llamada<T>> Enviar<T>(HttpMethod metodo, string ruta, object? cuerpo)
        {
            using var peticion = new HttpRequestMessage(metodo, $"{Url}/{ruta}");
            if (cuerpo != null) peticion.Content = JsonContent.Create(cuerpo);
            try
            {
                using var respuesta = await http.SendAsync(peticion);
                var json = await respuesta.Content.ReadAsStringAsync();
                if (!respuesta.IsSuccessStatusCode)
                {
                    return new Llamada<T>(default, $"Error del servidor (HTTP {(int)respuesta.StatusCode}).");
                }
                var valor = JsonSerializer.Deserialize<T>(json, Opciones);
                return new Llamada<T>(valor, null);
            }
            catch (Exception e) when (e is HttpRequestException or TaskCanceledException)
            {
                return new Llamada<T>(default, $"No se pudo conectar con el servidor ({Url}). Verifique que esté en ejecución.");
            }
            catch (JsonException)
            {
                return new Llamada<T>(default, "No se pudo leer la respuesta del servidor.");
            }
        }
    }
}
