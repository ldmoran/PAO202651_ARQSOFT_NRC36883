using System.Net;
using System.Text;
using System.Text.Json;
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
        public string? UltimaAutorizacion { get; private set; }

        public ServidorFalso(string json, HttpStatusCode estado = HttpStatusCode.OK)
            : this(() => new HttpResponseMessage(estado) { Content = new StringContent(json, Encoding.UTF8, "application/json") }) { }

        public ServidorFalso(Func<HttpResponseMessage> respuesta) => this.respuesta = respuesta;

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            UltimaUrl = request.RequestUri;
            UltimaAutorizacion = request.Headers.Authorization?.ToString();
            UltimoCuerpo = request.Content == null ? null : await request.Content.ReadAsStringAsync(cancellationToken);
            return respuesta();
        }
    }

    public class RestClientPruebas
    {
        private static RestClient Cliente(ServidorFalso f) => new("http://servidor:5080", new HttpClient(f));

        [Fact]
        public async Task LeeUnaConversionCorrecta()
        {
            var f = new ServidorFalso(JsonSerializer.Serialize(new
            {
                estado = "OK", codigo = "OK-200", mensaje = "Conversión realizada correctamente",
                resultado = 2.20462262185, unidadOrigen = "kilogramos", unidadDestino = "libras", datos = Array.Empty<string>()
            }));
            var r = await Cliente(f).Convertir("Masa", 1, "kilogramos", "libras", "tk");
            Assert.True(r.EsExitosa);
            Assert.Equal(2.20462262185, r.Resultado);
            Assert.Equal("kilogramos", r.UnidadOrigen);
            Assert.Equal("libras", r.UnidadDestino);
            Assert.Equal("http://servidor:5080/api/conversiones/masa", f.UltimaUrl!.ToString());
            Assert.Equal("Bearer tk", f.UltimaAutorizacion);
        }

        [Fact]
        public async Task LeeLasListasDelCatalogo()
        {
            var f = new ServidorFalso(JsonSerializer.Serialize(new
            {
                estado = "OK", codigo = "OK-200", mensaje = "ok", datos = new[] { "Longitud", "Masa" }
            }));
            var r = await Cliente(f).ObtenerCategorias("tk");
            Assert.Equal(new[] { "Longitud", "Masa" }, r.Datos);
            Assert.Equal("http://servidor:5080/api/catalogo", f.UltimaUrl!.ToString());
        }

        [Fact]
        public async Task LeeElTokenDelLogin()
        {
            var f = new ServidorFalso(JsonSerializer.Serialize(new
            {
                estado = "OK", codigo = "OK-200", mensaje = "Autenticación exitosa", token = "abc-123"
            }));
            var r = await Cliente(f).Login("monster", "monster9");
            Assert.Equal("abc-123", r.Token);
            Assert.Null(f.UltimaAutorizacion); // el login no lleva token
        }

        [Theory]
        [InlineData("AUTH-001")]
        [InlineData("AUTH-002")]
        public async Task ElErrorDeTokenPideNuevoLogin(string codigo)
        {
            var f = new ServidorFalso(JsonSerializer.Serialize(new { estado = "ERROR", codigo, mensaje = "x" }));
            var r = await Cliente(f).ObtenerCategorias("viejo");
            Assert.False(r.EsExitosa);
            Assert.True(r.RequiereNuevoLogin);
        }

        [Fact]
        public async Task UnJsonInvalidoNoRompeElCliente()
        {
            var r = await Cliente(new ServidorFalso("esto no es json")).Login("a", "b");
            Assert.Equal("SRV-500", r.Codigo);
        }

        [Fact]
        public async Task UnaRespuestaVaciaDaError500Local()
        {
            var r = await Cliente(new ServidorFalso("", HttpStatusCode.InternalServerError)).Login("a", "b");
            Assert.Equal("SRV-500", r.Codigo);
        }

        [Fact]
        public async Task ElMensajeViajaComoJsonConElUsuarioYClave()
        {
            var f = new ServidorFalso(JsonSerializer.Serialize(new { estado = "OK" }));
            await Cliente(f).Login("monster", "monster9");
            using var doc = JsonDocument.Parse(f.UltimoCuerpo!);
            Assert.Equal("monster", doc.RootElement.GetProperty("usuario").GetString());
            Assert.Equal("monster9", doc.RootElement.GetProperty("clave").GetString());
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
            var f = new ServidorFalso("{}");
            var r = await Cliente(f).Convertir("Volumen", 1, "a", "b", "tk");
            Assert.Equal("DATA-001", r.Codigo);
            Assert.Null(f.UltimaUrl);
        }

        [Fact]
        public async Task ElValorDecimalViajaConPuntoSinImportarLaCultura()
        {
            var f = new ServidorFalso(JsonSerializer.Serialize(new { estado = "OK" }));
            await Cliente(f).Convertir("Longitud", 12.5, "metros", "pies", "tk");
            Assert.Contains("\"valor\":12.5", f.UltimoCuerpo);
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
            var f = new ServidorFalso(JsonSerializer.Serialize(new { estado = "ERROR", codigo = "AUTH-002", mensaje = "Sesión expirada" }));
            var controlador = new ControladorConversiones(Cliente(f), sesion);
            var r = await controlador.Categorias();
            Assert.True(r.RequiereNuevoLogin);
            Assert.False(sesion.Activa);
        }
    }
}
