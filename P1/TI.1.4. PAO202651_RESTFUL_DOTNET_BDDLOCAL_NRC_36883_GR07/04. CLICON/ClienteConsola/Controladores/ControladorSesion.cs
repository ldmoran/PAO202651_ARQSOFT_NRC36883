using ec.edu.monster.ClienteConsola.Modelos;

namespace ec.edu.monster.ClienteConsola.Controladores
{
    /// <summary>Controlador de la sesion (CU-01): iniciar y cerrar sesion contra /api/auth.</summary>
    public class ControladorSesion
    {
        private readonly RestClient cliente;
        private readonly Sesion sesion;

        public ControladorSesion(RestClient cliente, Sesion sesion)
        {
            this.cliente = cliente;
            this.sesion = sesion;
        }

        public string Url => cliente.UrlBase;

        public bool HaySesion => sesion.Activa;

        public async Task<RespuestaServicio> IniciarSesion(string usuario, string clave)
        {
            var r = await cliente.Login(usuario, clave);
            if (r.EsExitosa && r.Token != null)
            {
                sesion.Iniciar(r.Token);
            }
            else
            {
                sesion.Cerrar();
            }
            return r;
        }

        public void CerrarSesion() => sesion.Cerrar();
    }
}
