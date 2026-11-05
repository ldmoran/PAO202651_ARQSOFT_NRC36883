namespace ec.edu.monster.eureka.Modelos
{
    public class DatosCuenta
    {
        public string Codigo { get; set; } = "";
        public string NombreCliente { get; set; } = "";
        public decimal Saldo { get; set; }
        public string Estado { get; set; } = "";
        public string Moneda { get; set; } = "";
        public string? EmailCliente { get; set; }
        public string? TelefonoCliente { get; set; }
    }

    public class MovimientoData
    {
        public string CodigoCuenta { get; set; } = "";
        public int Numero { get; set; }
        /// <summary>Fecha AAAA-MM-DD (texto).</summary>
        public string Fecha { get; set; } = "";
        public string Tipo { get; set; } = "";
        public decimal Importe { get; set; }
        public string? Referencia { get; set; }
        public decimal SaldoActual { get; set; }
        public string NombreCliente { get; set; } = "";
    }

    public class ResultadoOperacion
    {
        public int Codigo { get; set; }
        public string Mensaje { get; set; } = "";

        public static ResultadoOperacion Exito() => new() { Codigo = 1, Mensaje = "Operación exitosa" };
        public static ResultadoOperacion Error(string mensaje) => new() { Codigo = -1, Mensaje = mensaje };
    }

    /// <summary>Error de regla de negocio: su mensaje se muestra tal cual al usuario.</summary>
    public class ReglaNegocio : Exception
    {
        public ReglaNegocio(string mensaje) : base(mensaje) { }
    }

    public class LoginRequest
    {
        public string? Usuario { get; set; }
        public string? Contrasena { get; set; }
    }

    public class MovimientoRequest
    {
        public string? CuentaOrigen { get; set; }
        public string? CuentaDestino { get; set; }
        public string? Tipo { get; set; }
        public double Importe { get; set; }
    }
}
