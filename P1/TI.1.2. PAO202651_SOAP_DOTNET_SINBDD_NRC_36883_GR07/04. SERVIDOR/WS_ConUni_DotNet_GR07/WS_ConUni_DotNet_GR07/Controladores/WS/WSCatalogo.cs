using CoreWCF;
using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Modelos.Utilidades;

namespace ec.edu.monster.Controladores.WS
{
    /// <summary>CU-02 y CU-03: consulta del catalogo de categorias y unidades.</summary>
    [ServiceContract(Name = "WSCatalogo", Namespace = Contrato.Namespace)]
    public interface IWSCatalogo
    {
        [OperationContract(Name = "obtenerCategorias")]
        RespuestaServicio ObtenerCategorias(string token);

        [OperationContract(Name = "obtenerUnidades")]
        RespuestaServicio ObtenerUnidades(string token, string categoria);
    }

    public class WSCatalogo : IWSCatalogo
    {
        private readonly ManejadorCatalogo manejador = new();

        public RespuestaServicio ObtenerCategorias(string token) => manejador.ObtenerCategorias(token);

        public RespuestaServicio ObtenerUnidades(string token, string categoria) =>
            manejador.ObtenerUnidades(token, categoria);
    }
}
