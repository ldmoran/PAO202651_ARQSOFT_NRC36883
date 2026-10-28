using System.Text;
using ec.edu.monster.ClienteConsola.Controladores;
using ec.edu.monster.ClienteConsola.Modelos;
using ec.edu.monster.ClienteConsola.Vistas;

// Cliente de consola de "Conversion de Unidades Monster". Aqui se arman las piezas del MVC:
// modelo (SoapClient, Sesion), controladores y vistas.
//
// URL del servidor: argumento 1, variable CONUNI_URL o http://localhost:5080/
Console.OutputEncoding = Encoding.UTF8;
Console.InputEncoding = Encoding.UTF8;

var url = args.Length > 0 ? args[0] : SoapClient.UrlConfigurada();
var cliente = new SoapClient(url);
var sesion = new Sesion();
var controladorSesion = new ControladorSesion(cliente, sesion);
var controladorConversiones = new ControladorConversiones(cliente, sesion);
var io = new ConsolaIO(Console.In, Console.Out);

await new InterfazConsola(controladorSesion, new VistaSesion(controladorSesion, io),
    new VistaConversion(controladorConversiones, io), io).Ejecutar();
