package ec.edu.monster.clienteweb.controlador;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.monster.clienteweb.modelo.ErrorServicio;
import ec.edu.monster.clienteweb.modelo.Resultado;
import ec.edu.monster.clienteweb.modelo.RestClient;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador del cliente web. El navegador solo habla con este servlet (JSON); el servlet llama a la API REST
 * de EurekaBank y guarda en la sesion del servidor web el cliente REST (URL elegida) y el usuario.
 * Rutas: POST login, POST salir, GET sesion, GET cuentas, GET movimientos, GET tipos y POST operar.
 */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    static final String ATRIBUTO_CLIENTE = "clienteRest";
    static final String ATRIBUTO_USUARIO = "usuario";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ruta = ruta(req);
        try {
            switch (ruta) {
                case "sesion" -> {
                    HttpSession s = req.getSession(false);
                    boolean activa = s != null && s.getAttribute(ATRIBUTO_CLIENTE) != null;
                    escribir(resp, 200, Map.of("activa", activa, "usuario", activa ? String.valueOf(s.getAttribute(ATRIBUTO_USUARIO)) : ""));
                }
                case "cuentas" -> escribir(resp, 200, cliente(req).cuentas());
                case "movimientos" -> escribir(resp, 200, cliente(req).movimientos(req.getParameter("cuenta")));
                case "tipos" -> escribir(resp, 200, cliente(req).tipos());
                default -> escribir(resp, 404, mensaje(false, "Recurso no encontrado"));
            }
        } catch (SinSesion e) {
            escribir(resp, 401, mensaje(false, "Sesión no iniciada"));
        } catch (ErrorServicio e) {
            escribir(resp, 502, mensaje(false, e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ruta = ruta(req);
        JsonNode cuerpo;
        try {
            String texto = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            cuerpo = texto.isBlank() ? JSON.createObjectNode() : JSON.readTree(texto);
        } catch (IOException e) {
            escribir(resp, 400, mensaje(false, "El cuerpo de la solicitud no es un JSON válido"));
            return;
        }
        try {
            switch (ruta) {
                case "login" -> login(req, resp, cuerpo);
                case "salir" -> {
                    HttpSession s = req.getSession(false);
                    if (s != null) {
                        s.invalidate();
                    }
                    escribir(resp, 200, mensaje(true, "Sesión cerrada"));
                }
                case "operar" -> {
                    RestClient c = cliente(req);
                    double importe = cuerpo.path("importe").asDouble(Double.NaN);
                    if (Double.isNaN(importe)) {
                        escribir(resp, 400, mensaje(false, "El importe debe ser un número"));
                        return;
                    }
                    Resultado r = c.registrar(cuerpo.path("cuentaOrigen").asText(""), cuerpo.path("cuentaDestino").asText(""),
                            cuerpo.path("tipo").asText(""), importe);
                    escribir(resp, r.ok() ? 200 : 422, mensaje(r.ok(), r.mensaje()));
                }
                default -> escribir(resp, 404, mensaje(false, "Recurso no encontrado"));
            }
        } catch (SinSesion e) {
            escribir(resp, 401, mensaje(false, "Sesión no iniciada"));
        } catch (ErrorServicio e) {
            escribir(resp, 502, mensaje(false, e.getMessage()));
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp, JsonNode cuerpo) throws IOException {
        String usuario = cuerpo.path("usuario").asText("").trim();
        String clave = cuerpo.path("contrasena").asText("");
        if (usuario.isEmpty() || clave.isEmpty()) {
            escribir(resp, 400, mensaje(false, "Escriba el usuario y la contraseña"));
            return;
        }
        String url = urlValida(cuerpo.path("servidor").asText("").trim());
        if (url == null) {
            escribir(resp, 400, mensaje(false, "La dirección del servidor no es válida (use http://host:puerto/.../api/)"));
            return;
        }
        RestClient c = new RestClient(url.isEmpty() ? getServletContext().getInitParameter("eureka.url") : url);
        Resultado r = c.login(usuario, clave);
        if (!r.ok()) {
            escribir(resp, 401, mensaje(false, r.mensaje()));
            return;
        }
        HttpSession anterior = req.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession nueva = req.getSession(true);
        nueva.setAttribute(ATRIBUTO_CLIENTE, c);
        nueva.setAttribute(ATRIBUTO_USUARIO, usuario);
        escribir(resp, 200, mensaje(true, r.mensaje()));
    }

    /** Devuelve "" si no se escribio nada, la URL si es http(s) valida, o null si no sirve. */
    static String urlValida(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        try {
            URI u = URI.create(texto.trim());
            boolean http = "http".equalsIgnoreCase(u.getScheme()) || "https".equalsIgnoreCase(u.getScheme());
            return http && u.getHost() != null ? texto.trim() : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static class SinSesion extends RuntimeException {
    }

    private RestClient cliente(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Object c = s == null ? null : s.getAttribute(ATRIBUTO_CLIENTE);
        if (c == null) {
            throw new SinSesion();
        }
        return (RestClient) c;
    }

    private static String ruta(HttpServletRequest req) {
        String info = req.getPathInfo();
        return info == null ? "" : info.replaceFirst("^/", "").replaceFirst("/$", "");
    }

    private static Map<String, Object> mensaje(boolean ok, String texto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", ok);
        m.put("mensaje", texto);
        return m;
    }

    private static void escribir(HttpServletResponse resp, int estado, Object cuerpo) throws IOException {
        resp.setStatus(estado);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        JSON.writeValue(resp.getOutputStream(), cuerpo);
    }
}
