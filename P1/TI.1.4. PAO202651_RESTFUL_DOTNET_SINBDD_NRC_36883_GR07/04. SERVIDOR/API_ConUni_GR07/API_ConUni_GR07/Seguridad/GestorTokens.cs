using System.Collections.Concurrent;
using System.Globalization;
using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;

namespace ec.edu.monster.Seguridad
{
    /// <summary>
    /// Tokens activos en memoria (sin base de datos). Cubre CU-05 (validar token),
    /// CU-08 (token invalido), CU-09 (sesion expirada) y CU-01b (cerrar sesion).
    ///
    /// La vigencia por defecto es de 30 minutos y se puede cambiar con la variable
    /// de entorno "CONUNI_TOKEN_MINUTOS" (acepta decimales, por ejemplo 0.01).
    /// </summary>
    public static class GestorTokens
    {
        public const string VariableVigencia = "CONUNI_TOKEN_MINUTOS";

        private static readonly ConcurrentDictionary<string, long> Activos = new();

        private static long VigenciaMs()
        {
            var valor = Environment.GetEnvironmentVariable(VariableVigencia);
            if (double.TryParse(valor, NumberStyles.Float, CultureInfo.InvariantCulture, out var minutos) && minutos > 0)
            {
                return (long)(minutos * 60_000d);
            }
            return 30L * 60_000L;
        }

        private static long Ahora() => DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

        /// <summary>Genera un token unico y lo guarda con su fecha de expiracion.</summary>
        public static string GenerarToken()
        {
            var token = Guid.NewGuid().ToString();
            Activos[token] = Ahora() + VigenciaMs();
            return token;
        }

        /// <summary>
        /// Valida un token. Devuelve null si es valido; TokenInvalido (AUTH-001) si es
        /// nulo, inexistente o alterado; SesionExpirada (AUTH-002) si existio pero ya
        /// expiro (y lo elimina).
        /// </summary>
        public static CodigoResultado? Validar(string? token)
        {
            if (string.IsNullOrWhiteSpace(token))
            {
                return CodigoResultado.TokenInvalido;
            }
            var clave = token.Trim();
            if (!Activos.TryGetValue(clave, out var expira))
            {
                return CodigoResultado.TokenInvalido;
            }
            if (Ahora() > expira)
            {
                Activos.TryRemove(clave, out _);
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

        /// <summary>
        /// CU-01b: cierra la sesion eliminando el token del almacen. Operacion idempotente:
        /// no falla si el token ya no existe o nunca existio.
        /// </summary>
        public static void Invalidar(string? token)
        {
            if (!string.IsNullOrWhiteSpace(token))
            {
                Activos.TryRemove(token.Trim(), out _);
            }
        }
    }
}
