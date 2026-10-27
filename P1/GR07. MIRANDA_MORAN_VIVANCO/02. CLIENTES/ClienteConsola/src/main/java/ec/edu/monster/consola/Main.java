package ec.edu.monster.consola;

import ec.edu.monster.cliente.ComercializadoraSoap;
import ec.edu.monster.consola.controlador.ControladorConsola;
import ec.edu.monster.consola.vista.VistaConsola;
import java.util.Scanner;

/**
 * Cliente de consola. La direccion del servicio se puede dar como primer argumento o con la variable
 * COMERCIALIZADORA_URL (por ejemplo cuando el servidor corre en otra PC de la red).
 */
public class Main {

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getenv("COMERCIALIZADORA_URL");
        if (url == null || url.isBlank()) {
            url = ComercializadoraSoap.URL_POR_DEFECTO;
        }
        System.out.println("Servicio: " + url);
        new ControladorConsola(new ComercializadoraSoap(url), new VistaConsola(new Scanner(System.in, "UTF-8"), System.out))
                .ejecutar();
    }
}
