package ec.edu.monster.clienteconsola.modelo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;
import java.util.Map;

/**
 * Cliente de la API REST de EurekaBank. Solo arma las peticiones HTTP/JSON y lee las respuestas:
 * no contiene reglas de negocio, todo ocurre en el servidor.
 */
public class RestClient {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/REST_EurekaBank_Java_GR07/api/";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public RestClient(String baseUrl) {
        String url = baseUrl == null || baseUrl.isBlank() ? URL_POR_DEFECTO : baseUrl.trim();
        this.baseUrl = url.endsWith("/") ? url : url + "/";
    }

    /** URL del servidor: propiedad -Deureka.url, variable EUREKA_URL o la direccion por defecto. */
    public static String urlConfigurada() {
        String url = System.getProperty("eureka.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("EUREKA_URL");
        }
        return url == null || url.isBlank() ? URL_POR_DEFECTO : url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Resultado login(String usuario, String contrasena) {
        Map<String, Object> body = new HashMap<>();
        body.put("usuario", usuario);
        body.put("contrasena", contrasena);
        Respuesta r = enviar("POST", "sesion/login", body);
        if (r.estado == 200) {
            return new Resultado(true, texto(r.json, "mensaje", "Inicio de sesión exitoso"));
        }
        if (r.estado == 401 || r.estado == 400) {
            return new Resultado(false, texto(r.json, "mensaje", "Usuario o contraseña incorrectos"));
        }
        throw errorDe(r);
    }

    public List<Cuenta> cuentas() {
        return lista(enviar("GET", "cuentas", null), new TypeReference<List<Cuenta>>() { });
    }

    public List<Movimiento> movimientos(String cuenta) {
        String ruta = cuenta == null || cuenta.isBlank() ? "movimientos"
                : "movimientos?cuenta=" + URLEncoder.encode(cuenta.trim(), StandardCharsets.UTF_8);
        return lista(enviar("GET", ruta, null), new TypeReference<List<Movimiento>>() { });
    }

    public List<String> tipos() {
        return lista(enviar("GET", "movimientos/tipos", null), new TypeReference<List<String>>() { });
    }

    public Resultado registrar(String origen, String destino, String tipo, double importe) {
        Map<String, Object> body = new HashMap<>();
        body.put("cuentaOrigen", origen);
        body.put("cuentaDestino", destino == null || destino.isBlank() ? null : destino);
        body.put("tipo", tipo);
        body.put("importe", importe);
        Respuesta r = enviar("POST", "movimientos", body);
        if (r.estado == 200) {
            return new Resultado(true, texto(r.json, "mensaje", "Operación exitosa"));
        }
        if (r.estado == 400 || r.estado == 422) {
            return new Resultado(false, texto(r.json, "mensaje", "No se pudo registrar el movimiento"));
        }
        throw errorDe(r);
    }

    // ------------------------------------------------------------------ HTTP
    private record Respuesta(int estado, JsonNode json) {
    }

    private Respuesta enviar(String metodo, String ruta, Map<String, Object> body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (body == null) {
            request.GET();
        } else {
            try {
                request.header("Content-Type", "application/json; charset=utf-8")
                        .method(metodo, HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
            } catch (JsonProcessingException e) {
                throw new ErrorServicio("No se pudo preparar la solicitud JSON");
            }
        }
        try {
            HttpResponse<String> resp = http.send(request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode json = null;
            try {
                json = JSON.readTree(resp.body());
            } catch (IOException e) {
                // cuerpo vacio o no JSON: se informa por el estado HTTP
            }
            return new Respuesta(resp.statusCode(), json);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErrorServicio("Se canceló la consulta al servidor");
        } catch (IOException | IllegalArgumentException e) {
            throw new ErrorServicio("No se pudo conectar con el servidor");
        }
    }

    private static <T> List<T> lista(Respuesta r, TypeReference<List<T>> tipo) {
        if (r.estado != 200 || r.json == null) {
            throw errorDe(r);
        }
        try {
            return JSON.readValue(JSON.treeAsTokens(r.json), tipo);
        } catch (IOException e) {
            throw new ErrorServicio("La respuesta del servidor no tiene el formato esperado");
        }
    }

    private static ErrorServicio errorDe(Respuesta r) {
        String detalle = texto(r.json, "mensaje", null);
        return new ErrorServicio(detalle != null ? detalle : "El servidor respondió con el estado HTTP " + r.estado);
    }

    private static String texto(JsonNode json, String campo, String porDefecto) {
        return json != null && json.hasNonNull(campo) ? json.get(campo).asText() : porDefecto;
    }
}
