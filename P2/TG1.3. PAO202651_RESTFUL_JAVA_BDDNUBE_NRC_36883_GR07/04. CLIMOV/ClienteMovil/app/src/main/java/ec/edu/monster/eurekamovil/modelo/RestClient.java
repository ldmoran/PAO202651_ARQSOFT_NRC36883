package ec.edu.monster.eurekamovil.modelo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente de la API REST de EurekaBank para Android (HttpURLConnection + Gson). No contiene reglas de negocio:
 * todo ocurre en el servidor. Nota: en el emulador de Android el equipo anfitrion se ve como 10.0.2.2.
 */
public class RestClient {

    public static final String URL_POR_DEFECTO = "http://10.0.2.2:8080/REST_EurekaBank_Java_GR07/api/";
    private static final Gson JSON = new Gson();

    private final String baseUrl;

    public RestClient(String baseUrl) {
        String url = baseUrl == null || baseUrl.trim().isEmpty() ? URL_POR_DEFECTO : baseUrl.trim();
        this.baseUrl = url.endsWith("/") ? url : url + "/";
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
        Type tipo = new TypeToken<List<Cuenta>>() { }.getType();
        return lista(enviar("GET", "cuentas", null), tipo);
    }

    public List<Movimiento> movimientos(String cuenta) {
        String ruta = cuenta == null || cuenta.trim().isEmpty() ? "movimientos" : "movimientos?cuenta=" + codificar(cuenta.trim());
        Type tipo = new TypeToken<List<Movimiento>>() { }.getType();
        return lista(enviar("GET", ruta, null), tipo);
    }

    public List<String> tipos() {
        Type tipo = new TypeToken<List<String>>() { }.getType();
        return lista(enviar("GET", "movimientos/tipos", null), tipo);
    }

    public Resultado registrar(String origen, String destino, String tipo, double importe) {
        Map<String, Object> body = new HashMap<>();
        body.put("cuentaOrigen", origen);
        if (destino != null && !destino.trim().isEmpty()) {
            body.put("cuentaDestino", destino.trim());
        }
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
    private static class Respuesta {
        final int estado;
        final JsonElement json;

        Respuesta(int estado, JsonElement json) {
            this.estado = estado;
            this.json = json;
        }
    }

    private Respuesta enviar(String metodo, String ruta, Map<String, Object> body) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(baseUrl + ruta).toURL().openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod(metodo);
            connection.setRequestProperty("Accept", "application/json");
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(JSON.toJson(body).getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            InputStream response = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String contenido = response == null ? "" : leer(response);
            JsonElement json = null;
            try {
                json = JsonParser.parseString(contenido);
            } catch (RuntimeException e) {
                // cuerpo vacio o no JSON: se informa por el estado HTTP
            }
            return new Respuesta(status, json);
        } catch (IOException e) {
            throw new ErrorServicio("No se pudo conectar con el servidor (" + baseUrl + "). Verifique que esté en ejecución.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String leer(InputStream input) throws IOException {
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static <T> List<T> lista(Respuesta r, Type tipo) {
        if (r.estado != 200 || r.json == null) {
            throw errorDe(r);
        }
        try {
            return JSON.fromJson(r.json, tipo);
        } catch (RuntimeException e) {
            throw new ErrorServicio("La respuesta del servidor no tiene el formato esperado");
        }
    }

    private static ErrorServicio errorDe(Respuesta r) {
        String detalle = texto(r.json, "mensaje", null);
        return new ErrorServicio(detalle != null ? detalle : "El servidor respondió con el estado HTTP " + r.estado);
    }

    private static String texto(JsonElement json, String campo, String porDefecto) {
        if (json != null && json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            if (o.has(campo) && !o.get(campo).isJsonNull()) {
                return o.get(campo).getAsString();
            }
        }
        return porDefecto;
    }

    private static String codificar(String valor) {
        try {
            return URLEncoder.encode(valor, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 no está disponible", e);
        }
    }
}
