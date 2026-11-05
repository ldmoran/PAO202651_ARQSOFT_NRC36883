using System.Runtime.Serialization;

namespace ec.edu.monster.eureka.Modelos
{
    /// <summary>Espacio de nombres del contrato SOAP (igual al del servidor Java, para que los clientes sirvan con ambos).</summary>
    public static class Contrato
    {
        public const string Namespace = "http://ws.eurekabank.monster.edu.ec/";
    }

    [DataContract(Name = "DatosCuenta", Namespace = Contrato.Namespace)]
    public class DatosCuenta
    {
        [DataMember(Name = "Codigo", Order = 1)] public string Codigo { get; set; } = "";
        [DataMember(Name = "NombreCliente", Order = 2)] public string NombreCliente { get; set; } = "";
        [DataMember(Name = "Saldo", Order = 3)] public decimal Saldo { get; set; }
        [DataMember(Name = "Estado", Order = 4)] public string Estado { get; set; } = "";
        [DataMember(Name = "Moneda", Order = 5)] public string Moneda { get; set; } = "";
        [DataMember(Name = "EmailCliente", Order = 6)] public string? EmailCliente { get; set; }
        [DataMember(Name = "TelefonoCliente", Order = 7)] public string? TelefonoCliente { get; set; }
    }

    [DataContract(Name = "MovimientoData", Namespace = Contrato.Namespace)]
    public class MovimientoData
    {
        [DataMember(Name = "CodigoCuenta", Order = 1)] public string CodigoCuenta { get; set; } = "";
        [DataMember(Name = "Numero", Order = 2)] public int Numero { get; set; }
        /// <summary>Fecha AAAA-MM-DD (texto, igual que en el servidor Java).</summary>
        [DataMember(Name = "Fecha", Order = 3)] public string Fecha { get; set; } = "";
        [DataMember(Name = "Tipo", Order = 4)] public string Tipo { get; set; } = "";
        [DataMember(Name = "Importe", Order = 5)] public decimal Importe { get; set; }
        [DataMember(Name = "Referencia", Order = 6)] public string? Referencia { get; set; }
        [DataMember(Name = "SaldoActual", Order = 7)] public decimal SaldoActual { get; set; }
        [DataMember(Name = "NombreCliente", Order = 8)] public string NombreCliente { get; set; } = "";
    }

    [DataContract(Name = "resultado", Namespace = Contrato.Namespace)]
    public class ResultadoOperacion
    {
        [DataMember(Name = "codigo", Order = 1)] public int Codigo { get; set; }
        [DataMember(Name = "mensaje", Order = 2)] public string Mensaje { get; set; } = "";

        public static ResultadoOperacion Exito() => new() { Codigo = 1, Mensaje = "Operación exitosa" };
        public static ResultadoOperacion Error(string mensaje) => new() { Codigo = -1, Mensaje = mensaje };
    }

    /// <summary>Error de regla de negocio: su mensaje se muestra tal cual al usuario.</summary>
    public class ReglaNegocio : Exception
    {
        public ReglaNegocio(string mensaje) : base(mensaje) { }
    }
}
