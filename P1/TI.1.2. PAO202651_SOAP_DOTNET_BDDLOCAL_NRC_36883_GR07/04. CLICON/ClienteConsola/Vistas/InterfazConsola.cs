using ec.edu.monster.ClienteConsola.Controladores;

namespace ec.edu.monster.ClienteConsola.Vistas
{
    /// <summary>Menu principal de opciones numericas: reparte el trabajo a las vistas de sesion y de conversion.</summary>
    public class InterfazConsola
    {
        private readonly ControladorSesion sesion;
        private readonly VistaSesion vistaSesion;
        private readonly VistaConversion vistaConversion;
        private readonly ConsolaIO io;

        public InterfazConsola(ControladorSesion sesion, VistaSesion vistaSesion, VistaConversion vistaConversion, ConsolaIO io)
        {
            this.sesion = sesion;
            this.vistaSesion = vistaSesion;
            this.vistaConversion = vistaConversion;
            this.io = io;
        }

        public async Task Ejecutar()
        {
            io.Linea();
            io.Marco("CONVERSIÓN DE UNIDADES MONSTER", "Cliente de consola (.NET)", "Servidor: " + sesion.Url);
            var seguir = true;
            while (seguir)
            {
                MostrarMenu();
                var op = io.Leer("Opción: ");
                if (op == null)
                {
                    break; // fin de la entrada
                }
                switch (op.Trim())
                {
                    case "1":
                        await vistaSesion.IniciarSesion();
                        break;
                    case "2":
                        if (ExigirSesion())
                        {
                            await vistaConversion.VerCatalogo();
                        }
                        break;
                    case "3":
                        if (ExigirSesion())
                        {
                            await vistaConversion.Convertir();
                        }
                        break;
                    case "4":
                        vistaSesion.CerrarSesion();
                        break;
                    case "0":
                        seguir = false;
                        break;
                    default:
                        io.Linea("  Opción no válida.");
                        break;
                }
            }
            io.Linea("Hasta luego.");
        }

        private void MostrarMenu()
        {
            io.Seccion("Menú  |  Sesión: " + (sesion.HaySesion ? "ACTIVA" : "no iniciada"));
            if (!sesion.HaySesion)
            {
                io.Linea("  1. Iniciar sesión");
            }
            else
            {
                io.Linea("  2. Ver catálogo (categorías y unidades)");
                io.Linea("  3. Convertir");
                io.Linea("  4. Cerrar sesión");
            }
            io.Linea("  0. Salir");
        }

        private bool ExigirSesion()
        {
            if (!sesion.HaySesion)
            {
                io.Linea("  Debe iniciar sesión primero.");
                return false;
            }
            return true;
        }
    }
}
