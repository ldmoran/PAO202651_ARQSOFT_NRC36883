using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.Controladores
{
    /// <summary>
    /// Base comun de los controladores REST: extrae el token de la cabecera
    /// "Authorization: Bearer &lt;token&gt;", que en las versiones SOAP viajaba como
    /// un parametro mas de cada operacion.
    /// </summary>
    public abstract class ApiControllerBase : ControllerBase
    {
        protected string? Token()
        {
            var cabecera = Request.Headers.Authorization.ToString();
            if (string.IsNullOrWhiteSpace(cabecera))
            {
                return null;
            }
            const string prefijo = "Bearer ";
            return cabecera.StartsWith(prefijo, StringComparison.OrdinalIgnoreCase)
                ? cabecera[prefijo.Length..].Trim()
                : cabecera.Trim();
        }
    }
}
