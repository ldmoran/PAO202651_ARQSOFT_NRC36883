package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import ec.edu.monster.clienteweb.modelo.SoapClient;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** CU-01: inicia sesion en el servicio SOAP y guarda el token en la sesion del servidor web. */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String servidor = req.getParameter("servidor") == null ? "" : req.getParameter("servidor").trim();
        if (!servidor.isEmpty() && !Servicios.urlValida(servidor)) {
            Json.escribir(resp, HttpServletResponse.SC_BAD_REQUEST, Json.de(RespuestaServicio.errorLocal(
                    "DATA-001", "La dirección del servidor debe empezar con http:// o https://")));
            return;
        }
        RespuestaServicio r = (servidor.isEmpty() ? Servicios.cliente(getServletContext()) : new SoapClient(servidor))
                .login(req.getParameter("usuario"), req.getParameter("clave"));
        if (r.esExitosa() && r.getToken() != null) {
            HttpSession anterior = req.getSession(false);
            if (anterior != null) {
                anterior.invalidate(); // nueva sesion en cada login
            }
            HttpSession nueva = req.getSession(true);
            nueva.setAttribute(Servicios.ATRIBUTO_TOKEN, r.getToken());
            if (!servidor.isEmpty()) {
                nueva.setAttribute(Servicios.ATRIBUTO_SERVIDOR, servidor);
            }
            Json.escribir(resp, HttpServletResponse.SC_OK, Json.de(r));
        } else {
            int http = RespuestaServicio.COD_RED.equals(r.getCodigo())
                    ? HttpServletResponse.SC_BAD_GATEWAY : HttpServletResponse.SC_UNAUTHORIZED;
            Json.escribir(resp, http, Json.de(r));
        }
    }
}
