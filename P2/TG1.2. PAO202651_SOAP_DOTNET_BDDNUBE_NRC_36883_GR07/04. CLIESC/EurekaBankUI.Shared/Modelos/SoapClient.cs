using System.Globalization;
using System.Net.Http.Headers;
using System.Security;
using System.Text;
using System.Xml;
using System.Xml.Linq;

namespace ec.edu.monster.Cliente.Modelos
{
    public record Cuenta(string Codigo, string NombreCliente, decimal Saldo, string Estado, string Moneda, string Email, string Telefono);

    public record Movimiento(string CodigoCuenta, int Numero, string Fecha, string Tipo, decimal Importe, string Referencia,
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

    /// <summary>
    /// Cliente SOAP de EurekaBank. Funciona igual con el servidor Java (Payara) y con el .NET (CoreWCF): los dos
    /// publican el mismo contrato (mismo espacio de nombres y operaciones). Solo cambia la URL del servicio.
    /// </summary>
    public class SoapClient
    {
        public const string UrlPorDefecto = "http://localhost:5081/WSEurekaBank";
        private const string Ns = "http://ws.eurekabank.monster.edu.ec/";

        private readonly HttpClient http;

        public string Url { get; }

        public SoapClient(string? url, HttpClient? cliente = null)
        {
            Url = string.IsNullOrWhiteSpace(url) ? UrlPorDefecto : url.Trim();
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
            var r = await Llamar("iniciarSesion", ("usuario", usuario), ("contrasena", clave));
            if (r.Error != null) return Res<bool>.Mal(r.Error);
            var texto = r.Doc!.Descendants().FirstOrDefault(e => e.Name.LocalName == "resultado")?.Value;
            return Res<bool>.Bien(string.Equals(texto?.Trim(), "true", StringComparison.OrdinalIgnoreCase));
        }

        public async Task<Res<List<Cuenta>>> Cuentas()
        {
            var r = await Llamar("traerCuentasConClientes");
            if (r.Error != null) return Res<List<Cuenta>>.Mal(r.Error);
            var lista = Items(r.Doc!, "Codigo").Select(e => new Cuenta(
                Texto(e, "Codigo"), Texto(e, "NombreCliente"), Dec(e, "Saldo"), Texto(e, "Estado"), Texto(e, "Moneda"),
                Texto(e, "EmailCliente"), Texto(e, "TelefonoCliente"))).ToList();
            return Res<List<Cuenta>>.Bien(lista);
        }

        public async Task<Res<List<Movimiento>>> Movimientos(string? cuenta)
        {
            var r = await Llamar("traerMovimientos", ("cuenta", cuenta ?? ""));
            if (r.Error != null) return Res<List<Movimiento>>.Mal(r.Error);
            var lista = Items(r.Doc!, "CodigoCuenta").Select(e => new Movimiento(
                Texto(e, "CodigoCuenta"), (int)Dec(e, "Numero"), Texto(e, "Fecha"), Texto(e, "Tipo"), Dec(e, "Importe"),
                Texto(e, "Referencia"), Dec(e, "SaldoActual"), Texto(e, "NombreCliente"))).ToList();
            return Res<List<Movimiento>>.Bien(lista);
        }

        public async Task<Res<List<string>>> Tipos()
        {
            var r = await Llamar("tipoMovimientosPermitidos");
            if (r.Error != null) return Res<List<string>>.Mal(r.Error);
            var cuerpo = r.Doc!.Descendants().First(e => e.Name.LocalName == "tipoMovimientosPermitidosResponse");
            var valores = cuerpo.Descendants().Where(e => !e.HasElements && e.Value.Length > 0).Select(e => e.Value.Trim()).ToList();
            return Res<List<string>>.Bien(valores);
        }

        /// <summary>Registra un deposito, retiro o transferencia. Devuelve el mensaje del servidor.</summary>
        public async Task<Res<string>> Registrar(string tipo, string origen, string? destino, decimal importe)
        {
            var r = await Llamar("regMovimiento", ("cuentaOrigen", origen), ("cuentaDestino", destino ?? ""), ("tipo", tipo),
                ("importe", importe.ToString(CultureInfo.InvariantCulture)));
            if (r.Error != null) return Res<string>.Mal(r.Error);
            var codigo = r.Doc!.Descendants().FirstOrDefault(e => e.Name.LocalName == "codigo")?.Value;
            var mensaje = r.Doc!.Descendants().FirstOrDefault(e => e.Name.LocalName == "mensaje")?.Value ?? "";
            return codigo == "1" ? Res<string>.Bien(mensaje) : Res<string>.Mal(mensaje);
        }

        // ---------------- SOAP ----------------

        private record Llamada(XDocument? Doc, string? Error);

        private async Task<Llamada> Llamar(string operacion, params (string Nombre, string? Valor)[] parametros)
        {
            var cuerpo = new StringBuilder();
            foreach (var (nombre, valor) in parametros)
            {
                cuerpo.Append('<').Append(nombre).Append('>').Append(SecurityElement.Escape(valor ?? "")).Append("</").Append(nombre).Append('>');
            }
            var sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        $"<soapenv:Header/><soapenv:Body><{operacion} xmlns=\"{Ns}\">{cuerpo}</{operacion}></soapenv:Body></soapenv:Envelope>";
            using var peticion = new HttpRequestMessage(HttpMethod.Post, Url)
            {
                Content = new StringContent(sobre, Encoding.UTF8, new MediaTypeHeaderValue("text/xml"))
            };
            peticion.Headers.TryAddWithoutValidation("SOAPAction", $"\"{Ns}WSEurekaBank/{operacion}\"");
            try
            {
                using var respuesta = await http.SendAsync(peticion);
                var xml = await respuesta.Content.ReadAsStringAsync();
                var config = new XmlReaderSettings { DtdProcessing = DtdProcessing.Prohibit, XmlResolver = null };
                using var lector = XmlReader.Create(new StringReader(xml), config);
                var doc = XDocument.Load(lector);
                var fallo = doc.Descendants().FirstOrDefault(e => e.Name.LocalName == "Fault");
                if (fallo != null)
                {
                    var t = fallo.Descendants().FirstOrDefault(e => e.Name.LocalName == "faultstring")?.Value;
                    return new Llamada(null, "Error del servidor" + (t == null ? "" : ": " + t));
                }
                return new Llamada(doc, null);
            }
            catch (Exception e) when (e is HttpRequestException or TaskCanceledException)
            {
                return new Llamada(null, $"No se pudo conectar con el servidor ({Url}). Verifique que esté en ejecución.");
            }
            catch (XmlException)
            {
                return new Llamada(null, "No se pudo leer la respuesta del servidor.");
            }
        }

        /// <summary>Elementos "registro": los que tienen un hijo con el nombre indicado (el contenedor cambia segun el servidor).</summary>
        private static IEnumerable<XElement> Items(XDocument doc, string campoClave) =>
            doc.Descendants().Where(e => e.Elements().Any(h => h.Name.LocalName == campoClave));

        private static string Texto(XElement e, string nombre) =>
            e.Elements().FirstOrDefault(h => h.Name.LocalName == nombre)?.Value ?? "";

        private static decimal Dec(XElement e, string nombre) =>
            decimal.TryParse(Texto(e, nombre), NumberStyles.Any, CultureInfo.InvariantCulture, out var v) ? v : 0m;
    }
}
