using ec.edu.monster.Cliente.Modelos;
using Microsoft.Extensions.DependencyInjection;

namespace ec.edu.monster.Cliente.Servicios
{
    /// <summary>
    /// Sesion del usuario en una aplicacion de interfaz grafica (web, escritorio, movil): recuerda quien inicio sesion
    /// y llama al servicio REST. No contiene reglas del banco: todo se valida en el servidor.
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

        public string? Usuario { get; private set; }

        public bool HaySesion => Usuario != null;

        /// <summary>Cambia la direccion del servicio (por ejemplo, el servidor de otro equipo o 10.0.2.2 en el emulador).</summary>
        public void CambiarServidor(string url)
        {
            UrlServidor = url;
            cliente = new RestClient(url, http);
            Usuario = null;
        }

        public async Task<Res<bool>> IniciarSesion(string usuario, string clave)
        {
            var r = await cliente.IniciarSesion(usuario, clave);
            Usuario = r.Ok && r.Datos ? usuario.Trim() : null;
            return r;
        }

        public void CerrarSesion() => Usuario = null;

        public Task<Res<List<Cuenta>>> Cuentas() => cliente.Cuentas();

        public Task<Res<List<Movimiento>>> Movimientos(string? cuenta) => cliente.Movimientos(cuenta);

        public Task<Res<List<string>>> Tipos() => cliente.Tipos();

        public Task<Res<string>> Registrar(string tipo, string origen, string? destino, decimal importe) =>
            cliente.Registrar(tipo, origen, destino, importe);
    }

    public static class RegistroServicios
    {
        /// <summary>Registra la sesion del cliente; en las apps de Blazor se usa una por usuario (Scoped).</summary>
        public static IServiceCollection AgregarClienteEureka(this IServiceCollection servicios, string? urlServidor = null) =>
            servicios.AddScoped(_ => new SesionCliente(urlServidor));
    }
}
