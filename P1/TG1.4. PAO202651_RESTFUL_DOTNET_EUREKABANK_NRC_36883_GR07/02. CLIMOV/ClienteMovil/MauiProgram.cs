using ec.edu.monster.Cliente.Servicios;
using Microsoft.Extensions.Logging;

namespace ClienteMovil
{
    public static class MauiProgram
    {
        /// <summary>
        /// En el emulador de Android, 10.0.2.2 es el equipo donde corre el servidor REST.
        /// En un telefono real se cambia en el campo "Servidor" de la pantalla de inicio de sesion.
        /// </summary>
        public const string UrlServidorEmulador = "http://10.0.2.2:5081";

        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.UseMauiApp<App>();

            builder.Services.AddMauiBlazorWebView();
            builder.Services.AgregarClienteEureka(UrlServidorEmulador);
#if DEBUG
            builder.Logging.AddDebug();
#endif
            return builder.Build();
        }
    }
}
