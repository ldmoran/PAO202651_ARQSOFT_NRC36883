using System.Globalization;
using ec.edu.monster.ClienteConsola.Modelos;

namespace ec.edu.monster.ClienteConsola.Controladores
{
    /// <summary>
    /// Controlador del catalogo y las conversiones: usa el token de la sesion, llama a la
    /// API REST y cierra la sesion local cuando el servidor rechaza el token.
    /// </summary>
    public class ControladorConversiones
    {
        private readonly RestClient cliente;
        private readonly Sesion sesion;

        public ControladorConversiones(RestClient cliente, Sesion sesion)
        {
            this.cliente = cliente;
            this.sesion = sesion;
        }

        public async Task<RespuestaServicio> Categorias() =>
            Revisar(await cliente.ObtenerCategorias(sesion.Token));

        public async Task<RespuestaServicio> Unidades(string categoria) =>
            Revisar(await cliente.ObtenerUnidades(sesion.Token, categoria));

        public async Task<RespuestaServicio> Convertir(string categoria, double valor, string origen, string destino) =>
            Revisar(await cliente.Convertir(categoria, valor, origen, destino, sesion.Token));

        /// <summary>Si el servidor dice que el token no sirve, se cierra la sesion local.</summary>
        private RespuestaServicio Revisar(RespuestaServicio r)
        {
            if (r.RequiereNuevoLogin)
            {
                sesion.Cerrar();
            }
            return r;
        }

        /// <summary>Numero sin notacion cientifica y sin ceros sobrantes.</summary>
        public static string Formatear(double valor)
        {
            var d = Math.Round((decimal)valor, 10, MidpointRounding.AwayFromZero);
            return d.ToString("0.##########", CultureInfo.InvariantCulture);
        }
    }
}
