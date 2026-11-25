package ec.edu.monster.paises.movil.modelo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Habla con el servidor Java (ApiPaises) por HTTP y JSON. No guarda nada: la base de datos esta en el servidor. */
public class ClienteApi {

    public static class ErrorApi extends Exception {
        public ErrorApi(String mensaje) {
            super(mensaje);
        }
    }

    /** El emulador de Android ve el computador anfitrion en 10.0.2.2. */
    public static final String BASE_POR_DEFECTO = "http://10.0.2.2:8091";

    private final SharedPreferences preferencias;

    public ClienteApi(Context contexto) {
        this.preferencias = contexto.getSharedPreferences("atlas", Context.MODE_PRIVATE);
    }

    public String base() {
        String b = preferencias.getString("servidor", BASE_POR_DEFECTO).trim();
        while (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        return b;
    }

    public void guardarBase(String direccion) {
        String d = direccion == null ? "" : direccion.trim();
        if (!d.isEmpty() && !d.startsWith("http")) d = "http://" + d;
        preferencias.edit().putString("servidor", d.isEmpty() ? BASE_POR_DEFECTO : d).apply();
    }

    // ------------------------------------------------------------------ paises
    public List<Pais> listar(String nombre, String capital, String region) throws ErrorApi {
        StringBuilder q = new StringBuilder("/api/paises?");
        agregar(q, "nombre", nombre);
        agregar(q, "capital", capital);
        agregar(q, "region", region);
        return lista(enviar("GET", q.toString(), null, 15000));
    }

    public Pais crear(Pais p) throws ErrorApi {
        return uno("POST", "/api/paises", p);
    }

    public Pais actualizar(Pais p) throws ErrorApi {
        return uno("PUT", "/api/paises/" + p.id, p);
    }

    public void eliminar(int id) throws ErrorApi {
        enviar("DELETE", "/api/paises/" + id, null, 15000);
    }

    /** Descarga los paises de la API publica y los guarda en la base (sin duplicar). */
    public String importar() throws ErrorApi {
        JSONObject r = objeto(enviar("POST", "/api/importar", "{}", 120000));
        return r.optInt("recibidos") + " recibidos: " + r.optInt("nuevos") + " nuevos y " + r.optInt("actualizados") + " actualizados";
    }

    // ------------------------------------------------------------------ API publica
    public List<Pais> consultarEnApi(String nombre) throws ErrorApi {
        StringBuilder q = new StringBuilder("/api/externo?");
        agregar(q, "nombre", nombre);
        return lista(enviar("GET", q.toString(), null, 40000));
    }

    public void guardarDesdeApi(String nombre, String codigo) throws ErrorApi {
        try {
            JSONObject cuerpo = new JSONObject().put("nombre", nombre).put("codigo", codigo);
            enviar("POST", "/api/externo/guardar", cuerpo.toString(), 40000);
        } catch (JSONException e) {
            throw new ErrorApi("No se pudo preparar la solicitud");
        }
    }

    // ------------------------------------------------------------------ universidades (solo el servidor .NET las tiene)
    public List<Universidad> universidades(String nombre, String pais) throws ErrorApi {
        StringBuilder q = new StringBuilder("/api/universidades?");
        agregar(q, "nombre", nombre);
        agregar(q, "pais", pais);
        return listaUniversidades(enviar("GET", q.toString(), null, 15000));
    }

    public List<Universidad> universidadesEnApi(String pais, String universidad) throws ErrorApi {
        StringBuilder q = new StringBuilder("/api/universidades/externo?");
        agregar(q, "pais", pais);
        agregar(q, "universidad", universidad);
        return listaUniversidades(enviar("GET", q.toString(), null, 40000));
    }

    /** Guarda todas las universidades de un pais; devuelve un texto listo para mostrar. */
    public String importarUniversidades(String pais, String universidad) throws ErrorApi {
        try {
            JSONObject cuerpo = new JSONObject().put("pais", pais).put("universidad", universidad);
            JSONObject r = objeto(enviar("POST", "/api/universidades/importar", cuerpo.toString(), 60000));
            return r.optInt("nuevas") + " universidades guardadas (de " + r.optInt("recibidas") + " encontradas)";
        } catch (JSONException e) {
            throw new ErrorApi("No se pudo preparar la solicitud");
        }
    }

    public void eliminarUniversidad(int id) throws ErrorApi {
        enviar("DELETE", "/api/universidades/" + id, null, 15000);
    }

    private static List<Universidad> listaUniversidades(String json) throws ErrorApi {
        try {
            JSONArray a = new JSONArray(json);
            List<Universidad> r = new ArrayList<>();
            for (int i = 0; i < a.length(); i++) r.add(Universidad.desdeJson(a.getJSONObject(i)));
            return r;
        } catch (JSONException e) {
            throw new ErrorApi("La respuesta del servidor no se entiende");
        }
    }

    // ------------------------------------------------------------------ reportes
    public Reporte reportes() throws ErrorApi {
        return Reporte.desdeJson(objeto(enviar("GET", "/api/reportes", null, 15000)));
    }

    // ------------------------------------------------------------------ interno
    private static void agregar(StringBuilder q, String clave, String valor) {
        if (valor == null || valor.trim().isEmpty()) return;
        try {
            if (q.charAt(q.length() - 1) != '?') q.append('&');
            q.append(clave).append('=').append(URLEncoder.encode(valor.trim(), "UTF-8"));
        } catch (java.io.UnsupportedEncodingException ignorada) {
            // UTF-8 siempre existe
        }
    }

    private Pais uno(String metodo, String ruta, Pais p) throws ErrorApi {
        try {
            return Pais.desdeJson(objeto(enviar(metodo, ruta, p.aJson().toString(), 15000)));
        } catch (JSONException e) {
            throw new ErrorApi("No se pudo preparar la solicitud");
        }
    }

    private static List<Pais> lista(String json) throws ErrorApi {
        try {
            JSONArray a = new JSONArray(json);
            List<Pais> r = new ArrayList<>();
            for (int i = 0; i < a.length(); i++) r.add(Pais.desdeJson(a.getJSONObject(i)));
            return r;
        } catch (JSONException e) {
            throw new ErrorApi("La respuesta del servidor no se entiende");
        }
    }

    private static JSONObject objeto(String json) throws ErrorApi {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            throw new ErrorApi("La respuesta del servidor no se entiende");
        }
    }

    private String enviar(String metodo, String ruta, String cuerpo, int lecturaMs) throws ErrorApi {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(base() + ruta).openConnection();
            c.setRequestMethod(metodo);
            c.setConnectTimeout(6000);
            c.setReadTimeout(lecturaMs);
            c.setRequestProperty("Accept", "application/json");
            if (cuerpo != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream o = c.getOutputStream()) {
                    o.write(cuerpo.getBytes(StandardCharsets.UTF_8));
                }
            }
            int codigo = c.getResponseCode();
            InputStream flujo = codigo >= 400 ? c.getErrorStream() : c.getInputStream();
            String texto = flujo == null ? "" : leer(flujo);
            if (codigo >= 400) {
                String mensaje = "Algo salió mal (" + codigo + ")";
                try {
                    String e = new JSONObject(texto).optString("error", "");
                    if (!e.isEmpty()) mensaje = e;
                } catch (JSONException ignorada) {
                    // el cuerpo no era JSON: se deja el mensaje generico
                }
                throw new ErrorApi(mensaje);
            }
            return texto;
        } catch (IOException e) {
            throw new ErrorApi("No hay conexión con el servidor en " + base() + ". ¿Está encendido?");
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static String leer(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
        in.close();
        return b.toString("UTF-8");
    }
}
