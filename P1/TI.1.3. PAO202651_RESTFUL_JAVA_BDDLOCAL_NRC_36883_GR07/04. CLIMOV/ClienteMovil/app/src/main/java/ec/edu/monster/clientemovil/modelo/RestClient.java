package ec.edu.monster.clientemovil.modelo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RestClient {

    public static final String URL_POR_DEFECTO = "http://10.0.2.2:8080/REST_ConUni_Java_GR07/api/";
    private static final Gson JSON = new Gson();

    private final String baseUrl;

    public RestClient(String baseUrl) {
        String url = baseUrl == null || baseUrl.trim().isEmpty() ? URL_POR_DEFECTO : baseUrl.trim();
        this.baseUrl = url.endsWith("/") ? url : url + "/";
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
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(baseUrl + ruta).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod(metodo);
            connection.setRequestProperty("Accept", "application/json");
            if (token != null && !token.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(JSON.toJson(body).getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            InputStream response = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            return interpretar(response == null ? "" : leer(response), status);
        } catch (IOException e) {
            return RespuestaServicio.errorLocal(RespuestaServicio.COD_RED,
                    "No se pudo conectar con el servidor (" + baseUrl + "). Verifique que esté en ejecución.");
        } catch (RuntimeException e) {
            return RespuestaServicio.errorLocal("SRV-500", "Respuesta inesperada del servidor");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String leer(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static RespuestaServicio interpretar(String contenido, int estadoHttp) {
        try {
            JsonObject json = JsonParser.parseString(contenido).getAsJsonObject();
            RespuestaServicio respuesta = new RespuestaServicio();
            if (json.has("estado") && !json.get("estado").isJsonNull()) respuesta.setEstado(json.get("estado").getAsString());
            if (json.has("codigo") && !json.get("codigo").isJsonNull()) respuesta.setCodigo(json.get("codigo").getAsString());
            if (json.has("mensaje") && !json.get("mensaje").isJsonNull()) respuesta.setMensaje(json.get("mensaje").getAsString());
            if (json.has("resultado") && !json.get("resultado").isJsonNull()) respuesta.setResultado(json.get("resultado").getAsDouble());
            if (json.has("unidadOrigen") && !json.get("unidadOrigen").isJsonNull()) respuesta.setUnidadOrigen(json.get("unidadOrigen").getAsString());
            if (json.has("unidadDestino") && !json.get("unidadDestino").isJsonNull()) respuesta.setUnidadDestino(json.get("unidadDestino").getAsString());
            if (json.has("token") && !json.get("token").isJsonNull()) respuesta.setToken(json.get("token").getAsString());
            JsonArray datos = json.has("datos") && json.get("datos").isJsonArray() ? json.getAsJsonArray("datos") : new JsonArray();
            for (JsonElement dato : datos) respuesta.getDatos().add(dato.getAsString());
            return respuesta;
        } catch (RuntimeException e) {
            return RespuestaServicio.errorLocal("SRV-500",
                    "No se pudo leer la respuesta JSON del servidor (HTTP " + estadoHttp + ")");
        }
    }

    private static String segmento(String valor) {
        try {
            return URLEncoder.encode(valor == null ? "" : valor, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 no está disponible", e);
        }
    }
}
