package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import ec.edu.monster.clienteweb.modelo.SoapClient;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Acceso comun de los servlets: cliente SOAP y token de la sesion. */
public final class Servicios {

    public static final String ATRIBUTO_TOKEN = "token";
    public static final String ATRIBUTO_SERVIDOR = "servidor";

    private Servicios() {
    }

    /** Cliente SOAP con la URL del parametro de contexto "conuni.url" (web.xml). */
    public static SoapClient cliente(ServletContext ctx) {
        String url = ctx.getInitParameter("conuni.url");
        return new SoapClient(url);
    }

    /** Cliente SOAP con el servidor que se escribio al iniciar sesion; si no se escribio, el de web.xml. */
    public static SoapClient cliente(ServletContext ctx, HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        String url = s == null ? null : (String) s.getAttribute(ATRIBUTO_SERVIDOR);
        return url == null ? cliente(ctx) : new SoapClient(url);
    }

    /** La direccion del servidor debe ser una URL http o https. */
    public static boolean urlValida(String url) {
        return url != null && (url.regionMatches(true, 0, "http://", 0, 7) || url.regionMatches(true, 0, "https://", 0, 8));
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
