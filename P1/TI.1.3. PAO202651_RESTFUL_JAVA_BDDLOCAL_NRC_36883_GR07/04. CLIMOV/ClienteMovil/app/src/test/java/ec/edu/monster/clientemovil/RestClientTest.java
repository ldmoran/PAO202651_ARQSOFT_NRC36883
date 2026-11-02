package ec.edu.monster.clientemovil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.clientemovil.controlador.ControladorConversiones;
import ec.edu.monster.clientemovil.modelo.RespuestaServicio;
import ec.edu.monster.clientemovil.modelo.RestClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RestClientTest {

    private MockWebServer server;

    @Before
    public void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void stopServer() throws Exception {
        server.shutdown();
    }

    private RestClient client() {
        return new RestClient(server.url("/app").toString());
    }

    @Test
    public void loginReadsJsonToken() throws Exception {
        server.enqueue(new MockResponse().setBody(
                "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"abc-123\",\"datos\":[]}"));

        RespuestaServicio response = client().login("monster", "monster9");

        assertTrue(response.esExitosa());
        assertEquals("abc-123", response.getToken());
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/app/auth/login", request.getPath());
        assertTrue(request.getBody().readUtf8().contains("\"usuario\":\"monster\""));
    }

    @Test
    public void catalogoSendsBearerTokenAndParsesUnits() throws Exception {
        server.enqueue(new MockResponse().setBody(
                "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"datos\":[\"Longitud\",\"Masa\"]}"));

        RespuestaServicio response = client().obtenerCategorias("tok");

        RecordedRequest request = server.takeRequest();
        assertEquals("/app/catalogo/categorias", request.getPath());
        assertEquals("Bearer tok", request.getHeader("Authorization"));
        assertEquals(2, response.getDatos().size());
        assertEquals("Masa", response.getDatos().get(1));
    }

    @Test
    public void conversionUsesRestPathAndParsesValue() throws Exception {
        server.enqueue(new MockResponse().setBody(
                "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"t1\",\"datos\":[]}"));
        server.enqueue(new MockResponse().setBody(
                "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"resultado\":1609.344,"
                        + "\"unidadOrigen\":\"millas\",\"unidadDestino\":\"metros\",\"datos\":[]}"));
        ControladorConversiones controller = new ControladorConversiones(client());
        RespuestaServicio login = controller.iniciarSesion("monster", "monster9");
        assertTrue(login.esExitosa());

        RespuestaServicio response = controller.convertir("Longitud", 1, "millas", "metros");

        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("/app/conversiones/Longitud", request.getPath());
        assertEquals("Bearer " + login.getToken(), request.getHeader("Authorization"));
        assertEquals(1609.344, response.getResultado(), 1e-9);
    }

    @Test
    public void expiredTokenClearsSession() {
        server.enqueue(new MockResponse().setBody(
                "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"t1\",\"datos\":[]}"));
        ControladorConversiones controller = new ControladorConversiones(client());
        controller.iniciarSesion("monster", "monster9");
        server.enqueue(new MockResponse().setResponseCode(401).setBody(
                "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-002\",\"mensaje\":\"Sesión expirada\",\"datos\":[]}"));

        RespuestaServicio response = controller.categorias();

        assertTrue(response.requiereNuevoLogin());
        assertFalse(controller.haySesion());
    }

    @Test
    public void stoppedServerIsReportedAsNetworkError() throws Exception {
        String baseUrl = server.url("/app").toString();
        server.shutdown();

        RespuestaServicio response = new RestClient(baseUrl).login("a", "b");

        assertEquals(RespuestaServicio.COD_RED, response.getCodigo());
    }

    @Test
    public void formatNumbers() {
        assertEquals("5.0292", ControladorConversiones.formatear(5.0292));
        assertEquals("212", ControladorConversiones.formatear(212.0));
    }
}
