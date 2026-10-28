using System.Net;
using System.Text;
using ec.edu.monster.ClienteConsola.Controladores;
using ec.edu.monster.ClienteConsola.Modelos;

namespace ec.edu.monster.ClienteConsola.Pruebas
{
    /// <summary>Manejador HTTP falso: devuelve una respuesta fija y guarda lo que le enviaron.</summary>
    internal sealed class ServidorFalso : HttpMessageHandler
    {
        private readonly Func<HttpResponseMessage> respuesta;
        public string? UltimoCuerpo { get; private set; }
        public Uri? UltimaUrl { get; private set; }

        public ServidorFalso(string xml, HttpStatusCode estado = HttpStatusCode.OK)
            : this(() => new HttpResponseMessage(estado) { Content = new StringContent(xml, Encoding.UTF8, "text/xml") }) { }

        public ServidorFalso(Func<HttpResponseMessage> respuesta) => this.respuesta = respuesta;

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            UltimaUrl = request.RequestUri;
            UltimoCuerpo = request.Content == null ? null : await request.Content.ReadAsStringAsync(cancellationToken);
            return respuesta();
        }

        public static string Sobre(string operacion, string contenido) =>
            "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>" +
            $"<{operacion}Response xmlns=\"http://WebServices.monster.edu.ec/\"><{operacion}Result>{contenido}</{operacion}Result></{operacion}Response>" +
            "</s:Body></s:Envelope>";
    }

    public class SoapClientPruebas
    {
        private static SoapClient Cliente(ServidorFalso f) => new("http://servidor:5080", new HttpClient(f));

        [Fact]
        public async Task LeeUnaConversionCorrecta()
        {
            var f = new ServidorFalso(ServidorFalso.Sobre("convertirMasa",
                "<estado>OK</estado><codigo>OK-200</codigo><mensaje>Conversión realizada correctamente</mensaje>" +
                "<resultado>2.20462262185</resultado><unidadOrigen>kilogramos</unidadOrigen><unidadDestino>libras</unidadDestino><datos/>"));
            var r = await Cliente(f).Convertir("Masa", 1, "kilogramos", "libras", "tk");
            Assert.True(r.EsExitosa);
            Assert.Equal(2.20462262185, r.Resultado);
            Assert.Equal("kilogramos", r.UnidadOrigen);
            Assert.Equal("libras", r.UnidadDestino);
            Assert.Equal("http://servidor:5080/WSMasa", f.UltimaUrl!.ToString());
        }

        [Fact]
        public async Task LeeLasListasDelCatalogo()
        {
            var f = new ServidorFalso(ServidorFalso.Sobre("obtenerCategorias",
                "<estado>OK</estado><codigo>OK-200</codigo><mensaje>ok</mensaje><datos><string>Longitud</string><string>Masa</string></datos>"));
            var r = await Cliente(f).ObtenerCategorias("tk");
            Assert.Equal(new[] { "Longitud", "Masa" }, r.Datos);
        }

        [Fact]
        public async Task LeeElTokenDelLogin()
        {
            var f = new ServidorFalso(ServidorFalso.Sobre("login",
                "<estado>OK</estado><codigo>OK-200</codigo><mensaje>Autenticación exitosa</mensaje><token>abc-123</token>"));
            var r = await Cliente(f).Login("monster", "monster9");
            Assert.Equal("abc-123", r.Token);
        }

        [Theory]
        [InlineData("AUTH-001")]
        [InlineData("AUTH-002")]
        public async Task ElErrorDeTokenPideNuevoLogin(string codigo)
        {
            var f = new ServidorFalso(ServidorFalso.Sobre("obtenerCategorias",
                $"<estado>ERROR</estado><codigo>{codigo}</codigo><mensaje>x</mensaje><datos/>"));
            var r = await Cliente(f).ObtenerCategorias("viejo");
            Assert.False(r.EsExitosa);
            Assert.True(r.RequiereNuevoLogin);
        }

        [Fact]
        public async Task UnaFallaSoapSeConvierteEnError()
        {
            var f = new ServidorFalso("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><s:Fault>" +
                                      "<faultcode>s:Server</faultcode><faultstring>fallo interno</faultstring></s:Fault></s:Body></s:Envelope>",
                HttpStatusCode.InternalServerError);
            var r = await Cliente(f).Login("a", "b");
            Assert.Equal("SRV-500", r.Codigo);
            Assert.Contains("fallo interno", r.Mensaje);
        }

        [Fact]
        public async Task UnXmlInvalidoNoRompeElCliente()
        {
            var r = await Cliente(new ServidorFalso("esto no es xml")).Login("a", "b");
            Assert.Equal("SRV-500", r.Codigo);
        }

        [Fact]
        public async Task ElMensajeEscapaElXmlYLlevaElEspacioDeNombres()
        {
            var f = new ServidorFalso(ServidorFalso.Sobre("login", "<estado>OK</estado>"));
            await Cliente(f).Login("a<b&c", "x>y");
            Assert.Contains("a&lt;b&amp;c", f.UltimoCuerpo);
            Assert.Contains("x&gt;y", f.UltimoCuerpo);
            Assert.Contains("xmlns=\"http://WebServices.monster.edu.ec/\"", f.UltimoCuerpo);
        }

        [Fact]
        public async Task ElServidorApagadoDaNet001()
        {
            var f = new ServidorFalso(() => throw new HttpRequestException("sin conexión"));
            var r = await Cliente(f).Login("a", "b");
            Assert.Equal("NET-001", r.Codigo);
        }

        [Fact]
        public async Task UnaCategoriaDesconocidaSeRechazaSinLlamarAlServidor()
        {
            var f = new ServidorFalso("<x/>");
            var r = await Cliente(f).Convertir("Volumen", 1, "a", "b", "tk");
            Assert.Equal("DATA-001", r.Codigo);
            Assert.Null(f.UltimaUrl);
        }

        [Fact]
        public async Task ElValorDecimalViajaConPuntoSinImportarLaCultura()
        {
            var f = new ServidorFalso(ServidorFalso.Sobre("convertirLongitud", "<estado>OK</estado>"));
            await Cliente(f).Convertir("Longitud", 12.5, "metros", "pies", "tk");
            Assert.Contains("<valor>12.5</valor>", f.UltimoCuerpo);
        }

        [Fact]
        public void FormateaNumerosSinNotacionCientificaNiCerosSobrantes()
        {
            Assert.Equal("26.85", ControladorConversiones.Formatear(26.85));
            Assert.Equal("1609.344", ControladorConversiones.Formatear(1609.3440000000001));
            Assert.Equal("0.0000000001", ControladorConversiones.Formatear(1e-10));
            Assert.Equal("100", ControladorConversiones.Formatear(100.0));
        }

        [Fact]
        public async Task SiElTokenExpiraSeCierraLaSesionLocal()
        {
            var sesion = new Sesion();
            sesion.Iniciar("viejo");
            var f = new ServidorFalso(ServidorFalso.Sobre("obtenerCategorias",
                "<estado>ERROR</estado><codigo>AUTH-002</codigo><mensaje>Sesión expirada</mensaje>"));
            var controlador = new ControladorConversiones(Cliente(f), sesion);
            var r = await controlador.Categorias();
            Assert.True(r.RequiereNuevoLogin);
            Assert.False(sesion.Activa);
        }
    }
}
