using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc.Testing;

namespace ec.edu.monster.Pruebas
{
    /// <summary>
    /// Prueba REST real: se arranca el servidor completo y se invocan los endpoints
    /// con peticiones HTTP + JSON, igual que lo hacen los cuatro clientes.
    /// </summary>
    [Collection("VariableDeEntorno")]
    public class RestExtremoAExtremoPruebas : IClassFixture<WebApplicationFactory<Program>>
    {
        private readonly HttpClient http;

        public RestExtremoAExtremoPruebas(WebApplicationFactory<Program> fabrica)
        {
            http = fabrica.CreateClient();
        }

        private async Task<JsonElement> Get(string ruta, string? token = null)
        {
            using var peticion = new HttpRequestMessage(HttpMethod.Get, ruta);
            if (token is not null)
            {
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }
            var respuesta = await http.SendAsync(peticion);
            Assert.True(respuesta.IsSuccessStatusCode);
            return JsonDocument.Parse(await respuesta.Content.ReadAsStringAsync()).RootElement.Clone();
        }

        private async Task<JsonElement> Post(string ruta, object cuerpo, string? token = null)
        {
            using var peticion = new HttpRequestMessage(HttpMethod.Post, ruta) { Content = JsonContent.Create(cuerpo) };
            if (token is not null)
            {
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }
            var respuesta = await http.SendAsync(peticion);
            Assert.True(respuesta.IsSuccessStatusCode);
            return JsonDocument.Parse(await respuesta.Content.ReadAsStringAsync()).RootElement.Clone();
        }

        private static string? Campo(JsonElement r, string nombre) =>
            r.TryGetProperty(nombre, out var v) && v.ValueKind != JsonValueKind.Null ? v.ToString() : null;

        private async Task<string> IniciarSesion()
        {
            var r = await Post("/api/auth/login", new { usuario = "monster", clave = "monster9" });
            Assert.Equal("OK", Campo(r, "estado"));
            return Campo(r, "token")!;
        }

        [Fact]
        public async Task PublicaElDocumentoSwagger()
        {
            var respuesta = await http.GetAsync("/swagger/v1/swagger.json");
            var json = await respuesta.Content.ReadAsStringAsync();
            Assert.True(respuesta.IsSuccessStatusCode);
            Assert.Contains("/api/auth/login", json);
            Assert.Contains("/api/conversiones/longitud", json);
        }

        [Fact]
        public async Task LoginYCatalogoPorRest()
        {
            var token = await IniciarSesion();
            var c = await Get("/api/catalogo", token);
            Assert.Equal("OK", Campo(c, "estado"));
            var categorias = c.GetProperty("datos").EnumerateArray().Select(e => e.GetString()).ToList();
            Assert.Equal(new[] { "Longitud", "Masa", "Temperatura" }, categorias);
        }

        [Fact]
        public async Task ConversionPorRest()
        {
            var token = await IniciarSesion();
            var r = await Post("/api/conversiones/longitud",
                new { valor = 1, unidadOrigen = "millas", unidadDestino = "metros" }, token);
            Assert.Equal("OK", Campo(r, "estado"));
            Assert.Equal("OK-200", Campo(r, "codigo"));
            Assert.Equal(1609.344, r.GetProperty("resultado").GetDouble());

            r = await Post("/api/conversiones/temperatura",
                new { valor = 100, unidadOrigen = "celsius", unidadDestino = "fahrenheit" }, token);
            Assert.Equal(212.0, r.GetProperty("resultado").GetDouble());
        }

        [Fact]
        public async Task LogoutInvalidaElTokenDeInmediato()
        {
            var token = await IniciarSesion();
            var salida = await Post("/api/auth/logout", new { }, token);
            Assert.Equal("OK", Campo(salida, "estado"));
            var r = await Get("/api/catalogo", token);
            Assert.Equal("AUTH-001", Campo(r, "codigo"));
        }

        [Fact]
        public async Task ErroresPorRestSiempreTraenRespuestaEstructurada()
        {
            var r = await Post("/api/conversiones/masa",
                new { valor = 1, unidadOrigen = "gramos", unidadDestino = "libras" }, "falso");
            Assert.Equal("ERROR", Campo(r, "estado"));
            Assert.Equal("AUTH-001", Campo(r, "codigo"));

            var token = await IniciarSesion();
            r = await Post("/api/conversiones/masa",
                new { valor = 1, unidadOrigen = "toneladas", unidadDestino = "libras" }, token);
            Assert.Equal("UNIT-001", Campo(r, "codigo"));

            r = await Post("/api/auth/login", new { usuario = "monster", clave = "otra" });
            Assert.Equal("AUTH-003", Campo(r, "codigo"));
        }
    }
}
