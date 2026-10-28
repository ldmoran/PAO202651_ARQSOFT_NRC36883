using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;
using ec.edu.monster.Seguridad;

namespace ec.edu.monster.Controladores.ManejadoresWS
{
    /// <summary>Maneja el pedido de login (CU-01) que recibe el servicio WSLogin.</summary>
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
    }
}
