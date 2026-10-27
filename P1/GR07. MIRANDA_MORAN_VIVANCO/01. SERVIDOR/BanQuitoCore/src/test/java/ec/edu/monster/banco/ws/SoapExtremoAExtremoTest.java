package ec.edu.monster.banco.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.banco.BaseDatosPrueba;
import ec.edu.monster.banco.repositorios.CoreRepositorio;
import ec.edu.monster.banco.servicios.ServicioCredito;
import jakarta.xml.ws.Endpoint;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Publica WSCredito de verdad y le habla por HTTP/SOAP, como lo haria la Comercializadora. */
class SoapExtremoAExtremoTest {

    private static final String NS = "http://WebServices.banquito.edu.ec/";
    private static Endpoint endpoint;
    private static String url;

    @BeforeAll
    static void publicar() throws Exception {
        int puerto;
        try (ServerSocket s = new ServerSocket(0)) {
            puerto = s.getLocalPort();
        }
        url = "http://localhost:" + puerto + "/WSCredito";
        ServicioCredito servicio = new ServicioCredito(new CoreRepositorio(BaseDatosPrueba.crear("soap_e2e")),
                BaseDatosPrueba.RELOJ);
        endpoint = Endpoint.publish(url, new WSCredito(servicio));
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
    void publicaElWsdl() throws Exception {
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url + "?wsdl")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("verificarSujetoCredito"));
        assertTrue(resp.body().contains("otorgarCredito"));
    }

    @Test
    void verificarSujetoCreditoPorSoap() throws Exception {
        String r = llamar("verificarSujetoCredito", "<cedula>0101010101</cedula>");
        assertTrue(r.contains("<sujetoCredito>true</sujetoCredito>"), r);
    }

    @Test
    void calcularMontoMaximoPorSoap() throws Exception {
        String r = llamar("calcularMontoMaximo", "<cedula>0101010101</cedula>");
        assertTrue(r.contains("<montoMaximo>1080.0</montoMaximo>"), r);
    }

    @Test
    void otorgarYConsultarTablaPorSoap() throws Exception {
        String r = llamar("otorgarCredito", "<cedula>0505050505</cedula><monto>200</monto><plazoMeses>4</plazoMeses>");
        assertTrue(r.contains("<aprobado>true</aprobado>"), r);
        String t = llamar("obtenerTablaAmortizacion", "<codCredito>2</codCredito>");
        assertTrue(t.contains("<numCuota>4</numCuota>"), t);
    }
}
