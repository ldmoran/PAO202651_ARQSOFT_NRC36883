using CoreWCF;
using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Modelos.Utilidades;

namespace ec.edu.monster.Controladores.WS
{
    /// <summary>CU-04: realizar conversion de la categoria Temperatura.</summary>
    [ServiceContract(Name = "WSTemperatura", Namespace = Contrato.Namespace)]
    public interface IWSTemperatura
    {
        [OperationContract(Name = "convertirTemperatura")]
        RespuestaServicio ConvertirTemperatura(double valor, string unidadOrigen, string unidadDestino, string token);
    }

    public class WSTemperatura : IWSTemperatura
    {
        private readonly ManejadorConversion manejador = new();

        public RespuestaServicio ConvertirTemperatura(double valor, string unidadOrigen, string unidadDestino, string token) =>
            manejador.Convertir("Temperatura", valor, unidadOrigen, unidadDestino, token);
    }
}
