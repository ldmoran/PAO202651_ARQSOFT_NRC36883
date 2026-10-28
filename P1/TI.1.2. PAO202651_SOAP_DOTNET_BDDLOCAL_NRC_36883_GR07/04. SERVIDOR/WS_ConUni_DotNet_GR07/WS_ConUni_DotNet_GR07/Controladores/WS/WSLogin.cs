using CoreWCF;
using ec.edu.monster.Controladores.ManejadoresWS;
using ec.edu.monster.Modelos.Utilidades;

namespace ec.edu.monster.Controladores.WS
{
    /// <summary>
    /// CU-01: Autenticarse. Devuelve un token temporal si las credenciales son correctas.
    /// El espacio de nombres se fija para que no cambie el contrato (WSDL) que usan los clientes.
    /// </summary>
    [ServiceContract(Name = "WSLogin", Namespace = Contrato.Namespace)]
    public interface IWSLogin
    {
        [OperationContract(Name = "login")]
        RespuestaServicio Login(string usuario, string clave);
    }

    public class WSLogin : IWSLogin
    {
        private readonly ManejadorLogin manejador = new();

        public RespuestaServicio Login(string usuario, string clave) => manejador.Login(usuario, clave);
    }

    public static class Contrato
    {
        public const string Namespace = "http://WebServices.monster.edu.ec/";
    }
}
