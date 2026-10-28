package ec.edu.monster.pruebas;

import ec.edu.monster.controladores.ws.WSCatalogo;
import ec.edu.monster.controladores.ws.WSLogin;
import ec.edu.monster.controladores.ws.WSLongitud;
import ec.edu.monster.controladores.ws.WSMasa;
import ec.edu.monster.controladores.ws.WSTemperatura;
import jakarta.xml.ws.Endpoint;

/**
 * Lanzador para pruebas manuales SIN servidor de aplicaciones: publica los mismos
 * servicios en las mismas URLs que usa Payara
 * (http://localhost:8080/WS_ConUni_Java_GR07/WSLogin, ...).
 *
 * Uso (desde NetBeans: ejecutar este archivo, o por consola con el classpath de pruebas):
 *   java ec.edu.monster.pruebas.ServidorLocal [puerto]
 */
public class ServidorLocal {

    public static void main(String[] args) throws Exception {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        String base = "http://localhost:" + puerto + "/WS_ConUni_Java_GR07/";
        Endpoint.publish(base + "WSLogin", new WSLogin());
        Endpoint.publish(base + "WSCatalogo", new WSCatalogo());
        Endpoint.publish(base + "WSLongitud", new WSLongitud());
        Endpoint.publish(base + "WSMasa", new WSMasa());
        Endpoint.publish(base + "WSTemperatura", new WSTemperatura());
        System.out.println("Servidor local listo en " + base + "  (WSDL: <servicio>?wsdl). Ctrl+C para detener.");
        Thread.currentThread().join();
    }
}
