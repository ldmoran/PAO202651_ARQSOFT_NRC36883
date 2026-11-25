package ec.edu.monster.paises.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Consumo de la API publica de paises (JSON por HTTP GET).
 *
 * La direccion base se puede cambiar con la propiedad paises.api.url o la variable PAISES_API_URL.
 * Por defecto usa https://www.apicountries.com: una API publica sin clave con el mismo formato que
 * REST Countries v3.1, que fue dada de baja (la version nueva exige una clave de API).
 */
public class ConsumoAPI {

    private final String base;
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NORMAL).build();

    public ConsumoAPI() {
        this(valor());
    }

    public ConsumoAPI(String base) {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private static String valor() {
        String v = System.getProperty("paises.api.url");
        if (v == null || v.isBlank()) {
            v = System.getenv("PAISES_API_URL");
        }
        return (v == null || v.isBlank()) ? "https://www.apicountries.com" : v;
    }

    /** Todos los paises (JSON: arreglo). */
    public String obtenerTodos() throws IOException {
        return get(base + "/countries");
    }

    /** Paises cuyo nombre coincide con el texto (JSON: arreglo; 404 si no hay). */
    public String obtenerPorNombre(String nombre) throws IOException {
        return get(base + "/name/" + URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    private String get(String url) throws IOException {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(40))
                .header("Accept", "application/json").header("User-Agent", "ApiPaises-Taller/1.0").GET().build();
        try {
            HttpResponse<String> r = cliente.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() == 404) {
                return "[]";
            }
            if (r.statusCode() / 100 != 2) {
                throw new IOException("La API respondio " + r.statusCode());
            }
            return r.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Consulta interrumpida", e);
        }
    }
}
