package ec.edu.monster.paises.vista;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.paises.controlador.PaisController;
import ec.edu.monster.paises.modelo.Pais;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Vista del patron MVC: expone el controlador como API JSON y sirve la interfaz web
 * (HTML, CSS y JS de src/main/resources/web). El celular Android consume la misma API.
 */
public class ServidorWeb {

    private static final Gson GSON = new Gson();
    private final PaisController controlador;
    private HttpServer servidor;

    public ServidorWeb(PaisController controlador) {
        this.controlador = controlador;
    }

    public int iniciar(String host, int puerto) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(host, puerto), 0);
        servidor.createContext("/", this::atender);
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        servidor.start();
        return servidor.getAddress().getPort();
    }

    public void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    // ------------------------------------------------------------------ enrutamiento
    private void atender(HttpExchange ex) throws IOException {
        try {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            String metodo = ex.getRequestMethod();
            String ruta = ex.getRequestURI().getPath();
            if ("OPTIONS".equals(metodo)) {
                responder(ex, 204, null);
                return;
            }
            if (ruta.equals("/api") || ruta.startsWith("/api/")) {
                api(ex, metodo, ruta);
            } else if ("GET".equals(metodo) || "HEAD".equals(metodo)) {
                estatico(ex, ruta);
            } else {
                json(ex, 405, error("Método no permitido"));
            }
        } catch (PaisController.ErrorDeNegocio e) {
            json(ex, 400, error(e.getMessage()));
        } catch (JsonParseException | NumberFormatException e) {
            json(ex, 400, error("Los datos enviados no son válidos"));
        } catch (SQLException e) {
            json(ex, 500, error("Error de base de datos: " + e.getMessage()));
        } catch (IOException e) {
            json(ex, 502, error("No se pudo consultar la API de países: " + e.getMessage()));
        } finally {
            ex.close();
        }
    }

    private void api(HttpExchange ex, String metodo, String ruta) throws IOException, SQLException, PaisController.ErrorDeNegocio {
        Map<String, String> q = consulta(ex);
        String[] t = ruta.substring(Math.min(ruta.length(), 5)).split("/");   // quita "/api/"
        String recurso = t.length > 0 ? t[0] : "";
        switch (recurso) {
            case "salud" -> json(ex, 200, Map.of("estado", "ok", "paises", controlador.listar(null, null, null).size()));
            case "importar" -> {
                exigir(metodo, "POST");
                json(ex, 200, controlador.importarPaises());
            }
            case "reportes" -> json(ex, 200, controlador.reportes());
            case "externo" -> externo(ex, metodo, t, q);
            case "paises" -> paises(ex, metodo, t, q);
            default -> json(ex, 404, error("Ruta desconocida"));
        }
    }

    private void paises(HttpExchange ex, String metodo, String[] t, Map<String, String> q) throws IOException, SQLException, PaisController.ErrorDeNegocio {
        if (t.length == 1) {
            if ("GET".equals(metodo)) {
                json(ex, 200, controlador.listar(q.get("nombre"), q.get("capital"), q.get("region")));
            } else if ("POST".equals(metodo)) {
                json(ex, 201, controlador.crear(leerPais(ex)));
            } else {
                json(ex, 405, error("Método no permitido"));
            }
            return;
        }
        int id = Integer.parseInt(t[1]);
        switch (metodo) {
            case "GET" -> json(ex, 200, controlador.obtener(id));
            case "PUT" -> json(ex, 200, controlador.actualizar(id, leerPais(ex)));
            case "DELETE" -> {
                controlador.eliminar(id);
                json(ex, 200, Map.of("eliminado", id));
            }
            default -> json(ex, 405, error("Método no permitido"));
        }
    }

    private void externo(HttpExchange ex, String metodo, String[] t, Map<String, String> q) throws IOException, SQLException, PaisController.ErrorDeNegocio {
        if (t.length > 1 && "guardar".equals(t[1])) {
            exigir(metodo, "POST");
            JsonObject o = GSON.fromJson(cuerpo(ex), JsonObject.class);
            String nombre = o.has("nombre") ? o.get("nombre").getAsString() : "";
            String codigo = o.has("codigo") ? o.get("codigo").getAsString() : "";
            json(ex, 201, controlador.guardarDesdeApi(nombre, codigo));
        } else {
            json(ex, 200, controlador.consultarEnApi(q.get("nombre")));
        }
    }

    private static void exigir(String metodo, String esperado) throws PaisController.ErrorDeNegocio {
        if (!esperado.equals(metodo)) {
            throw new PaisController.ErrorDeNegocio("Usa el método " + esperado);
        }
    }

    private Pais leerPais(HttpExchange ex) throws IOException {
        Pais p = GSON.fromJson(cuerpo(ex), Pais.class);
        if (p == null) {
            throw new JsonParseException("vacío");
        }
        // pasa por los setters para normalizar espacios y nulos
        Pais limpio = new Pais();
        limpio.setCodigo(p.getCodigo());
        limpio.setNombre(p.getNombre());
        limpio.setCapital(p.getCapital());
        limpio.setRegion(p.getRegion());
        limpio.setSubregion(p.getSubregion());
        limpio.setPoblacion(p.getPoblacion());
        limpio.setArea(p.getArea());
        limpio.setMoneda(p.getMoneda());
        limpio.setBandera(p.getBandera());
        return limpio;
    }

    // ------------------------------------------------------------------ archivos de la interfaz web
    private void estatico(HttpExchange ex, String ruta) throws IOException {
        if (ruta.equals("/")) {
            ruta = "/index.html";
        }
        if (ruta.contains("..")) {
            json(ex, 400, error("Ruta no válida"));
            return;
        }
        try (InputStream in = ServidorWeb.class.getResourceAsStream("/web" + ruta)) {
            if (in == null) {
                json(ex, 404, error("No existe " + ruta));
                return;
            }
            byte[] datos = in.readAllBytes();
            ex.getResponseHeaders().add("Content-Type", tipo(ruta));
            ex.getResponseHeaders().add("Cache-Control", ruta.startsWith("/fonts/") ? "public, max-age=604800" : "no-cache");
            responder(ex, 200, datos);
        }
    }

    private static String tipo(String ruta) {
        if (ruta.endsWith(".html")) return "text/html; charset=utf-8";
        if (ruta.endsWith(".css")) return "text/css; charset=utf-8";
        if (ruta.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (ruta.endsWith(".svg")) return "image/svg+xml";
        if (ruta.endsWith(".woff2")) return "font/woff2";
        if (ruta.endsWith(".png")) return "image/png";
        if (ruta.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    // ------------------------------------------------------------------ utilidades
    private static Map<String, String> consulta(HttpExchange ex) {
        Map<String, String> m = new HashMap<>();
        String raw = ex.getRequestURI().getRawQuery();
        if (raw != null) {
            for (String par : raw.split("&")) {
                int i = par.indexOf('=');
                String k = URLDecoder.decode(i < 0 ? par : par.substring(0, i), StandardCharsets.UTF_8);
                String v = i < 0 ? "" : URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8);
                m.put(k, v);
            }
        }
        return m;
    }

    private static String cuerpo(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, Object> error(String mensaje) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", mensaje);
        return m;
    }

    private static void json(HttpExchange ex, int codigo, Object objeto) throws IOException {
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        responder(ex, codigo, GSON.toJson(objeto).getBytes(StandardCharsets.UTF_8));
    }

    private static void responder(HttpExchange ex, int codigo, byte[] datos) throws IOException {
        if (datos == null || "HEAD".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(codigo, -1);
            return;
        }
        ex.sendResponseHeaders(codigo, datos.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(datos);
        }
    }
}
