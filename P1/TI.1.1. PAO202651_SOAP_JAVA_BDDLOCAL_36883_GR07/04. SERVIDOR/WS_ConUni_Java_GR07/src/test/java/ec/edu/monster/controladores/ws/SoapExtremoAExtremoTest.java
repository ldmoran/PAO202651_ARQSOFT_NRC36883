package ec.edu.monster.controladores.ws;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.datos.Datos;
import ec.edu.monster.pruebas.BaseDatosPrueba;
import jakarta.xml.ws.Endpoint;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Publica los servicios y los invoca con mensajes SOAP reales por HTTP. */
class SoapExtremoAExtremoTest {

    /** Espacio de nombres del contrato: los cuatro clientes lo tienen escrito, no debe cambiar. */
    private static final String NS = "http://WebServices.monster.edu.ec/";
    private static final String[] SERVICIOS = {"WSLogin", "WSCatalogo", "WSLongitud", "WSMasa", "WSTemperatura"};
    private static int puerto;
    private static final List<Endpoint> ENDPOINTS = new ArrayList<>();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @BeforeAll
    static void publicar() throws Exception {
        Datos.configurar(BaseDatosPrueba.crear("soap"), BaseDatosPrueba.RELOJ);
        try (ServerSocket s = new ServerSocket(0)) {
            puerto = s.getLocalPort();
        }
        ENDPOINTS.add(Endpoint.publish(url("WSLogin"), new WSLogin()));
        ENDPOINTS.add(Endpoint.publish(url("WSCatalogo"), new WSCatalogo()));
        ENDPOINTS.add(Endpoint.publish(url("WSLongitud"), new WSLongitud()));
        ENDPOINTS.add(Endpoint.publish(url("WSMasa"), new WSMasa()));
        ENDPOINTS.add(Endpoint.publish(url("WSTemperatura"), new WSTemperatura()));
    }

    @AfterAll
    static void detener() {
        ENDPOINTS.forEach(Endpoint::stop);
        Datos.restablecer();
    }

    private static String url(String servicio) {
        return "http://localhost:" + puerto + "/" + servicio;
    }

    private static String soap(String servicio, String operacion, String cuerpo) throws Exception {
        String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"" + NS + "\">"
                + "<soapenv:Header/><soapenv:Body><ws:" + operacion + ">" + cuerpo + "</ws:" + operacion + ">"
                + "</soapenv:Body></soapenv:Envelope>";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url(servicio)))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(envelope)).build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static String campo(String xml, String nombre) {
        Matcher m = Pattern.compile("<" + nombre + ">([^<]*)</" + nombre + ">").matcher(xml);
        return m.find() ? m.group(1) : null;
    }

    private static String loginToken() throws Exception {
        String r = soap("WSLogin", "login", "<usuario>monster</usuario><clave>monster9</clave>");
        assertEquals("OK-200", campo(r, "codigo"), r);
        return campo(r, "token");
    }

    private static String conv(String srv, String op, String valor, String o, String d, String token) throws Exception {
        return soap(srv, op, "<valor>" + valor + "</valor><unidadOrigen>" + o + "</unidadOrigen><unidadDestino>" + d
                + "</unidadDestino><token>" + token + "</token>");
    }

    @Test
    void wsdlDisponibleYConElMismoEspacioDeNombres() throws Exception {
        for (String s : SERVICIOS) {
            HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(url(s) + "?wsdl")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, r.statusCode(), s);
            assertTrue(r.body().contains("definitions"), s);
            assertTrue(r.body().contains("targetNamespace=\"" + NS + "\""), "espacio de nombres de " + s);
        }
    }

    @Test
    void loginPorSoap() throws Exception {
        String ok = soap("WSLogin", "login", "<usuario>monster</usuario><clave>monster9</clave>");
        assertEquals("OK", campo(ok, "estado"));
        assertNotNull(campo(ok, "token"));
        String mal = soap("WSLogin", "login", "<usuario>monster</usuario><clave>x</clave>");
        assertEquals("ERROR", campo(mal, "estado"));
        assertEquals("AUTH-003", campo(mal, "codigo"));
    }

    @Test
    void conversionesPorSoap() throws Exception {
        String t = loginToken();
        String l = conv("WSLongitud", "convertirLongitud", "1", "millas", "metros", t);
        assertEquals("OK-200", campo(l, "codigo"), l);
        assertEquals(1609.344, Double.parseDouble(campo(l, "resultado")), 1e-9);
        String m = conv("WSMasa", "convertirMasa", "1", "kilogramos", "libras", t);
        assertEquals(2.20462262185, Double.parseDouble(campo(m, "resultado")), 1e-6);
        String c = conv("WSTemperatura", "convertirTemperatura", "100", "celsius", "fahrenheit", t);
        assertEquals(212.0, Double.parseDouble(campo(c, "resultado")), 1e-9);
    }

    @Test
    void erroresPorSoap() throws Exception {
        String t = loginToken();
        assertEquals("AUTH-001", campo(conv("WSMasa", "convertirMasa", "1", "gramos", "libras", "falso"), "codigo"));
        assertEquals("UNIT-001", campo(conv("WSMasa", "convertirMasa", "1", "toneladas", "libras", t), "codigo"));
        assertEquals("DATA-001", campo(conv("WSLongitud", "convertirLongitud", "1", "metros", "celsius", t), "codigo"));
    }

    @Test
    void catalogoPorSoap() throws Exception {
        String t = loginToken();
        String cat = soap("WSCatalogo", "obtenerCategorias", "<token>" + t + "</token>");
        assertTrue(cat.contains("<datos>Longitud</datos>") && cat.contains("<datos>Masa</datos>")
                && cat.contains("<datos>Temperatura</datos>"), cat);
        String un = soap("WSCatalogo", "obtenerUnidades", "<token>" + t + "</token><categoria>Temperatura</categoria>");
        assertTrue(un.contains("<datos>fahrenheit</datos>"), un);
    }
}
