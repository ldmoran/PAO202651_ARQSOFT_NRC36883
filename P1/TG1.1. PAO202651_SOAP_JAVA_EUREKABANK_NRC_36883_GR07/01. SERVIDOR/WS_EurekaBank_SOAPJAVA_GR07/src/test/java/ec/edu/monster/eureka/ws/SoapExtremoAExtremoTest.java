package ec.edu.monster.eureka.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.eureka.BaseDatosPrueba;
import ec.edu.monster.eureka.repositorios.EurekaRepositorio;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import jakarta.xml.ws.Endpoint;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Publica WSEurekaBank de verdad y le habla por HTTP/SOAP, como lo hacen los clientes. */
class SoapExtremoAExtremoTest {

    private static final String NS = "http://ws.eurekabank.monster.edu.ec/";
    private static Endpoint endpoint;
    private static String url;

    @BeforeAll
    static void publicar() throws Exception {
        int puerto;
        try (ServerSocket s = new ServerSocket(0)) {
            puerto = s.getLocalPort();
        }
        url = "http://localhost:" + puerto + "/WSEurekaBank";
        endpoint = Endpoint.publish(url, new WSEurekaBank(new ServicioEureka(new EurekaRepositorio(BaseDatosPrueba.crear("eureka_soap")))));
    }

    @AfterAll
    static void detener() {
        endpoint.stop();
    }

    private static String llamar(String operacion, String parametros) throws Exception {
        String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>"
                + "<ns:" + operacion + " xmlns:ns=\"" + NS + "\">" + parametros + "</ns:" + operacion + ">"
                + "</soapenv:Body></soapenv:Envelope>";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(sobre)).build();
        HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode(), resp.body());
        return resp.body();
    }

    @Test
    void iniciarSesion() throws Exception {
        assertTrue(llamar("iniciarSesion", "<usuario>lmoran</usuario><contrasena>Eureka2026!</contrasena>").contains(">true<"));
        assertTrue(llamar("iniciarSesion", "<usuario>lmoran</usuario><contrasena>mala</contrasena>").contains(">false<"));
    }

    @Test
    void listarCuentas() throws Exception {
        String r = llamar("traerCuentasConClientes", "");
        assertTrue(r.contains("<Codigo>00100001</Codigo>"), r);
        assertTrue(r.contains("<NombreCliente>Coronel Castillo, Eric</NombreCliente>"), r);
    }

    @Test
    void tiposPermitidos() throws Exception {
        String r = llamar("tipoMovimientosPermitidos", "");
        assertTrue(r.contains(">DEPOSITO<") && r.contains(">RETIRO<") && r.contains(">TRANSFERENCIA<"), r);
    }

    @Test
    void registrarYConsultar() throws Exception {
        String r = llamar("regMovimiento",
                "<cuentaOrigen>00200002</cuentaOrigen><cuentaDestino></cuentaDestino><tipo>DEPOSITO</tipo><importe>10.5</importe>");
        assertTrue(r.contains("<codigo>1</codigo>"), r);
        String m = llamar("traerMovimientos", "<cuenta>00200002</cuenta>");
        assertTrue(m.contains("<Numero>2</Numero>") && m.contains("<SaldoActual>3510.50</SaldoActual>"), m);
    }

    @Test
    void errorDeNegocio() throws Exception {
        String r = llamar("regMovimiento",
                "<cuentaOrigen>00100003</cuentaOrigen><cuentaDestino></cuentaDestino><tipo>RETIRO</tipo><importe>999999</importe>");
        assertTrue(r.contains("<codigo>-1</codigo>") && r.contains("Saldo insuficiente"), r);
    }
}
