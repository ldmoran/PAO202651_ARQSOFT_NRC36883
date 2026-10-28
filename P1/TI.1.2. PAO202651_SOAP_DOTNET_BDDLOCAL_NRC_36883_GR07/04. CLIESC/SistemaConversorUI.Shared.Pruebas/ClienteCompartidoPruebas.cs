using System.Net;
using System.Text;
using ec.edu.monster.Cliente.Modelos;
using ec.edu.monster.Cliente.Servicios;

namespace ec.edu.monster.Cliente.Pruebas
{
    /// <summary>Manejador HTTP falso: devuelve respuestas fijas por servicio y guarda lo que le enviaron.</summary>
    internal sealed class ServidorFalso : HttpMessageHandler
    {
        private readonly Func<Uri, HttpResponseMessage> respuesta;
        public List<(Uri Url, string Cuerpo)> Llamadas { get; } = new();

        public ServidorFalso(Func<Uri, HttpResponseMessage> respuesta) => this.respuesta = respuesta;

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            var cuerpo = request.Content == null ? "" : await request.Content.ReadAsStringAsync(cancellationToken);
            Llamadas.Add((request.RequestUri!, cuerpo));
            return respuesta(request.RequestUri!);
        }

        public static HttpResponseMessage Xml(string operacion, string contenido) => new(HttpStatusCode.OK)
        {
            Content = new StringContent(
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>" +
                $"<{operacion}Response xmlns=\"http://WebServices.monster.edu.ec/\"><{operacion}Result>{contenido}</{operacion}Result></{operacion}Response>" +
                "</s:Body></s:Envelope>", Encoding.UTF8, "text/xml")
        };
    }

    public class ClienteCompartidoPruebas
    {
        private const string LoginOk = "<estado>OK</estado><codigo>OK-200</codigo><mensaje>Autenticación exitosa</mensaje><token>tk-1</token>";

        [Fact]
        public async Task SoapClientLeeUnaConversion()
        {
            var f = new ServidorFalso(_ => ServidorFalso.Xml("convertirLongitud",
                "<estado>OK</estado><codigo>OK-200</codigo><mensaje>ok</mensaje><resultado>1</resultado><unidadOrigen>yardas</unidadOrigen><unidadDestino>millas</unidadDestino>"));
            var cliente = new SoapClient("http://servidor:5080", new HttpClient(f));
            var r = await cliente.Convertir("Longitud", 1760, "yardas", "millas", "tk");
            Assert.True(r.EsExitosa);
            Assert.Equal(1.0, r.Resultado);
            Assert.EndsWith("/WSLongitud", f.Llamadas[0].Url.ToString());
        }

        [Fact]
        public async Task SoapClientDaNet001SiElServidorEstaApagado()
        {
            var cliente = new SoapClient("http://servidor:5080",
                new HttpClient(new ServidorFalso(_ => throw new HttpRequestException("sin conexión"))));
            Assert.Equal("NET-001", (await cliente.Login("a", "b")).Codigo);
        }

        [Fact]
        public async Task SoapClientNoRompeConUnXmlInvalido()
        {
            var cliente = new SoapClient("http://servidor:5080",
                new HttpClient(new ServidorFalso(_ => new HttpResponseMessage(HttpStatusCode.OK) { Content = new StringContent("no es xml") })));
            Assert.Equal("SRV-500", (await cliente.Login("a", "b")).Codigo);
        }

        [Fact]
        public void LaSesionEmpiezaSinToken()
        {
            var s = new SesionCliente("http://servidor:5080/");
            Assert.False(s.HaySesion);
            Assert.Equal("http://servidor:5080/", s.UrlServidor);
        }

        [Fact]
        public void CambiarDeServidorCierraLaSesion()
        {
            var s = new SesionCliente("http://uno/");
            s.CambiarServidor("http://dos/");
            Assert.Equal("http://dos/", s.UrlServidor);
            Assert.False(s.HaySesion);
        }

        [Fact]
        public async Task ElLoginCorrectoGuardaElTokenYLoEnviaEnLasSiguientesLlamadas()
        {
            var f = new ServidorFalso(u => u.AbsolutePath.EndsWith("WSLogin")
                ? ServidorFalso.Xml("login", LoginOk)
                : ServidorFalso.Xml("obtenerCategorias", "<estado>OK</estado><codigo>OK-200</codigo><mensaje>ok</mensaje><datos><string>Masa</string></datos>"));
            var s = new SesionCliente("http://servidor:5080/", new HttpClient(f));
            Assert.True((await s.IniciarSesion("monster", "monster9")).EsExitosa);
            Assert.True(s.HaySesion);
            var c = await s.Categorias();
            Assert.Equal(new[] { "Masa" }, c.Datos);
            Assert.Contains("<token>tk-1</token>", f.Llamadas[1].Cuerpo);
        }

        [Fact]
        public async Task UnLoginIncorrectoNoDejaSesion()
        {
            var f = new ServidorFalso(_ => ServidorFalso.Xml("login",
                "<estado>ERROR</estado><codigo>AUTH-003</codigo><mensaje>Usuario o clave incorrectos</mensaje>"));
            var s = new SesionCliente("http://servidor:5080/", new HttpClient(f));
            var r = await s.IniciarSesion("monster", "mala");
            Assert.False(r.EsExitosa);
            Assert.False(s.HaySesion);
        }

        [Fact]
        public async Task SiElServidorDiceSesionExpiradaSeCierraLaSesionLocal()
        {
            var f = new ServidorFalso(u => u.AbsolutePath.EndsWith("WSLogin")
                ? ServidorFalso.Xml("login", LoginOk)
                : ServidorFalso.Xml("obtenerCategorias", "<estado>ERROR</estado><codigo>AUTH-002</codigo><mensaje>Sesión expirada</mensaje>"));
            var s = new SesionCliente("http://servidor:5080/", new HttpClient(f));
            await s.IniciarSesion("monster", "monster9");
            var r = await s.Categorias();
            Assert.True(r.RequiereNuevoLogin);
            Assert.False(s.HaySesion);
        }

        [Fact]
        public void CerrarSesionOlvidaElToken()
        {
            var s = new SesionCliente("http://uno/");
            s.CerrarSesion();
            Assert.Null(s.Token);
        }
    }
}
