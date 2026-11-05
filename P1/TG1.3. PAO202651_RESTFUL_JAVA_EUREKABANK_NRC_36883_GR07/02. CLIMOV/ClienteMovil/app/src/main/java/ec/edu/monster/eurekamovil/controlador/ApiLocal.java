package ec.edu.monster.eurekamovil.controlador;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.eurekamovil.modelo.ErrorServicio;
import ec.edu.monster.eurekamovil.modelo.Resultado;
import ec.edu.monster.eurekamovil.modelo.RestClient;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Atiende las llamadas de la interfaz (mismas rutas del cliente web: sesion, login, salir, cuentas, movimientos, tipos y operar)
 * y las traduce a la API REST de EurekaBank con RestClient. Guarda la sesion en memoria.
 */
public class ApiLocal {

    /** Estado HTTP y cuerpo JSON de una respuesta. */
    public static final class Respuesta {
        public final int estado;
        public final String json;

        public Respuesta(int estado, String json) {
            this.estado = estado;
            this.json = json;
        }
    }

    private static final Gson JSON = new Gson();
    private RestClient cliente;
    private String usuario = "";

    public synchronized Respuesta atender(String metodo, String rutaCompleta, String cuerpo) {
        String ruta = rutaCompleta;
        String consulta = "";
        int q = rutaCompleta.indexOf('?');
        if (q >= 0) {
            ruta = rutaCompleta.substring(0, q);
            consulta = rutaCompleta.substring(q + 1);
        }
        try {
            switch (ruta) {
                case "sesion": {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("activa", cliente != null);
                    m.put("usuario", usuario);
                    return ok(m);
                }
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

    private static String cad(JsonObject o, String campo) {
        return o.has(campo) && !o.get(campo).isJsonNull() ? o.get(campo).getAsString() : "";
    }

    private Respuesta login(String cuerpo) {
        JsonObject n = JsonParser.parseString(cuerpo).getAsJsonObject();
        String u = cad(n, "usuario").trim();
        String clave = cad(n, "contrasena");
        if (u.isEmpty() || clave.isEmpty()) {
            return new Respuesta(400, texto(mensaje(false, "Escriba el usuario y la contraseña")));
        }
        String url = urlValida(cad(n, "servidor").trim());
        if (url == null) {
            return new Respuesta(400, texto(mensaje(false, "La dirección del servidor no es válida (use http://host:puerto/.../api/)")));
        }
        RestClient nuevo = new RestClient(url.isEmpty() ? RestClient.URL_POR_DEFECTO : url);
        Resultado r = nuevo.login(u, clave);
        if (!r.ok) {
            return new Respuesta(401, texto(mensaje(false, r.mensaje)));
        }
        cliente = nuevo;
        usuario = u;
        return ok(mensaje(true, r.mensaje));
    }

    private Respuesta operar(String cuerpo) {
        RestClient c = cliente();
        JsonObject n = JsonParser.parseString(cuerpo).getAsJsonObject();
        double importe;
        try {
            importe = n.get("importe").getAsDouble();
        } catch (Exception e) {
            return new Respuesta(400, texto(mensaje(false, "El importe debe ser un número")));
        }
        Resultado r = c.registrar(cad(n, "cuentaOrigen"), cad(n, "cuentaDestino"), cad(n, "tipo"), importe);
        return new Respuesta(r.ok ? 200 : 422, texto(mensaje(r.ok, r.mensaje)));
    }

    /** "" si no se escribio nada, la URL si es http(s) valida, o null si no sirve. */
    public static String urlValida(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
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
                try {
                    return URLDecoder.decode(par.substring(i + 1), "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    return null;
                }
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
        return JSON.toJson(o);
    }
}
