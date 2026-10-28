package ec.edu.monster.clienteweb;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import ec.edu.monster.clienteweb.modelo.SoapClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente contra un servidor falso (sin necesidad del servidor real). */
class SoapClientTest {

    private HttpServer servidor;
    private String respuestaXml;
    private String ultimaPeticion;

    private static String envelope(String retorno) {
        return "<?xml version=\"1.0\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<ns2:resp xmlns:ns2=\"http://WebServices.monster.edu.ec/\"><return>" + retorno + "</return></ns2:resp>"
                + "</S:Body></S:Envelope>";
    }

    @BeforeEach
    void iniciar() throws Exception {
        servidor = HttpServer.create(new InetSocketAddress(0), 0);
        servidor.createContext("/app/", ex -> {
            ultimaPeticion = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] b = respuestaXml.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        servidor.start();
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    private SoapClient cliente() {
        return new SoapClient("http://localhost:" + servidor.getAddress().getPort() + "/app");
    }

    @Test
    void leeUnaConversionExitosa() {
        respuestaXml = envelope("<codigo>OK-200</codigo><estado>OK</estado><mensaje>Conversión realizada correctamente</mensaje>"
                + "<resultado>1609.344</resultado><unidadDestino>metros</unidadDestino><unidadOrigen>millas</unidadOrigen>");
        RespuestaServicio r = cliente().convertir("Longitud", 1, "millas", "metros", "tok");
        assertTrue(r.esExitosa());
        assertEquals(1609.344, r.getResultado(), 1e-9);
        assertEquals("millas", r.getUnidadOrigen());
        assertEquals("Conversión realizada correctamente", r.getMensaje());
        assertTrue(ultimaPeticion.contains("<ws:convertirLongitud>"));
        assertTrue(ultimaPeticion.contains("<token>tok</token>"));
        assertTrue(ultimaPeticion.contains("<valor>1.0</valor>"));
    }

    @Test
    void leeElTokenDelLoginYEscapaXml() {
        respuestaXml = envelope("<codigo>OK-200</codigo><estado>OK</estado><mensaje>Autenticación exitosa</mensaje><token>abc-123</token>");
        RespuestaServicio r = cliente().login("a&b", "<x>");
        assertEquals("abc-123", r.getToken());
        assertTrue(ultimaPeticion.contains("a&amp;b") && ultimaPeticion.contains("&lt;x&gt;"));
    }

    @Test
    void leeListasDelCatalogo() {
        respuestaXml = envelope("<codigo>OK-200</codigo><datos>Longitud</datos><datos>Masa</datos><datos>Temperatura</datos><estado>OK</estado>");
        RespuestaServicio r = cliente().obtenerCategorias("tok");
        assertEquals(java.util.List.of("Longitud", "Masa", "Temperatura"), r.getDatos());
    }

    @Test
    void fallaSoapSeInformaSinRomper() {
        respuestaXml = "<?xml version=\"1.0\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><S:Fault>"
                + "<faultcode>S:Server</faultcode><faultstring>boom</faultstring></S:Fault></S:Body></S:Envelope>";
        RespuestaServicio r = cliente().obtenerCategorias("t");
        assertFalse(r.esExitosa());
        assertTrue(r.getMensaje().contains("boom"));
    }

    @Test
    void servidorApagadoDaErrorDeRed() {
        servidor.stop(0);
        RespuestaServicio r = cliente().login("a", "b");
        assertEquals(RespuestaServicio.COD_RED, r.getCodigo());
    }

    @Test
    void categoriaDesconocidaNoLlamaAlServidor() {
        RespuestaServicio r = cliente().convertir("Volumen", 1, "a", "b", "t");
        assertEquals("DATA-001", r.getCodigo());
    }

}
