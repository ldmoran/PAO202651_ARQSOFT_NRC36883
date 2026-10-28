using ec.edu.monster.ClienteConsola.Controladores;

namespace ec.edu.monster.ClienteConsola.Vistas
{
    /// <summary>Pantalla de inicio y cierre de sesion (CU-01).</summary>
    public class VistaSesion
    {
        private readonly ControladorSesion controlador;
        private readonly ConsolaIO io;

        public VistaSesion(ControladorSesion controlador, ConsolaIO io)
        {
            this.controlador = controlador;
            this.io = io;
        }

        public async Task IniciarSesion()
        {
            io.Seccion("Iniciar sesión");
            if (controlador.HaySesion)
            {
                io.Linea("  Ya hay una sesión activa.");
                return;
            }
            var usuario = io.Leer("Usuario: ");
            var clave = io.Leer("Clave: ");
            if (usuario == null || clave == null)
            {
                return;
            }
            var r = await controlador.IniciarSesion(usuario, clave);
            if (r.EsExitosa)
            {
                io.Ok(r.Mensaje);
            }
            else
            {
                io.Error(r);
            }
        }

        public void CerrarSesion()
        {
            controlador.CerrarSesion();
            io.Ok("Sesión cerrada.");
        }
    }
}
