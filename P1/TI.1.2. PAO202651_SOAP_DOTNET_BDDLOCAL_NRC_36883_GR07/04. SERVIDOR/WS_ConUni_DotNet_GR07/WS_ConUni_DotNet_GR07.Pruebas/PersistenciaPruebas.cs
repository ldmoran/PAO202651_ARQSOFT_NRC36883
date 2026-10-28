using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Datos;
using ec.edu.monster.Seguridad;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.Pruebas
{
    /// <summary>Lo que el servidor deja guardado en la base: bitacora, sesiones, historial y catalogo editable.</summary>
    [Collection("VariableDeEntorno")]
    public class PersistenciaPruebas
    {
        private const string Sal = "a3f1c9d27b4e5086d1f2a9b7c3e4d5f6";
        private const string Hash = "4b6aa92fc59932c627242729eb132f7c3c6343e1aa6d907c00fda4dee58d3b30";

        private static long Contar(IFuenteDatos fuente, string sql)
        {
            using var cn = fuente.Abrir();
            using var cmd = cn.CreateCommand();
            cmd.CommandText = sql;
            return Convert.ToInt64(cmd.ExecuteScalar());
        }

        private static void Ejecutar(IFuenteDatos fuente, string sql)
        {
            using var cn = fuente.Abrir();
            using var cmd = cn.CreateCommand();
            cmd.CommandText = sql;
            cmd.ExecuteNonQuery();
        }

        [SqlServerFact]
        public void LaClaveNoSeGuardaEnClaro()
        {
            var fuente = BaseDatosPrueba.Nueva();
            Assert.Equal(0, Contar(fuente, "SELECT COUNT(*) FROM usuario WHERE hash_clave LIKE '%monster9%'"));
            Assert.True(Claves.Coincide("monster9", Sal, Hash));
            Assert.False(Claves.Coincide("Monster9", Sal, Hash));
            Assert.NotEqual(Claves.GenerarSal(), Claves.GenerarSal());
        }

        [SqlServerFact]
        public void CadaIntentoDeLoginQuedaEnLaBitacora()
        {
            var fuente = BaseDatosPrueba.Nueva();
            var login = new ManejadorLogin();
            Assert.True(login.Login("monster", "monster9").EsExitosa());
            Assert.Equal("AUTH-003", login.Login("monster", "mala").Codigo);
            Assert.Equal("AUTH-003", login.Login("fantasma", "x").Codigo);
            Assert.Equal(3, Contar(fuente, "SELECT COUNT(*) FROM intento_acceso"));
            Assert.Equal(1, Contar(fuente, "SELECT COUNT(*) FROM intento_acceso WHERE exitoso = 1"));
            Assert.Equal(1, Contar(fuente, "SELECT COUNT(*) FROM intento_acceso WHERE id_usuario IS NULL AND nombre_ingresado = 'fantasma'"));
            Assert.Equal(1, Contar(fuente, "SELECT COUNT(*) FROM sesion"));
        }

        [SqlServerFact]
        public void CadaConversionCorrectaQuedaEnElHistorial()
        {
            var fuente = BaseDatosPrueba.Nueva();
            var token = new ManejadorLogin().Login("monster", "monster9").Token!;
            var conversion = new ManejadorConversion();
            Assert.True(conversion.Convertir("Longitud", 1, "millas", "metros", token).EsExitosa());
            Assert.True(conversion.Convertir("Temperatura", 100, "celsius", "fahrenheit", token).EsExitosa());
            // las conversiones con error no se guardan
            Assert.False(conversion.Convertir("Masa", 1, "toneladas", "libras", token).EsExitosa());
            Assert.Equal(2, Contar(fuente, "SELECT COUNT(*) FROM conversion"));
            Assert.Equal(1, Contar(fuente, "SELECT COUNT(*) FROM v_historial_conversiones " +
                "WHERE nombre_usuario = 'monster' AND categoria = 'Temperatura' AND unidad_destino = 'fahrenheit' AND resultado = 212"));
        }

        [SqlServerFact]
        public void AgregarUnaCategoriaSoloRequiereInsertarFilas()
        {
            var fuente = BaseDatosPrueba.Nueva();
            Ejecutar(fuente, "INSERT INTO categoria (nombre, descripcion) VALUES ('Volumen', 'Capacidad: base litro')");
            Ejecutar(fuente, "INSERT INTO unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden) VALUES " +
                "(4, 'mililitros', 'ml', 0.001, 0, 1), (4, 'litros', 'L', 1, 0, 2), (4, 'galones', 'gal', 3.785411784, 0, 3)");
            var token = new ManejadorLogin().Login("monster", "monster9").Token!;
            var categorias = new ManejadorCatalogo().ObtenerCategorias(token);
            Assert.Equal(new[] { "Longitud", "Masa", "Temperatura", "Volumen" }, categorias.Datos);
            var r = new ManejadorConversion().Convertir("Volumen", 1, "galones", "litros", token);
            Assert.True(r.EsExitosa());
            Assert.Equal(3.785411784, r.Resultado!.Value, 9);
        }

        [SqlServerFact]
        public void UnaUnidadDesactivadaDejaDeOfrecerse()
        {
            var fuente = BaseDatosPrueba.Nueva();
            Ejecutar(fuente, "UPDATE unidad SET activa = 0 WHERE nombre = 'quintales'");
            var token = new ManejadorLogin().Login("monster", "monster9").Token!;
            Assert.Equal(new[] { "gramos", "kilogramos", "onzas", "libras" },
                new ManejadorCatalogo().ObtenerUnidades(token, "Masa").Datos);
            Assert.Equal("UNIT-001", new ManejadorConversion().Convertir("Masa", 1, "quintales", "libras", token).Codigo);
        }

        [SqlServerFact]
        public void SiLaBaseNoRespondeElServicioDevuelveSrv500()
        {
            BaseDatos.Configurar(new FuenteCaida(), TimeProvider.System);
            Assert.Equal("SRV-500", new ManejadorLogin().Login("monster", "monster9").Codigo);
            Assert.Equal("SRV-500", new ManejadorCatalogo().ObtenerCategorias("x").Codigo);
        }

        private sealed class FuenteCaida : IFuenteDatos
        {
            public System.Data.Common.DbConnection Abrir() => throw new ErrorDatos("sin conexion", new InvalidOperationException());
        }
    }
}
