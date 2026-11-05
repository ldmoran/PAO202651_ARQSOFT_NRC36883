using System.Data.Common;
using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.Servicios;
using Microsoft.Data.SqlClient;

namespace WS_EurekaBank_SOAPDOTNET_GR07.Pruebas
{
    /// <summary>Crea una base temporal con los scripts reales (tablas y datos) y la borra al terminar.</summary>
    public sealed class BaseTemporal : IFuenteDatos, IDisposable
    {
        private const string Maestra = "Server=localhost;Database=master;Integrated Security=True;TrustServerCertificate=True";
        private readonly string nombre = "eurekabank_prueba_" + Guid.NewGuid().ToString("N")[..8];

        public BaseTemporal()
        {
            Ejecutar(Maestra, $"CREATE DATABASE [{nombre}]");
            foreach (var script in new[] { "01_CrearBaseYTablas.sql", "02_CargaDatos.sql" })
            {
                var texto = File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "BDD", script));
                // los scripts apuntan a "eurekabank": se redirigen a la base temporal
                texto = texto.Replace("IF DB_ID('eurekabank') IS NULL CREATE DATABASE eurekabank;", "")
                             .Replace("USE eurekabank;", $"USE [{nombre}];");
                foreach (var lote in System.Text.RegularExpressions.Regex.Split(texto, @"^\s*GO\s*$", System.Text.RegularExpressions.RegexOptions.Multiline | System.Text.RegularExpressions.RegexOptions.IgnoreCase))
                {
                    if (!string.IsNullOrWhiteSpace(lote)) Ejecutar(Cadena, lote);
                }
            }
        }

        private string Cadena => $"Server=localhost;Database={nombre};Integrated Security=True;TrustServerCertificate=True";

        private static void Ejecutar(string cadena, string sql)
        {
            using var cn = new SqlConnection(cadena);
            cn.Open();
            using var c = cn.CreateCommand();
            c.CommandText = sql;
            c.ExecuteNonQuery();
        }

        public DbConnection Abrir()
        {
            var cn = new SqlConnection(Cadena);
            cn.Open();
            return cn;
        }

        public decimal Saldo(string cuenta)
        {
            using var cn = Abrir();
            using var c = cn.CreateCommand();
            c.CommandText = "SELECT saldo FROM cuenta WHERE cod_cuenta = '" + cuenta + "'";
            return (decimal)c.ExecuteScalar()!;
        }

        public int Contar(string tabla)
        {
            using var cn = Abrir();
            using var c = cn.CreateCommand();
            c.CommandText = "SELECT COUNT(*) FROM " + tabla;
            return (int)c.ExecuteScalar()!;
        }

        public void Dispose()
        {
            SqlConnection.ClearAllPools();
            Ejecutar(Maestra, $"ALTER DATABASE [{nombre}] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [{nombre}]");
        }
    }

    public class ServicioEurekaPruebas : IDisposable
    {
        private readonly BaseTemporal bd = new();
        private readonly ServicioEureka servicio;

        public ServicioEurekaPruebas() => servicio = new ServicioEureka(new EurekaRepositorio(bd));

        public void Dispose() => bd.Dispose();

        // ---------- sesion ----------
        [Fact] public void LoginCorrecto() => Assert.True(servicio.IniciarSesion("lmoran", "Eureka2026!"));
        [Fact] public void LoginClaveIncorrecta() => Assert.False(servicio.IniciarSesion("lmoran", "otra"));
        [Fact] public void LoginUsuarioInexistente() => Assert.False(servicio.IniciarSesion("nadie", "Eureka2026!"));
        [Fact] public void LoginVacio() => Assert.False(servicio.IniciarSesion("", ""));

        [Fact]
        public void LoginRegistraIntentos()
        {
            servicio.IniciarSesion("lmoran", "mala");
            servicio.IniciarSesion("lmoran", "Eureka2026!");
            Assert.Equal(2, bd.Contar("intento_acceso"));
        }

        [Fact]
        public void LaClaveNoEstaEnTextoPlano()
        {
            var u = new EurekaRepositorio(bd).BuscarUsuario("lmoran")!;
            Assert.NotEqual("Eureka2026!", u.ClaveHash);
            Assert.Equal(64, u.ClaveHash.Length);
            Assert.Equal(Claves.Hash(u.Sal, "Eureka2026!"), u.ClaveHash);
        }

        // ---------- consultas ----------
        [Fact] public void CuentasActivasConCliente()
        {
            var l = servicio.Cuentas();
            Assert.Equal(5, l.Count);                       // la sexta esta ANULADA
            Assert.Contains(l, c => c.Codigo == "00100001" && c.NombreCliente.StartsWith("Coronel"));
        }

        [Fact] public void MovimientosDeUnaCuentaConSaldo()
        {
            var l = servicio.Movimientos("00100001");
            Assert.Equal(3, l.Count);
            Assert.Equal(3, l[0].Numero);                   // del mas reciente al mas antiguo
            Assert.Equal(7000m, l[0].SaldoActual);
            Assert.EndsWith("Salida", l[0].Tipo);
        }

        [Fact] public void MovimientosDeTodasLasCuentas() => Assert.Equal(9, servicio.Movimientos("").Count);
        [Fact] public void TiposPermitidos() => Assert.Equal(new[] { "DEPOSITO", "RETIRO", "TRANSFERENCIA" }, ServicioEureka.TiposPermitidos);

        // ---------- operaciones ----------
        [Fact] public void Deposito()
        {
            var r = servicio.Registrar("00100001", null, "DEPOSITO", 250.50);
            Assert.Equal(1, r.Codigo);
            Assert.Equal(7250.50m, bd.Saldo("00100001"));
            Assert.Equal(4, servicio.Movimientos("00100001").Count);
        }

        [Fact] public void Retiro()
        {
            Assert.Equal(1, servicio.Registrar("00100001", "", "retiro", 1000).Codigo);
            Assert.Equal(6000m, bd.Saldo("00100001"));
        }

        [Fact] public void RetiroConSaldoInsuficienteNoCambiaNada()
        {
            var r = servicio.Registrar("00100001", null, "RETIRO", 99999);
            Assert.Equal(-1, r.Codigo);
            Assert.Contains("Saldo insuficiente", r.Mensaje);
            Assert.Equal(7000m, bd.Saldo("00100001"));
            Assert.Equal(9, bd.Contar("movimiento"));
        }

        [Fact] public void Transferencia()
        {
            var r = servicio.Registrar("00100001", "00100002", "TRANSFERENCIA", 100);
            Assert.Equal(1, r.Codigo);
            Assert.Equal(6900m, bd.Saldo("00100001"));
            Assert.Equal(6900m, bd.Saldo("00100002"));
            Assert.Equal(11, bd.Contar("movimiento"));
            Assert.Equal("00100001", servicio.Movimientos("00100002")[0].Referencia);
        }

        [Fact] public void TransferenciaFallidaDeshaceTodo()
        {
            var r = servicio.Registrar("00100001", "99999999", "TRANSFERENCIA", 100);   // destino inexistente
            Assert.Equal(-1, r.Codigo);
            Assert.Equal(7000m, bd.Saldo("00100001"));                                   // la salida se deshizo
            Assert.Equal(9, bd.Contar("movimiento"));
        }

        [Fact] public void TransferenciaAlaMismaCuenta() =>
            Assert.Equal(-1, servicio.Registrar("00100001", "00100001", "TRANSFERENCIA", 10).Codigo);

        [Fact] public void CuentaAnuladaNoOpera() =>
            Assert.Equal(-1, servicio.Registrar("00100004", null, "DEPOSITO", 10).Codigo);

        [Theory]
        [InlineData("", null, "DEPOSITO", 10)]
        [InlineData("00100001", null, "DEPOSITO", 0)]
        [InlineData("00100001", null, "DEPOSITO", -5)]
        [InlineData("00100001", null, "", 10)]
        [InlineData("00100001", null, "PRESTAMO", 10)]
        [InlineData("00100001", "00100002", "DEPOSITO", 10)]
        [InlineData("00100001", null, "TRANSFERENCIA", 10)]
        public void ValidacionesDeEntrada(string origen, string? destino, string tipo, double importe) =>
            Assert.Equal(-1, servicio.Registrar(origen, destino, tipo, importe).Codigo);
    }
}
