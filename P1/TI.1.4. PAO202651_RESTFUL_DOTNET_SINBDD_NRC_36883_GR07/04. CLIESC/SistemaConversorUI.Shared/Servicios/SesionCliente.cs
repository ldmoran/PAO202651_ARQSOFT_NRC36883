using ec.edu.monster.Cliente.Modelos;
using Microsoft.Extensions.DependencyInjection;

namespace ec.edu.monster.Cliente.Servicios
{
    /// <summary>
    /// Sesion del usuario en una aplicacion de interfaz grafica (web, escritorio, movil): guarda el
    /// token que entrego el servidor, llama a la API REST con el y cierra la sesion local cuando
    /// el servidor lo rechaza. No contiene logica de conversion.
    /// </summary>
    public class SesionCliente
    {
        private readonly HttpClient? http;
        private RestClient cliente;

        public SesionCliente(string? urlServidor = null, HttpClient? http = null)
        {
            this.http = http;
            UrlServidor = string.IsNullOrWhiteSpace(urlServidor) ? RestClient.UrlConfigurada() : urlServidor;
            cliente = new RestClient(UrlServidor, http);
        }

        public string UrlServidor { get; private set; }

        public string? Token { get; private set; }

        public bool HaySesion => Token != null;

        /// <summary>Cambia la direccion del servidor (por ejemplo 10.0.2.2 en el emulador de Android).</summary>
        public void CambiarServidor(string url)
        {
            UrlServidor = url;
            cliente = new RestClient(url, http);
            Token = null;
        }

        public async Task<RespuestaServicio> IniciarSesion(string usuario, string clave)
        {
            var r = await cliente.Login(usuario, clave);
            Token = r.EsExitosa ? r.Token : null;
            return r;
        }

        public void CerrarSesion() => Token = null;

        public async Task<RespuestaServicio> Categorias() => Revisar(await cliente.ObtenerCategorias(Token));

        public async Task<RespuestaServicio> Unidades(string categoria) =>
            Revisar(await cliente.ObtenerUnidades(Token, categoria));

        public async Task<RespuestaServicio> Convertir(string categoria, double valor, string origen, string destino) =>
            Revisar(await cliente.Convertir(categoria, valor, origen, destino, Token));

        private RespuestaServicio Revisar(RespuestaServicio r)
        {
            if (r.RequiereNuevoLogin)
            {
                Token = null;
            }
            return r;
        }
    }

    public static class RegistroServicios
    {
        /// <summary>Registra la sesion del cliente; en las apps de Blazor se usa una por usuario (Scoped).</summary>
        public static IServiceCollection AgregarClienteMonster(this IServiceCollection servicios, string? urlServidor = null) =>
            servicios.AddScoped(_ => new SesionCliente(urlServidor));
    }
}
