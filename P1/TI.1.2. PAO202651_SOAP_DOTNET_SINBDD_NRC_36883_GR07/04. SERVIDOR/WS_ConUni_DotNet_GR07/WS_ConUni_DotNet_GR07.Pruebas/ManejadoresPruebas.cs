using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Modelos.Utilidades;

namespace ec.edu.monster.Pruebas
{
    /// <summary>Pruebas de la logica (CU-01 a CU-11) sin pasar por SOAP.</summary>
    [Collection("VariableDeEntorno")]
    public class ManejadoresPruebas : IDisposable
    {
        private readonly ManejadorLogin login = new();
        private readonly ManejadorCatalogo catalogo = new();
        private readonly ManejadorConversion conversion = new();
        private readonly string token;

        public ManejadoresPruebas()
        {
            Environment.SetEnvironmentVariable("CONUNI_TOKEN_MINUTOS", null);
            var r = login.Login("monster", "monster9");
            Assert.True(r.EsExitosa());
            token = r.Token!;
        }

        public void Dispose() => Environment.SetEnvironmentVariable("CONUNI_TOKEN_MINUTOS", null);

        private RespuestaServicio Convertir(string categoria, double valor, string? origen, string? destino, string? tk) =>
            conversion.Convertir(categoria, valor, origen, destino, tk);

        private static void AssertError(RespuestaServicio r, string codigo)
        {
            Assert.Equal("ERROR", r.Estado);
            Assert.Equal(codigo, r.Codigo);
            Assert.Null(r.Resultado);
        }

        // ---- CU-01 ----
        [Fact]
        public void LoginCorrectoDevuelveToken()
        {
            var r = login.Login("monster", "monster9");
            Assert.Equal("OK", r.Estado);
            Assert.Equal("OK-200", r.Codigo);
            Assert.NotNull(r.Token);
        }

        [Fact]
        public void LoginIncorrectoNoDaToken()
        {
            var r = login.Login("monster", "mala");
            Assert.Equal("AUTH-003", r.Codigo);
            Assert.Null(r.Token);
        }

        [Fact]
        public void LoginConCamposVacios()
        {
            Assert.Equal("DATA-001", login.Login("", "monster9").Codigo);
            Assert.Equal("DATA-001", login.Login("monster", null).Codigo);
        }

        // ---- CU-02 / CU-03 ----
        [Fact]
        public void CategoriasYUnidades()
        {
            Assert.Equal(new[] { "Longitud", "Masa", "Temperatura" }, catalogo.ObtenerCategorias(token).Datos);
            Assert.Equal(new[] { "gramos", "kilogramos", "onzas", "libras", "quintales" },
                catalogo.ObtenerUnidades(token, "masa").Datos);
            Assert.Equal("DATA-001", catalogo.ObtenerUnidades(token, "volumen").Codigo);
        }

        [Fact]
        public void CatalogoExigeToken()
        {
            Assert.Equal("AUTH-001", catalogo.ObtenerCategorias("xyz").Codigo);
            Assert.Equal("AUTH-001", catalogo.ObtenerUnidades(null, "Masa").Codigo);
        }

        // ---- CU-04 conversiones ----
        [Fact]
        public void Longitud()
        {
            Assert.Equal(1609.344, Convertir("Longitud", 1, "millas", "metros", token).Resultado!.Value, 9);
            Assert.Equal(1.0, Convertir("Longitud", 100, "centimetros", "metros", token).Resultado!.Value, 9);
            var r = Convertir("Longitud", 3, "pies", "yardas", token);
            Assert.Equal(1.0, r.Resultado!.Value, 9);
            Assert.Equal("OK", r.Estado);
            Assert.Equal("pies", r.UnidadOrigen);
            Assert.Equal("yardas", r.UnidadDestino);
            // kilometros no esta en el catalogo del ERS
            AssertError(Convertir("Longitud", 5, "kilometros", "metros", token), "UNIT-001");
        }

        [Fact]
        public void Masa()
        {
            Assert.Equal(1000.0, Convertir("Masa", 1, "kilogramos", "gramos", token).Resultado!.Value, 9);
            Assert.Equal(100.0, Convertir("Masa", 1, "quintales", "libras", token).Resultado!.Value, 9);
            Assert.Equal(16.0, Convertir("Masa", 1, "libras", "onzas", token).Resultado!.Value, 6);
        }

        [Fact]
        public void Temperatura()
        {
            Assert.Equal(212.0, Convertir("Temperatura", 100, "celsius", "fahrenheit", token).Resultado!.Value, 9);
            Assert.Equal(373.15, Convertir("Temperatura", 100, "celsius", "kelvin", token).Resultado!.Value, 9);
            Assert.Equal(0.0, Convertir("Temperatura", 32, "fahrenheit", "celsius", token).Resultado!.Value, 9);
            Assert.Equal(33.0, Convertir("Temperatura", 100, "celsius", "newton", token).Resultado!.Value, 9);
            Assert.Equal(80.0, Convertir("Temperatura", 100, "celsius", "reaumur", token).Resultado!.Value, 9);
            Assert.Equal(100.0, Convertir("Temperatura", 80, "reaumur", "celsius", token).Resultado!.Value, 9);
        }

        [Fact]
        public void AceptaMayusculasEspaciosYTildes()
        {
            var r = Convertir("longitud", 1, " METROS ", "Centímetros", token);
            Assert.Equal(100.0, r.Resultado!.Value, 9);
            r = Convertir("Masa", 1, "KILOGRAMOS", "Gramos", token);
            Assert.Equal(1000.0, r.Resultado!.Value, 9);
        }

        [Fact]
        public void ElResultadoNoTraeRuidoDeComaFlotante()
        {
            // 0.1 + 0.2 en coma flotante da 0.30000000000000004: se redondea a 12 cifras
            Assert.Equal(0.3, ManejadorConversion.Redondear(0.1 + 0.2));
        }

        // ---- errores CU-08 a CU-11 ----
        [Fact]
        public void TokenInvalido()
        {
            AssertError(Convertir("Masa", 1, "gramos", "libras", "no-existe"), "AUTH-001");
            AssertError(Convertir("Masa", 1, "gramos", "libras", null), "AUTH-001");
        }

        [Fact]
        public void UnidadNoSoportada()
        {
            var r = Convertir("Masa", 1, "toneladas", "libras", token);
            AssertError(r, "UNIT-001");
            Assert.Equal("Unidad de origen no soportada", r.Mensaje);
            r = Convertir("Masa", 1, "gramos", "toneladas", token);
            Assert.Equal("Unidad de destino no soportada", r.Mensaje);
        }

        [Fact]
        public void DatosInconsistentes()
        {
            AssertError(Convertir("Masa", double.NaN, "gramos", "libras", token), "DATA-001");
            AssertError(Convertir("Masa", 1, null, "libras", token), "DATA-001");
            AssertError(Convertir("Masa", 1, "gramos", " ", token), "DATA-001");
            // longitud -> temperatura: cada unidad existe, pero en categorias distintas
            AssertError(Convertir("Longitud", 1, "metros", "celsius", token), "DATA-001");
            // unidades de otra categoria usadas en una operacion que no es la suya
            AssertError(Convertir("Longitud", 1, "celsius", "kelvin", token), "DATA-001");
            AssertError(Convertir("Volumen", 1, "litros", "litros", token), "DATA-001");
        }

        [Fact]
        public void PrimeroSeValidaElToken()
        {
            // con token malo y datos malos, gana el error de token (CU-05 antes que CU-06)
            Assert.Equal("AUTH-001", Convertir("Masa", double.NaN, null, null, "x").Codigo);
        }
    }
}
