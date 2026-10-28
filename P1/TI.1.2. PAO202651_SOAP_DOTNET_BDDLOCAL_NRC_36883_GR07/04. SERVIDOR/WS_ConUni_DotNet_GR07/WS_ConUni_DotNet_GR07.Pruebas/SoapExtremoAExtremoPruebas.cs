using System.Text;
using System.Xml.Linq;
using Microsoft.AspNetCore.Mvc.Testing;

namespace ec.edu.monster.Pruebas
{
    /// <summary>
    /// Prueba SOAP real: se arranca el servidor completo y se invocan los servicios
    /// con mensajes XML por HTTP, igual que lo hacen los cuatro clientes.
    /// </summary>
    [Collection("VariableDeEntorno")]
    public class SoapExtremoAExtremoPruebas : IClassFixture<WebApplicationFactory<Program>>
    {
        private const string Ns = "http://WebServices.monster.edu.ec/";
        private readonly HttpClient http;

        public SoapExtremoAExtremoPruebas(WebApplicationFactory<Program> fabrica)
        {
            if (BaseDatosPrueba.Disponible)
            {
                BaseDatosPrueba.Compartida();
            }
            http = fabrica.CreateClient();
        }

        private async Task<XElement> Llamar(string servicio, string operacion, params (string Nombre, string Valor)[] parametros)
        {
            var cuerpo = new StringBuilder();
            foreach (var (nombre, valor) in parametros)
            {
                cuerpo.Append($"<{nombre}>{System.Security.SecurityElement.Escape(valor)}</{nombre}>");
            }
            var sobre = $"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        $"<soapenv:Body><{operacion} xmlns=\"{Ns}\">{cuerpo}</{operacion}></soapenv:Body></soapenv:Envelope>";
            var peticion = new HttpRequestMessage(HttpMethod.Post, "/" + servicio)
            {
                Content = new StringContent(sobre, Encoding.UTF8, "text/xml")
            };
            peticion.Headers.Add("SOAPAction", $"\"{Ns}{servicio}/{operacion}\"");
            var respuesta = await http.SendAsync(peticion);
            var xml = await respuesta.Content.ReadAsStringAsync();
            Assert.True(respuesta.IsSuccessStatusCode, xml);
            var raiz = XDocument.Parse(xml).Descendants().First(e => e.Name.LocalName == $"{operacion}Result");
            return raiz;
        }

        private static string? Campo(XElement r, string nombre) =>
            r.Elements().FirstOrDefault(e => e.Name.LocalName == nombre)?.Value;

        private async Task<string> IniciarSesion()
        {
            var r = await Llamar("WSLogin", "login", ("usuario", "monster"), ("clave", "monster9"));
            Assert.Equal("OK", Campo(r, "estado"));
            return Campo(r, "token")!;
        }

        [SqlServerFact]
        public async Task PublicaElWsdlDeLosCincoServicios()
        {
            foreach (var servicio in new[] { "WSLogin", "WSCatalogo", "WSLongitud", "WSMasa", "WSTemperatura" })
            {
                var respuesta = await http.GetAsync($"/{servicio}?wsdl");
                var wsdl = await respuesta.Content.ReadAsStringAsync();
                Assert.True(respuesta.IsSuccessStatusCode, servicio);
                Assert.Contains(Ns, wsdl);
                Assert.Contains("definitions", wsdl);
            }
        }

        [SqlServerFact]
        public async Task LoginYCatalogoPorSoap()
        {
            var token = await IniciarSesion();
            var c = await Llamar("WSCatalogo", "obtenerCategorias", ("token", token));
            Assert.Equal("OK", Campo(c, "estado"));
            var categorias = c.Elements().First(e => e.Name.LocalName == "datos").Elements().Select(e => e.Value).ToList();
            Assert.Equal(new[] { "Longitud", "Masa", "Temperatura" }, categorias);
        }

        [SqlServerFact]
        public async Task ConversionPorSoap()
        {
            var token = await IniciarSesion();
            var r = await Llamar("WSLongitud", "convertirLongitud",
                ("valor", "1"), ("unidadOrigen", "millas"), ("unidadDestino", "metros"), ("token", token));
            Assert.Equal("OK", Campo(r, "estado"));
            Assert.Equal("OK-200", Campo(r, "codigo"));
            Assert.Equal("1609.344", Campo(r, "resultado"));

            r = await Llamar("WSTemperatura", "convertirTemperatura",
                ("valor", "100"), ("unidadOrigen", "celsius"), ("unidadDestino", "fahrenheit"), ("token", token));
            Assert.Equal("212", Campo(r, "resultado"));
        }

        [SqlServerFact]
        public async Task ErroresPorSoapSiempreTraenRespuestaEstructurada()
        {
            var r = await Llamar("WSMasa", "convertirMasa",
                ("valor", "1"), ("unidadOrigen", "gramos"), ("unidadDestino", "libras"), ("token", "falso"));
            Assert.Equal("ERROR", Campo(r, "estado"));
            Assert.Equal("AUTH-001", Campo(r, "codigo"));

            var token = await IniciarSesion();
            r = await Llamar("WSMasa", "convertirMasa",
                ("valor", "1"), ("unidadOrigen", "toneladas"), ("unidadDestino", "libras"), ("token", token));
            Assert.Equal("UNIT-001", Campo(r, "codigo"));

            r = await Llamar("WSLogin", "login", ("usuario", "monster"), ("clave", "otra"));
            Assert.Equal("AUTH-003", Campo(r, "codigo"));
        }
    }
}
