using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Seguridad;

namespace ec.edu.monster.Pruebas
{
    /// <summary>Se ejecutan una por una porque cambian una variable de entorno compartida.</summary>
    [Collection("VariableDeEntorno")]
    public class GestorTokensPruebas : IDisposable
    {
        private const int IdMonster = 1;
        private readonly RelojManual reloj = new(new DateTimeOffset(2026, 10, 27, 12, 0, 0, TimeSpan.Zero));

        public GestorTokensPruebas()
        {
            if (BaseDatosPrueba.Disponible)
            {
                BaseDatosPrueba.Compartida(reloj);
            }
        }

        public void Dispose() => Environment.SetEnvironmentVariable(GestorTokens.VariableVigencia, null);

        [SqlServerFact]
        public void TokenRecienGeneradoEsValido() =>
            Assert.Null(GestorTokens.Validar(GestorTokens.GenerarToken(IdMonster)));

        [SqlServerFact]
        public void TokenInexistenteOAlterado()
        {
            var t = GestorTokens.GenerarToken(IdMonster);
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(t + "x"));
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(""));
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(null));
            Assert.Equal("AUTH-001", CodigoResultado.TokenInvalido.Texto());
        }

        [SqlServerFact]
        public void TokenExpiradoDaSesionExpiradaYQuedaCerradoEnLaBase()
        {
            var t = GestorTokens.GenerarToken(IdMonster);
            reloj.Avanzar(TimeSpan.FromMinutes(29));
            Assert.Null(GestorTokens.Validar(t)); // aun vale: la vigencia por defecto es de 30 minutos
            reloj.Avanzar(TimeSpan.FromMinutes(2));
            Assert.Equal(CodigoResultado.SesionExpirada, GestorTokens.Validar(t));
            Assert.Equal(CodigoResultado.TokenInvalido, GestorTokens.Validar(t)); // la sesion ya quedo inactiva
        }

        [SqlServerFact]
        public void VigenciaConfigurable()
        {
            Environment.SetEnvironmentVariable(GestorTokens.VariableVigencia, "1");
            var t = GestorTokens.GenerarToken(IdMonster);
            reloj.Avanzar(TimeSpan.FromSeconds(61));
            Assert.Equal(CodigoResultado.SesionExpirada, GestorTokens.Validar(t));
        }
    }

    [CollectionDefinition("VariableDeEntorno", DisableParallelization = true)]
    public class VariableDeEntornoColeccion : ICollectionFixture<LimpiezaBaseDatos> { }
}
