package ec.edu.monster.clienteconsola;

import ec.edu.monster.clienteconsola.controlador.ControladorConversiones;
import ec.edu.monster.clienteconsola.controlador.ControladorSesion;
import ec.edu.monster.clienteconsola.modelo.Sesion;
import ec.edu.monster.clienteconsola.modelo.SoapClient;
import ec.edu.monster.clienteconsola.vista.ConsolaIO;
import ec.edu.monster.clienteconsola.vista.InterfazConsola;
import ec.edu.monster.clienteconsola.vista.VistaConversion;
import ec.edu.monster.clienteconsola.vista.VistaSesion;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Cliente de consola de "Conversion de Unidades Monster". Aqui se arman las piezas del MVC:
 * modelo (SoapClient, Sesion), controladores y vistas.
 *
 * URL del servidor: argumento 1, propiedad -Dconuni.url, variable CONUNI_URL o
 * http://localhost:8080/WS_ConUni_Java_GR07/
 */
public class ClienteConsola {

    public static void main(String[] args) {
        PrintStream salida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8);
        String url = args.length > 0 ? args[0] : SoapClient.urlConfigurada();

        SoapClient cliente = new SoapClient(url);
        Sesion sesion = new Sesion();
        ControladorSesion controladorSesion = new ControladorSesion(cliente, sesion);
        ControladorConversiones controladorConversiones = new ControladorConversiones(cliente, sesion);

        ConsolaIO io = new ConsolaIO(entrada, salida);
        new InterfazConsola(controladorSesion, new VistaSesion(controladorSesion, io),
                new VistaConversion(controladorConversiones, io), io).ejecutar();
    }
}
