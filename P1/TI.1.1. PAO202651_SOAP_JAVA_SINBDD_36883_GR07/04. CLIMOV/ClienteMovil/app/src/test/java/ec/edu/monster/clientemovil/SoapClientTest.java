package ec.edu.monster.clientemovil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.clientemovil.controlador.ControladorConversiones;
import ec.edu.monster.clientemovil.modelo.RespuestaServicio;
import ec.edu.monster.clientemovil.modelo.SoapClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Prueba el cliente SOAP contra un servidor falso (no necesita el servidor real ni un emulador). */
public class SoapClientTest {

    private HttpServer servidor;
    private volatile String respuestaXml;
    private volatile String ultimaPeticion;

    private static String envelope(String retorno) {
        return "<?xml version=\"1.0\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<ns2:resp xmlns:ns2=\"http://WebServices.monster.edu.ec/\"><return>" + retorno + "</return></ns2:resp>"
                + "</S:Body></S:Envelope>";
    }

    @Before
    public void iniciar() throws Exception {
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

    @After
    public void detener() {
        servidor.stop(0);
    }

    private SoapClient cliente() {
        return new SoapClient("http://localhost:" + servidor.getAddress().getPort() + "/app");
    }

    @Test
    public void leeUnaConversionExitosa() {
        respuestaXml = envelope("<codigo>OK-200</codigo><estado>OK</estado><mensaje>Conversión realizada correctamente</mensaje>"
                + "<resultado>1609.344</resultado><unidadDestino>metros</unidadDestino><unidadOrigen>millas</unidadOrigen>");
        RespuestaServicio r = cliente().convertir("Longitud", 1, "millas", "metros", "tok");
        assertTrue(r.esExitosa());
        assertEquals(1609.344, r.getResultado(), 1e-9);
        assertEquals("millas", r.getUnidadOrigen());
        assertTrue(ultimaPeticion.contains("<ws:convertirLongitud>"));
        assertTrue(ultimaPeticion.contains("<token>tok</token>"));
    }

    @Test
    public void leeElTokenDelLoginYEscapaXml() {
        respuestaXml = envelope("<codigo>OK-200</codigo><estado>OK</estado><mensaje>ok</mensaje><token>abc-123</token>");
        RespuestaServicio r = cliente().login("a&b", "<x>");
        assertEquals("abc-123", r.getToken());
        assertTrue(ultimaPeticion.contains("a&amp;b") && ultimaPeticion.contains("&lt;x&gt;"));
    }

    @Test
    public void leeListasDelCatalogo() {
        respuestaXml = envelope("<codigo>OK-200</codigo><datos>Longitud</datos><datos>Masa</datos><estado>OK</estado>");
        RespuestaServicio r = cliente().obtenerCategorias("tok");
        assertEquals(2, r.getDatos().size());
        assertEquals("Masa", r.getDatos().get(1));
    }

    @Test
    public void sesionExpiradaCierraLaSesionLocal() {
        respuestaXml = envelope("<codigo>OK-200</codigo><estado>OK</estado><mensaje>ok</mensaje><token>t1</token>");
        ControladorConversiones c = new ControladorConversiones(cliente());
        assertTrue(c.iniciarSesion("monster", "monster9").esExitosa());
        assertTrue(c.haySesion());
        respuestaXml = envelope("<codigo>AUTH-002</codigo><estado>ERROR</estado><mensaje>Sesión expirada</mensaje>");
        RespuestaServicio r = c.convertir("Masa", 1, "gramos", "libras");
        assertTrue(r.requiereNuevoLogin());
        assertFalse(c.haySesion());
    }

    @Test
    public void servidorApagadoDaErrorDeRed() {
        servidor.stop(0);
        RespuestaServicio r = cliente().login("a", "b");
        assertEquals(RespuestaServicio.COD_RED, r.getCodigo());
    }

    @Test
    public void formatearNumeros() {
        assertEquals("5.0292", ControladorConversiones.formatear(5.0292));
        assertEquals("212", ControladorConversiones.formatear(212.0));
    }
}
