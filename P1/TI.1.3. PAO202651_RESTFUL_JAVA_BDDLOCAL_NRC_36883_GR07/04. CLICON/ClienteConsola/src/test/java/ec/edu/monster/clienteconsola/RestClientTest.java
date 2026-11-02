package ec.edu.monster.clienteconsola;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.clienteconsola.controlador.ControladorConversiones;
import ec.edu.monster.clienteconsola.controlador.ControladorSesion;
import ec.edu.monster.clienteconsola.modelo.RespuestaServicio;
import ec.edu.monster.clienteconsola.modelo.RestClient;
import ec.edu.monster.clienteconsola.modelo.Sesion;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestClientTest {

    private HttpServer server;
    private volatile String responseJson;
    private volatile String requestPath;
    private volatile String requestMethod;
    private volatile String requestBody;
    private volatile String authorization;
    private volatile int responseStatus;

    @BeforeEach
    void startServer() throws Exception {
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

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private RestClient client() {
        return new RestClient("http://localhost:" + server.getAddress().getPort() + "/app");
    }

    @Test
    void loginPostsJsonAndReadsToken() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"mensaje\":\"ok\",\"token\":\"abc-123\",\"datos\":[]}";

        RespuestaServicio response = client().login("monster", "monster9");

        assertTrue(response.esExitosa());
        assertEquals("abc-123", response.getToken());
        assertEquals("POST", requestMethod);
        assertEquals("/app/auth/login", requestPath);
        assertTrue(requestBody.contains("\"usuario\":\"monster\""));
        assertTrue(requestBody.contains("\"clave\":\"monster9\""));
    }

    @Test
    void catalogoSendsBearerTokenAndReadsCategories() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"datos\":[\"Longitud\",\"Masa\",\"Temperatura\"]}";

        RespuestaServicio response = client().obtenerCategorias("tok");

        assertEquals("GET", requestMethod);
        assertEquals("/app/catalogo/categorias", requestPath);
        assertEquals("Bearer tok", authorization);
        assertEquals(java.util.List.of("Longitud", "Masa", "Temperatura"), response.getDatos());
    }

    @Test
    void conversionPostsCategoryAndUnits() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"resultado\":1609.344,"
                + "\"unidadOrigen\":\"millas\",\"unidadDestino\":\"metros\",\"datos\":[]}";

        RespuestaServicio response = client().convertir("Longitud", 1, "millas", "metros", "tok");

        assertTrue(response.esExitosa());
        assertEquals(1609.344, response.getResultado(), 1e-9);
        assertEquals("POST", requestMethod);
        assertEquals("/app/conversiones/Longitud", requestPath);
        assertEquals("Bearer tok", authorization);
        assertTrue(requestBody.contains("\"unidadOrigen\":\"millas\""));
        assertTrue(requestBody.contains("\"unidadDestino\":\"metros\""));
    }

    @Test
    void error401MakesControllerDiscardExpiredSession() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"t1\",\"datos\":[]}";
        RestClient restClient = client();
        Sesion session = new Sesion();
        ControladorSesion login = new ControladorSesion(restClient, session);
        ControladorConversiones conversions = new ControladorConversiones(restClient, session);
        assertTrue(login.iniciarSesion("monster", "monster9").esExitosa());

        responseStatus = 401;
        responseJson = "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-002\",\"mensaje\":\"Sesión expirada\",\"datos\":[]}";
        RespuestaServicio response = conversions.convertir("Masa", 1, "gramos", "libras");

        assertTrue(response.requiereNuevoLogin());
        assertFalse(login.haySesion());
    }

    @Test
    void serverErrorsRemainStructured() {
        responseStatus = 401;
        responseJson = "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-003\",\"mensaje\":\"Credenciales incorrectas\",\"datos\":[]}";

        RespuestaServicio response = client().login("bad", "bad");

        assertEquals("AUTH-003", response.getCodigo());
        assertFalse(response.esExitosa());
    }

    @Test
    void invalidJsonIsReported() {
        responseJson = "<html>error</html>";

        RespuestaServicio response = client().obtenerCategorias("tok");

        assertEquals("SRV-500", response.getCodigo());
    }

    @Test
    void stoppedServerIsReportedAsNetworkError() {
        server.stop(0);

        RespuestaServicio response = client().login("a", "b");

        assertEquals(RespuestaServicio.COD_RED, response.getCodigo());
    }

    @Test
    void formatNumbers() {
        assertEquals("5.0292", ControladorConversiones.formatear(5.0292));
        assertEquals("212", ControladorConversiones.formatear(212.0));
        assertEquals("1000000000000", ControladorConversiones.formatear(1e12));
        assertEquals("0.0000000001", ControladorConversiones.formatear(1e-10));
    }
}
