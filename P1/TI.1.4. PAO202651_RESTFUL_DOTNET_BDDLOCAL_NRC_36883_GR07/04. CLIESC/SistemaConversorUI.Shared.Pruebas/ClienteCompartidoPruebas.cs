using System.Net;
using System.Text;
using System.Text.Json;
using ec.edu.monster.Cliente.Modelos;
using ec.edu.monster.Cliente.Servicios;

namespace ec.edu.monster.Cliente.Pruebas
{
    /// <summary>Manejador HTTP falso: devuelve respuestas JSON fijas por ruta y guarda lo que le enviaron.</summary>
    internal sealed class ServidorFalso : HttpMessageHandler
    {
        private readonly Func<Uri, HttpResponseMessage> respuesta;
        public List<(Uri Url, string Cuerpo, string? Autorizacion)> Llamadas { get; } = new();

        public ServidorFalso(Func<Uri, HttpResponseMessage> respuesta) => this.respuesta = respuesta;

        protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            var cuerpo = request.Content == null ? "" : await request.Content.ReadAsStringAsync(cancellationToken);
            Llamadas.Add((request.RequestUri!, cuerpo, request.Headers.Authorization?.ToString()));
            return respuesta(request.RequestUri!);
        }

        public static HttpResponseMessage Json(object cuerpo) => new(HttpStatusCode.OK)
        {
            Content = new StringContent(JsonSerializer.Serialize(cuerpo), Encoding.UTF8, "application/json")
        };
    }

    public class ClienteCompartidoPruebas
    {
        private static readonly object LoginOk = new { estado = "OK", codigo = "OK-200", mensaje = "Autenticación exitosa", token = "tk-1" };

        [Fact]
        public async Task RestClientLeeUnaConversion()
        {
            var f = new ServidorFalso(_ => ServidorFalso.Json(new
            {
                estado = "OK", codigo = "OK-200", mensaje = "ok", resultado = 1.0, unidadOrigen = "yardas", unidadDestino = "millas"
            }));
            var cliente = new RestClient("http://servidor:5080", new HttpClient(f));
            var r = await cliente.Convertir("Longitud", 1760, "yardas", "millas", "tk");
            Assert.True(r.EsExitosa);
            Assert.Equal(1.0, r.Resultado);
            Assert.EndsWith("/api/conversiones/longitud", f.Llamadas[0].Url.ToString());
        }

        [Fact]
        public async Task RestClientDaNet001SiElServidorEstaApagado()
        {
            var cliente = new RestClient("http://servidor:5080",
                new HttpClient(new ServidorFalso(_ => throw new HttpRequestException("sin conexión"))));
            Assert.Equal("NET-001", (await cliente.Login("a", "b")).Codigo);
        }

        [Fact]
        public async Task RestClientNoRompeConUnJsonInvalido()
        {
            var cliente = new RestClient("http://servidor:5080",
                new HttpClient(new ServidorFalso(_ => new HttpResponseMessage(HttpStatusCode.OK) { Content = new StringContent("no es json") })));
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
            var f = new ServidorFalso(u => u.AbsolutePath.EndsWith("api/auth/login")
                ? ServidorFalso.Json(LoginOk)
                : ServidorFalso.Json(new { estado = "OK", codigo = "OK-200", mensaje = "ok", datos = new[] { "Masa" } }));
            var s = new SesionCliente("http://servidor:5080/", new HttpClient(f));
            Assert.True((await s.IniciarSesion("monster", "monster9")).EsExitosa);
            Assert.True(s.HaySesion);
            var c = await s.Categorias();
            Assert.Equal(new[] { "Masa" }, c.Datos);
            Assert.Equal("Bearer tk-1", f.Llamadas[1].Autorizacion);
        }

        [Fact]
        public async Task UnLoginIncorrectoNoDejaSesion()
        {
            var f = new ServidorFalso(_ => ServidorFalso.Json(new
            {
                estado = "ERROR", codigo = "AUTH-003", mensaje = "Usuario o clave incorrectos"
            }));
            var s = new SesionCliente("http://servidor:5080/", new HttpClient(f));
            var r = await s.IniciarSesion("monster", "mala");
            Assert.False(r.EsExitosa);
            Assert.False(s.HaySesion);
        }

        [Fact]
        public async Task SiElServidorDiceSesionExpiradaSeCierraLaSesionLocal()
        {
            var f = new ServidorFalso(u => u.AbsolutePath.EndsWith("api/auth/login")
                ? ServidorFalso.Json(LoginOk)
                : ServidorFalso.Json(new { estado = "ERROR", codigo = "AUTH-002", mensaje = "Sesión expirada" }));
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
