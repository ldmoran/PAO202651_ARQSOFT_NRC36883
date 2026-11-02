package ec.edu.monster.clienteweb;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import ec.edu.monster.clienteweb.modelo.RestClient;
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
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"token\":\"abc-123\",\"datos\":[]}";

        RespuestaServicio response = client().login("monster", "monster9");

        assertTrue(response.esExitosa());
        assertEquals("abc-123", response.getToken());
        assertEquals("POST", requestMethod);
        assertEquals("/app/auth/login", requestPath);
        assertTrue(requestBody.contains("\"usuario\":\"monster\""));
    }

    @Test
    void catalogoSendsAuthorizationHeader() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"datos\":[\"Longitud\",\"Masa\"]}";

        RespuestaServicio response = client().obtenerCategorias("tok");

        assertEquals("/app/catalogo/categorias", requestPath);
        assertEquals("Bearer tok", authorization);
        assertEquals(2, response.getDatos().size());
    }

    @Test
    void conversionUsesRestEndpointAndParsesResult() {
        responseJson = "{\"estado\":\"OK\",\"codigo\":\"OK-200\",\"resultado\":1609.344,"
                + "\"unidadOrigen\":\"millas\",\"unidadDestino\":\"metros\",\"datos\":[]}";

        RespuestaServicio response = client().convertir("Longitud", 1, "millas", "metros", "tok");

        assertEquals("/app/conversiones/Longitud", requestPath);
        assertEquals("Bearer tok", authorization);
        assertTrue(requestBody.contains("\"unidadDestino\":\"metros\""));
        assertEquals(1609.344, response.getResultado(), 1e-9);
    }

    @Test
    void http401ResponseKeepsAuthenticationError() {
        responseStatus = 401;
        responseJson = "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-002\",\"mensaje\":\"Sesión expirada\",\"datos\":[]}";

        RespuestaServicio response = client().obtenerCategorias("expired");

        assertTrue(response.requiereNuevoLogin());
        assertEquals("AUTH-002", response.getCodigo());
    }

    @Test
    void invalidJsonIsReported() {
        responseJson = "not-json";

        RespuestaServicio response = client().login("a", "b");

        assertEquals("SRV-500", response.getCodigo());
    }
}
