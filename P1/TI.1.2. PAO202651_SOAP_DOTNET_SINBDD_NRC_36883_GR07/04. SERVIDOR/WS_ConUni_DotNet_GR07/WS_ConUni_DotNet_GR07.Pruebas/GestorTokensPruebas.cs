using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Seguridad;

namespace ec.edu.monster.Pruebas
{
    /// <summary>Se ejecutan una por una porque cambian una variable de entorno compartida.</summary>
    [Collection("VariableDeEntorno")]
    public class GestorTokensPruebas : IDisposable
    {
        public void Dispose() => Environment.SetEnvironmentVariable(GestorTokens.VariableVigencia, null);

        [Fact]
        public void TokenRecienGeneradoEsValido() =>
            Assert.Null(GestorTokens.Validar(GestorTokens.GenerarToken()));

        [Fact]
        public void TokenInexistenteOAlterado()
        {
            var t = GestorTokens.GenerarToken();
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(t + "x"));
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(""));
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(null));
            Assert.Equal("AUTH-001", CodigoResultado.TokenInvalido.Texto());
        }

        [Fact]
        public async Task TokenExpiradoDaSesionExpiradaYSeElimina()
        {
            Environment.SetEnvironmentVariable(GestorTokens.VariableVigencia, "0.0005"); // unos 30 ms
            var t = GestorTokens.GenerarToken();
            await Task.Delay(200);
            Assert.Equal(CodigoResultado.SesionExpirada, GestorTokens.Validar(t));
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(t)); // ya no existe
        }
    }

    [CollectionDefinition("VariableDeEntorno", DisableParallelization = true)]
    public class VariableDeEntornoColeccion { }
}
