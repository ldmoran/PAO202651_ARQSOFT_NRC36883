package ec.edu.monster.clienteescritorio.controlador;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.monster.clienteescritorio.modelo.ErrorServicio;
import ec.edu.monster.clienteescritorio.modelo.Resultado;
import ec.edu.monster.clienteescritorio.modelo.RestClient;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atiende las llamadas de la interfaz (las mismas rutas que usa el cliente web: login, sesion, cuentas, movimientos,
 * tipos, operar y salir) y las traduce a la API REST de EurekaBank con RestClient. Guarda la sesion en memoria.
 */
public class ApiLocal {

    /** Estado HTTP y cuerpo JSON de una respuesta. */
    public record Respuesta(int estado, String json) {
    }

    private static final ObjectMapper JSON = new ObjectMapper();
    private RestClient cliente;
    private String usuario = "";

    public Respuesta atender(String metodo, String rutaCompleta, String cuerpo) {
        String ruta = rutaCompleta;
        String consulta = "";
        int q = rutaCompleta.indexOf('?');
        if (q >= 0) {
            ruta = rutaCompleta.substring(0, q);
            consulta = rutaCompleta.substring(q + 1);
        }
        try {
            switch (ruta) {
                case "sesion":
                    return ok(Map.of("activa", cliente != null, "usuario", usuario));
                case "login":
                    return login(cuerpo);
                case "salir":
                    cliente = null;
                    usuario = "";
                    return ok(mensaje(true, "Sesión cerrada"));
                case "cuentas":
                    return ok(cliente().cuentas());
                case "movimientos":
                    return ok(cliente().movimientos(parametro(consulta, "cuenta")));
                case "tipos":
                    return ok(cliente().tipos());
                case "operar":
                    return operar(cuerpo);
                default:
                    return new Respuesta(404, texto(mensaje(false, "Recurso no encontrado")));
            }
        } catch (SinSesion e) {
            return new Respuesta(401, texto(mensaje(false, "Sesión no iniciada")));
        } catch (ErrorServicio e) {
            return new Respuesta(502, texto(mensaje(false, e.getMessage())));
        } catch (Exception e) {
            return new Respuesta(500, texto(mensaje(false, "Error inesperado")));
        }
    }

    private Respuesta login(String cuerpo) throws Exception {
        JsonNode n = JSON.readTree(cuerpo);
        String u = n.path("usuario").asText("").trim();
        String clave = n.path("contrasena").asText("");
        if (u.isEmpty() || clave.isEmpty()) {
            return new Respuesta(400, texto(mensaje(false, "Escriba el usuario y la contraseña")));
        }
        String url = urlValida(n.path("servidor").asText("").trim());
        if (url == null) {
            return new Respuesta(400, texto(mensaje(false, "La dirección del servidor no es válida (use http://host:puerto/.../api/)")));
        }
        RestClient nuevo = new RestClient(url.isEmpty() ? RestClient.urlConfigurada() : url);
        Resultado r = nuevo.login(u, clave);
        if (!r.ok()) {
            return new Respuesta(401, texto(mensaje(false, r.mensaje())));
        }
        cliente = nuevo;
        usuario = u;
        return ok(mensaje(true, r.mensaje()));
    }

    private Respuesta operar(String cuerpo) throws Exception {
        RestClient c = cliente();
        JsonNode n = JSON.readTree(cuerpo);
        double importe = n.path("importe").asDouble(Double.NaN);
        if (Double.isNaN(importe)) {
            return new Respuesta(400, texto(mensaje(false, "El importe debe ser un número")));
        }
        Resultado r = c.registrar(n.path("cuentaOrigen").asText(""), n.path("cuentaDestino").asText(""), n.path("tipo").asText(""), importe);
        return new Respuesta(r.ok() ? 200 : 422, texto(mensaje(r.ok(), r.mensaje())));
    }

    /** "" si no se escribio nada, la URL si es http(s) valida, o null si no sirve. */
    public static String urlValida(String texto) {
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

    private RestClient cliente() {
        if (cliente == null) {
            throw new SinSesion();
        }
        return cliente;
    }

    private static String parametro(String consulta, String nombre) {
        for (String par : consulta.split("&")) {
            int i = par.indexOf('=');
            if (i > 0 && par.substring(0, i).equals(nombre)) {
                return URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static Map<String, Object> mensaje(boolean ok, String texto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", ok);
        m.put("mensaje", texto);
        return m;
    }

    private static Respuesta ok(Object cuerpo) {
        return new Respuesta(200, texto(cuerpo));
    }

    private static String texto(Object o) {
        try {
            return JSON.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}
