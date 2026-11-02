package ec.edu.monster.clienteconsola.modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RestClient {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/REST_ConUni_Java_GR07/api/";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public RestClient(String baseUrl) {
        String url = baseUrl == null || baseUrl.isBlank() ? URL_POR_DEFECTO : baseUrl.trim();
        this.baseUrl = url.endsWith("/") ? url : url + "/";
    }

    public static String urlConfigurada() {
        String url = System.getProperty("conuni.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("CONUNI_URL");
        }
        return url == null || url.isBlank() ? URL_POR_DEFECTO : url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public RespuestaServicio login(String usuario, String clave) {
        Map<String, Object> body = new HashMap<>();
        body.put("usuario", usuario);
        body.put("clave", clave);
        return enviar("POST", "auth/login", null, body);
    }

    public RespuestaServicio obtenerCategorias(String token) {
        return enviar("GET", "catalogo/categorias", token, null);
    }

    public RespuestaServicio obtenerUnidades(String token, String categoria) {
        return enviar("GET", "catalogo/" + segmento(categoria) + "/unidades", token, null);
    }

    public RespuestaServicio convertir(String categoria, double valor, String origen, String destino, String token) {
        Map<String, Object> body = new HashMap<>();
        body.put("valor", valor);
        body.put("unidadOrigen", origen);
        body.put("unidadDestino", destino);
        return enviar("POST", "conversiones/" + segmento(categoria), token, body);
    }

    private RespuestaServicio enviar(String metodo, String ruta, String token, Map<String, Object> body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body == null) {
            request.GET();
        } else {
            try {
                request.header("Content-Type", "application/json; charset=utf-8")
                        .method(metodo, HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
            } catch (JsonProcessingException e) {
                return RespuestaServicio.errorLocal("SRV-500", "No se pudo preparar la solicitud JSON");
            }
        }

        HttpResponse<String> response;
        try {
            response = http.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return RespuestaServicio.errorLocal(RespuestaServicio.COD_RED,
                    "No se pudo conectar con el servidor (" + baseUrl + "). Verifique que esté en ejecución.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return RespuestaServicio.errorLocal(RespuestaServicio.COD_RED, "Operación interrumpida");
        }
        return interpretar(response.body(), response.statusCode());
    }

    private static RespuestaServicio interpretar(String contenido, int estadoHttp) {
        try {
            JsonNode json = JSON.readTree(contenido);
            if (json == null || !json.isObject()) {
                return RespuestaServicio.errorLocal("SRV-500", "Respuesta JSON inválida (HTTP " + estadoHttp + ")");
            }
            RespuestaServicio respuesta = new RespuestaServicio();
            if (json.hasNonNull("estado")) respuesta.setEstado(json.get("estado").asText());
            if (json.hasNonNull("codigo")) respuesta.setCodigo(json.get("codigo").asText());
            if (json.hasNonNull("mensaje")) respuesta.setMensaje(json.get("mensaje").asText());
            if (json.hasNonNull("resultado")) respuesta.setResultado(json.get("resultado").asDouble());
            if (json.hasNonNull("unidadOrigen")) respuesta.setUnidadOrigen(json.get("unidadOrigen").asText());
            if (json.hasNonNull("unidadDestino")) respuesta.setUnidadDestino(json.get("unidadDestino").asText());
            if (json.hasNonNull("token")) respuesta.setToken(json.get("token").asText());
            JsonNode datos = json.get("datos");
            if (datos != null && datos.isArray()) {
                for (JsonNode dato : datos) respuesta.getDatos().add(dato.asText());
            }
            return respuesta;
        } catch (JsonProcessingException e) {
            return RespuestaServicio.errorLocal("SRV-500", "No se pudo leer la respuesta JSON del servidor");
        }
    }

    private static String segmento(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
