using CoreWCF;
using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Modelos.Utilidades;

namespace ec.edu.monster.Controladores.WS
{
    /// <summary>CU-04: realizar conversion de la categoria Longitud.</summary>
    [ServiceContract(Name = "WSLongitud", Namespace = Contrato.Namespace)]
    public interface IWSLongitud
    {
        [OperationContract(Name = "convertirLongitud")]
        RespuestaServicio ConvertirLongitud(double valor, string unidadOrigen, string unidadDestino, string token);
    }

    public class WSLongitud : IWSLongitud
    {
        private readonly ManejadorConversion manejador = new();

        public RespuestaServicio ConvertirLongitud(double valor, string unidadOrigen, string unidadDestino, string token) =>
            manejador.Convertir("Longitud", valor, unidadOrigen, unidadDestino, token);
    }
}
