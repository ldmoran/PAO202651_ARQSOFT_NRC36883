using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using ec.edu.monster.eureka.Datos;
using Microsoft.AspNetCore.Mvc.Testing;

namespace API_EurekaBank_RESTDOTNET_GR07.Pruebas
{
    /// <summary>
    /// Arranca la API REST completa (ASP.NET Core) sobre una base SQL Server temporal creada con los scripts reales y le habla
    /// por HTTP/JSON, como lo hacen los clientes.
    /// </summary>
    public sealed class RestExtremoAExtremoPruebas : IDisposable
    {
        private readonly BaseTemporal bd = new();
        private readonly WebApplicationFactory<Program> fabrica;
        private readonly HttpClient http;

        public RestExtremoAExtremoPruebas()
        {
            BaseDatos.Configurar(bd);
            fabrica = new WebApplicationFactory<Program>();
            http = fabrica.CreateClient();
        }

        public void Dispose()
        {
            http.Dispose();
            fabrica.Dispose();
            bd.Dispose();
        }

        private async Task<JsonElement> Json(HttpResponseMessage r)
        {
            Assert.Equal(HttpStatusCode.OK, r.StatusCode);
            return JsonDocument.Parse(await r.Content.ReadAsStringAsync()).RootElement;
        }

        [Fact]
        public async Task LoginCorrecto()
        {
            var r = await http.PostAsJsonAsync("/api/sesiones", new { usuario = "lmoran", contrasena = "Eureka2026!" });
            Assert.True((await Json(r)).GetBoolean());
        }

        [Fact]
        public async Task LoginIncorrecto()
        {
            var r = await http.PostAsJsonAsync("/api/sesiones", new { usuario = "lmoran", contrasena = "mala" });
            Assert.False((await Json(r)).GetBoolean());
        }

        [Fact]
        public async Task LoginSinCuerpoNoCaeElServidor()
        {
            var r = await http.PostAsJsonAsync("/api/sesiones", new { });
            Assert.False((await Json(r)).GetBoolean());
        }

        [Fact]
        public async Task ListaLasCuentasActivas()
        {
            var j = await Json(await http.GetAsync("/api/cuentas"));
            Assert.Equal(5, j.GetArrayLength());
            Assert.Equal("00100001", j[0].GetProperty("codigo").GetString());
        }

        [Fact]
        public async Task MovimientosDeUnaCuenta()
        {
            var j = await Json(await http.GetAsync("/api/movimientos?cuenta=00100001"));
            Assert.Equal(3, j.GetArrayLength());
            Assert.Equal(7000m, j[0].GetProperty("saldoActual").GetDecimal());
        }

        [Fact]
        public async Task MovimientosDeTodasLasCuentas()
        {
            var j = await Json(await http.GetAsync("/api/movimientos"));
            Assert.Equal(9, j.GetArrayLength());
        }

        [Fact]
        public async Task TiposPermitidos()
        {
            var j = await Json(await http.GetAsync("/api/movimientos/tipos"));
            Assert.Equal(new[] { "DEPOSITO", "RETIRO", "TRANSFERENCIA" }, j.EnumerateArray().Select(x => x.GetString()).ToArray());
        }

        [Fact]
        public async Task RegistraUnDepositoYLoVeEnLosMovimientos()
        {
            var r = await http.PostAsJsonAsync("/api/movimientos", new { cuentaOrigen = "00100001", cuentaDestino = "", tipo = "DEPOSITO", importe = 10.5 });
            var j = await Json(r);
            Assert.Equal(1, j.GetProperty("codigo").GetInt32());
            var m = await Json(await http.GetAsync("/api/movimientos?cuenta=00100001"));
            Assert.Equal(4, m.GetArrayLength());
            Assert.Equal(7010.5m, m[0].GetProperty("saldoActual").GetDecimal());
        }

        [Fact]
        public async Task TransferenciaEntreCuentas()
        {
            var r = await http.PostAsJsonAsync("/api/movimientos", new { cuentaOrigen = "00100001", cuentaDestino = "00100002", tipo = "TRANSFERENCIA", importe = 100 });
            Assert.Equal(1, (await Json(r)).GetProperty("codigo").GetInt32());
            Assert.Equal(6900m, bd.Saldo("00100001"));
            Assert.Equal(6900m, bd.Saldo("00100002"));
        }

        [Fact]
        public async Task ErrorDeNegocioDevuelveCodigoMenosUnoYNoCambiaNada()
        {
            var r = await http.PostAsJsonAsync("/api/movimientos", new { cuentaOrigen = "00100001", cuentaDestino = "", tipo = "RETIRO", importe = 999999 });
            var j = await Json(r);
            Assert.Equal(-1, j.GetProperty("codigo").GetInt32());
            Assert.Contains("Saldo insuficiente", j.GetProperty("mensaje").GetString());
            Assert.Equal(7000m, bd.Saldo("00100001"));
        }

        [Fact]
        public async Task CuerpoVacioEsUnErrorControlado()
        {
            var r = await http.PostAsJsonAsync("/api/movimientos", new { });
            Assert.Equal(-1, (await Json(r)).GetProperty("codigo").GetInt32());
        }
    }
}
