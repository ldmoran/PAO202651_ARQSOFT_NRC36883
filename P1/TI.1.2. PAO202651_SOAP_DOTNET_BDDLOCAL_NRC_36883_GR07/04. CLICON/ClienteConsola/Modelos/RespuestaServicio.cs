namespace ec.edu.monster.ClienteConsola.Modelos
{
    /// <summary>
    /// Respuesta estructurada del servicio SOAP (estado, codigo, mensaje, resultado,
    /// unidadOrigen, unidadDestino; mas token en el login y datos en el catalogo).
    /// </summary>
    public class RespuestaServicio
    {
        public const string CodTokenInvalido = "AUTH-001";
        public const string CodSesionExpirada = "AUTH-002";

        /// <summary>Codigo propio del cliente: no se pudo hablar con el servidor.</summary>
        public const string CodRed = "NET-001";

        public string? Estado { get; set; }
        public string? Codigo { get; set; }
        public string? Mensaje { get; set; }
        public double? Resultado { get; set; }
        public string? UnidadOrigen { get; set; }
        public string? UnidadDestino { get; set; }
        public string? Token { get; set; }
        public List<string> Datos { get; } = new();

        public bool EsExitosa => Estado == "OK";

        /// <summary>true si el servidor rechazo el token (hay que volver a iniciar sesion).</summary>
        public bool RequiereNuevoLogin => Codigo == CodTokenInvalido || Codigo == CodSesionExpirada;

        public static RespuestaServicio ErrorLocal(string codigo, string mensaje) =>
            new() { Estado = "ERROR", Codigo = codigo, Mensaje = mensaje };
    }
}
