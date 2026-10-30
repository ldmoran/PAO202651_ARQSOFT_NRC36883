using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;

namespace ec.edu.monster.Seguridad
{
    /// <summary>CU-01: Autenticarse. Un unico usuario, con credenciales en memoria.</summary>
    public static class ServicioAutenticacion
    {
        private const string Usuario = "monster";
        private const string Clave = "monster9";

        public static RespuestaServicio Autenticar(string? usuario, string? clave)
        {
            if (string.IsNullOrWhiteSpace(usuario) || string.IsNullOrWhiteSpace(clave))
            {
                return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes, "Usuario y clave son obligatorios");
            }
            if (Usuario == usuario.Trim() && Clave == clave)
            {
                return RespuestaMapeador.ExitoLogin(GestorTokens.GenerarToken());
            }
            return RespuestaMapeador.Error(CodigoResultado.CredencialesIncorrectas, "Usuario o clave incorrectos");
        }
    }
}
