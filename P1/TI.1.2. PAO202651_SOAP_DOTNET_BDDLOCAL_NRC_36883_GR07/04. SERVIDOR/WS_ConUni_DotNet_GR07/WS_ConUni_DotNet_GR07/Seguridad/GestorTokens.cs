using System.Globalization;
using ec.edu.monster.Datos;
using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;

namespace ec.edu.monster.Seguridad
{
    /// <summary>
    /// Tokens de sesion guardados en la tabla sesion. Cubre CU-05 (validar token),
    /// CU-08 (token invalido) y CU-09 (sesion expirada).
    ///
    /// La vigencia por defecto es de 30 minutos y se puede cambiar con la variable
    /// de entorno "CONUNI_TOKEN_MINUTOS" (acepta decimales, por ejemplo 0.01).
    /// </summary>
    public static class GestorTokens
    {
        public const string VariableVigencia = "CONUNI_TOKEN_MINUTOS";

        private static TimeSpan Vigencia()
        {
            var valor = Environment.GetEnvironmentVariable(VariableVigencia);
            if (double.TryParse(valor, NumberStyles.Float, CultureInfo.InvariantCulture, out var minutos) && minutos > 0)
            {
                return TimeSpan.FromMinutes(minutos);
            }
            return TimeSpan.FromMinutes(30);
        }

        /// <summary>Genera un token unico para el usuario y lo guarda con su fecha de expiracion.</summary>
        public static string GenerarToken(int idUsuario)
        {
            var token = Guid.NewGuid().ToString();
            var ahora = BaseDatos.Ahora();
            new SesionRepositorio(BaseDatos.Fuente).Crear(token, idUsuario, ahora, ahora + Vigencia());
            return token;
        }

        /// <summary>
        /// Valida un token. Devuelve null si es valido; TokenInvalido (AUTH-001) si es
        /// nulo, inexistente, alterado o ya cerrado; SesionExpirada (AUTH-002) si existia
        /// pero ya expiro (y la cierra en la base).
        /// </summary>
        public static CodigoResultado? Validar(string? token)
        {
            if (string.IsNullOrWhiteSpace(token))
            {
                return CodigoResultado.TokenInvalido;
            }
            var limpio = token.Trim();
            var sesiones = new SesionRepositorio(BaseDatos.Fuente);
            var sesion = sesiones.Buscar(limpio);
            if (sesion is null || !sesion.Activa)
            {
                return CodigoResultado.TokenInvalido;
            }
            if (BaseDatos.Ahora() > sesion.Expiracion)
            {
                sesiones.Desactivar(limpio);
                return CodigoResultado.SesionExpirada;
            }
            return null;
        }

        /// <summary>Respuesta de error para un token que no sirve, o null si el token es valido.</summary>
        public static RespuestaServicio? Verificar(string? token)
        {
            var codigo = Validar(token);
            if (codigo is null)
            {
                return null;
            }
            return codigo == CodigoResultado.SesionExpirada
                ? RespuestaMapeador.Error(codigo.Value, "Sesión expirada")
                : RespuestaMapeador.Error(codigo.Value, "Token inválido");
        }
    }
}
