using CoreWCF;
using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Modelos.Utilidades;

namespace ec.edu.monster.Controladores.WS
{
    /// <summary>CU-04: realizar conversion de la categoria Masa.</summary>
    [ServiceContract(Name = "WSMasa", Namespace = Contrato.Namespace)]
    public interface IWSMasa
    {
        [OperationContract(Name = "convertirMasa")]
        RespuestaServicio ConvertirMasa(double valor, string unidadOrigen, string unidadDestino, string token);
    }

    public class WSMasa : IWSMasa
    {
        private readonly ManejadorConversion manejador = new();

        public RespuestaServicio ConvertirMasa(double valor, string unidadOrigen, string unidadDestino, string token) =>
            manejador.Convertir("Masa", valor, unidadOrigen, unidadDestino, token);
    }
}
