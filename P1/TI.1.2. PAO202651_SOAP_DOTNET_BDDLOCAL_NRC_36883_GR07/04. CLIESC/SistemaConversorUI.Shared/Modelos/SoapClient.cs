using System.Globalization;
using System.Net.Http.Headers;
using System.Security;
using System.Text;
using System.Xml;
using System.Xml.Linq;

namespace ec.edu.monster.Cliente.Modelos
{
    /// <summary>
    /// Cliente SOAP del servicio "Conversion de Unidades Monster". Arma el mensaje XML
    /// (contrato WSDL del servidor), lo envia por HTTP y lee la respuesta estructurada.
    /// No contiene logica de conversion: todo el calculo ocurre en el servidor.
    /// </summary>
    public class SoapClient
    {
        public const string UrlPorDefecto = "http://localhost:5080/";
        private const string Ns = "http://WebServices.monster.edu.ec/";

        private readonly HttpClient http;

        public string UrlBase { get; }

        public SoapClient(string? urlBase, HttpClient? cliente = null)
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
            Llamar("WSLogin", "login", ("usuario", usuario), ("clave", clave));

        public Task<RespuestaServicio> ObtenerCategorias(string? token) =>
            Llamar("WSCatalogo", "obtenerCategorias", ("token", token));

        public Task<RespuestaServicio> ObtenerUnidades(string? token, string categoria) =>
            Llamar("WSCatalogo", "obtenerUnidades", ("token", token), ("categoria", categoria));

        /// <summary>Invoca el servicio de la categoria elegida (Longitud, Masa o Temperatura).</summary>
        public Task<RespuestaServicio> Convertir(string categoria, double valor, string origen, string destino, string? token)
        {
            var (servicio, operacion) = (categoria ?? "").Trim().ToLowerInvariant() switch
            {
                "longitud" => ("WSLongitud", "convertirLongitud"),
                "masa" => ("WSMasa", "convertirMasa"),
                "temperatura" => ("WSTemperatura", "convertirTemperatura"),
                _ => ("", "")
            };
            if (servicio == "")
            {
                return Task.FromResult(RespuestaServicio.ErrorLocal("DATA-001", "Categoría no soportada"));
            }
            return Llamar(servicio, operacion,
                ("valor", valor.ToString("R", CultureInfo.InvariantCulture)),
                ("unidadOrigen", origen), ("unidadDestino", destino), ("token", token));
        }

        // ---------------- SOAP ----------------

        private async Task<RespuestaServicio> Llamar(string servicio, string operacion, params (string Nombre, string? Valor)[] parametros)
        {
            var cuerpo = new StringBuilder();
            foreach (var (nombre, valor) in parametros)
            {
                cuerpo.Append('<').Append(nombre).Append('>').Append(SecurityElement.Escape(valor ?? "")).Append("</").Append(nombre).Append('>');
            }
            // El elemento de la operacion declara el espacio de nombres por defecto: los parametros
            // lo heredan, como espera el servidor.
            var sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        $"<soapenv:Header/><soapenv:Body><{operacion} xmlns=\"{Ns}\">{cuerpo}</{operacion}></soapenv:Body></soapenv:Envelope>";
            using var peticion = new HttpRequestMessage(HttpMethod.Post, UrlBase + servicio)
            {
                Content = new StringContent(sobre, Encoding.UTF8, new MediaTypeHeaderValue("text/xml"))
            };
            peticion.Headers.TryAddWithoutValidation("SOAPAction", $"\"{Ns}{servicio}/{operacion}\"");
            try
            {
                using var respuesta = await http.SendAsync(peticion);
                var xml = await respuesta.Content.ReadAsStringAsync();
                return Interpretar(xml, (int)respuesta.StatusCode, operacion);
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

        /// <summary>Convierte el XML de respuesta en un <see cref="RespuestaServicio"/>.</summary>
        public static RespuestaServicio Interpretar(string xml, int estadoHttp, string operacion = "")
        {
            try
            {
                var configuracion = new XmlReaderSettings { DtdProcessing = DtdProcessing.Prohibit, XmlResolver = null };
                using var lector = XmlReader.Create(new StringReader(xml), configuracion);
                var doc = XDocument.Load(lector);

                var fallo = doc.Descendants().FirstOrDefault(e => e.Name.LocalName == "Fault");
                if (fallo != null)
                {
                    var texto = fallo.Descendants().FirstOrDefault(e => e.Name.LocalName == "faultstring")?.Value
                                ?? fallo.Descendants().FirstOrDefault(e => e.Name.LocalName == "Text")?.Value;
                    return RespuestaServicio.ErrorLocal("SRV-500", "Error del servidor" + (texto == null ? "" : ": " + texto));
                }
                var retorno = doc.Descendants().FirstOrDefault(e => e.Name.LocalName == operacion + "Result")
                              ?? doc.Descendants().FirstOrDefault(e => e.Name.LocalName.EndsWith("Result"));
                if (retorno == null)
                {
                    return RespuestaServicio.ErrorLocal("SRV-500", $"Respuesta sin contenido (HTTP {estadoHttp})");
                }
                var r = new RespuestaServicio();
                foreach (var e in retorno.Elements())
                {
                    switch (e.Name.LocalName)
                    {
                        case "estado": r.Estado = e.Value; break;
                        case "codigo": r.Codigo = e.Value; break;
                        case "mensaje": r.Mensaje = e.Value; break;
                        case "resultado": r.Resultado = double.Parse(e.Value, CultureInfo.InvariantCulture); break;
                        case "unidadOrigen": r.UnidadOrigen = e.Value; break;
                        case "unidadDestino": r.UnidadDestino = e.Value; break;
                        case "token": r.Token = e.Value; break;
                        case "datos": r.Datos.AddRange(e.Elements().Select(x => x.Value)); break;
                    }
                }
                return r;
            }
            catch (Exception)
            {
                return RespuestaServicio.ErrorLocal("SRV-500", "No se pudo leer la respuesta del servidor");
            }
        }
    }
}
