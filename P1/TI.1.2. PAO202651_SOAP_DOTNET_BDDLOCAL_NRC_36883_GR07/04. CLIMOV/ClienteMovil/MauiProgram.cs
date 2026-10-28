using ec.edu.monster.Cliente.Servicios;
using Microsoft.Extensions.Logging;

namespace ClienteMovil
{
    public static class MauiProgram
    {
        /// <summary>
        /// En el emulador de Android, 10.0.2.2 es el equipo donde corre el servidor SOAP.
        /// En un telefono real se cambia en el campo "Servidor" de la pantalla de inicio de sesion.
        /// </summary>
        public const string UrlServidorEmulador = "http://10.0.2.2:5080/";

        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder.UseMauiApp<App>();

            builder.Services.AddMauiBlazorWebView();
            builder.Services.AgregarClienteMonster(UrlServidorEmulador);
#if DEBUG
            builder.Logging.AddDebug();
#endif
            return builder.Build();
        }
    }
}
