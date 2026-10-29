package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import ec.edu.monster.clienteweb.modelo.RestClient;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/** Acceso común de los servlets: cliente REST y token de la sesión. */
public final class Servicios {

    public static final String ATRIBUTO_TOKEN = "token";
    public static final String ATRIBUTO_SERVIDOR = "servidor";

    private Servicios() {
    }

    /** Cliente REST con la URL del parametro de contexto "conuni.url" (web.xml). */
    public static RestClient cliente(ServletContext ctx) {
        String url = ctx.getInitParameter("conuni.url");
        return new RestClient(url);
    }

    public static RestClient cliente(ServletContext ctx, String url) {
        return new RestClient(url == null || url.isBlank() ? ctx.getInitParameter("conuni.url") : url);
    }

    public static RestClient cliente(ServletContext ctx, HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String url = session == null ? null : (String) session.getAttribute(ATRIBUTO_SERVIDOR);
        return cliente(ctx, url);
    }

    public static boolean urlValida(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            return uri.isAbsolute()
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null
                    && uri.getRawUserInfo() == null
                    && uri.getRawQuery() == null
                    && uri.getRawFragment() == null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static String token(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (String) s.getAttribute(ATRIBUTO_TOKEN);
    }

    /** Responde 401 si no hay sesion; devuelve el token si existe, o null (ya respondio). */
    public static String exigirSesion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String token = token(req);
        if (token == null) {
            RespuestaServicio r = RespuestaServicio.errorLocal(RespuestaServicio.COD_TOKEN_INVALIDO, "Debe iniciar sesión");
            Json.escribir(resp, HttpServletResponse.SC_UNAUTHORIZED, Json.de(r));
        }
        return token;
    }

    /** Responde con el resultado del servicio y cierra la sesion si el token ya no sirve. */
    public static void responder(HttpServletRequest req, HttpServletResponse resp, RespuestaServicio r) throws IOException {
        int http = 200;
        if (r.requiereNuevoLogin()) {
            HttpSession s = req.getSession(false);
            if (s != null) {
                s.invalidate();
            }
            http = HttpServletResponse.SC_UNAUTHORIZED;
        } else if (!r.esExitosa()) {
            http = RespuestaServicio.COD_RED.equals(r.getCodigo()) ? HttpServletResponse.SC_BAD_GATEWAY : 200;
        }
        Json.escribir(resp, http, Json.de(r));
    }
}
