using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;
using ec.edu.monster.Seguridad;

namespace ec.edu.monster.Controladores.Manejadores
{
    /// <summary>Maneja los pedidos de login y logout (CU-01, CU-01b) que recibe AuthController.</summary>
    public class ManejadorLogin
    {
        public RespuestaServicio Login(string? usuario, string? clave)
        {
            try
            {
                return ServicioAutenticacion.Autenticar(usuario, clave);
            }
            catch (Exception)
            {
                return RespuestaMapeador.Error(CodigoResultado.ErrorServidor, "Error interno del servicio");
            }
        }

        /// <summary>CU-01b: cierra la sesion asociada al token, si existe.</summary>
        public RespuestaServicio Logout(string? token)
        {
            try
            {
                GestorTokens.Invalidar(token);
                return RespuestaMapeador.Exito("Sesión cerrada correctamente");
            }
            catch (Exception)
            {
                return RespuestaMapeador.Error(CodigoResultado.ErrorServidor, "Error interno del servicio");
            }
        }
    }
}
