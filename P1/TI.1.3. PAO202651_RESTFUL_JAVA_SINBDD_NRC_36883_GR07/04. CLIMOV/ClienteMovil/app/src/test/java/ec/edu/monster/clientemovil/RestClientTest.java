package ec.edu.monster.clientemovil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.clientemovil.controlador.ControladorConversiones;
import ec.edu.monster.clientemovil.modelo.RespuestaServicio;
import ec.edu.monster.clientemovil.modelo.RestClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RestClientTest {

    private HttpServer server;
    private volatile String responseJson;
    private volatile String requestPath;
    private volatile String requestMethod;
    private volatile String requestBody;
    private volatile String authorization;
    private volatile int responseStatus;

    @Before
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/app/", exchange -> {
            requestPath = exchange.getRequestURI().getPath();
            requestMethod = exchange.getRequestMethod();
            authorization = exchange.getRequestHeaders().getFirst("Authorization");
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(responseStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        responseStatus = 200;
    }

    @After
    public void stopServer() {
        server.stop(0);
    }

    private RestClient client() {
        return new RestClient("http://localhost:" + server.getAddress().getPort() + "/app");
    }

    @Test
    public void loginReadsJsonToken() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"abc-123\",\"datos\":[]}";

        RespuestaServicio response = client().login("monster", "monster9");

        assertTrue(response.esExitosa());
        assertEquals("abc-123", response.getToken());
        assertEquals("POST", requestMethod);
        assertEquals("/app/auth/login", requestPath);
        assertTrue(requestBody.contains("\"usuario\":\"monster\""));
    }

    @Test
    public void catalogoSendsBearerTokenAndParsesUnits() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"datos\":[\"Longitud\",\"Masa\"]}";

        RespuestaServicio response = client().obtenerCategorias("tok");

        assertEquals("/app/catalogo/categorias", requestPath);
        assertEquals("Bearer tok", authorization);
        assertEquals(2, response.getDatos().size());
        assertEquals("Masa", response.getDatos().get(1));
    }

    @Test
    public void conversionUsesRestPathAndParsesValue() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"t1\",\"datos\":[]}";
        ControladorConversiones controller = new ControladorConversiones(client());
        RespuestaServicio login = controller.iniciarSesion("monster", "monster9");
        assertTrue(login.esExitosa());
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"resultado\":1609.344,"
                + "\"unidadOrigen\":\"millas\",\"unidadDestino\":\"metros\",\"datos\":[]}";

        RespuestaServicio response = controller.convertir("Longitud", 1, "millas", "metros");

        assertEquals("/app/conversiones/Longitud", requestPath);
        assertEquals("Bearer " + login.getToken(), authorization);
        assertEquals(1609.344, response.getResultado(), 1e-9);
    }

    @Test
    public void expiredTokenClearsSession() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"t1\",\"datos\":[]}";
        ControladorConversiones controller = new ControladorConversiones(client());
        controller.iniciarSesion("monster", "monster9");
        responseStatus = 401;
        responseJson = "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-002\",\"mensaje\":\"Sesión expirada\",\"datos\":[]}";

        RespuestaServicio response = controller.categorias();

        assertTrue(response.requiereNuevoLogin());
        assertFalse(controller.haySesion());
    }

    @Test
    public void stoppedServerIsReportedAsNetworkError() {
        server.stop(0);

        RespuestaServicio response = client().login("a", "b");

        assertEquals(RespuestaServicio.COD_RED, response.getCodigo());
    }

    @Test
    public void formatNumbers() {
        assertEquals("5.0292", ControladorConversiones.formatear(5.0292));
        assertEquals("212", ControladorConversiones.formatear(212.0));
    }
}
