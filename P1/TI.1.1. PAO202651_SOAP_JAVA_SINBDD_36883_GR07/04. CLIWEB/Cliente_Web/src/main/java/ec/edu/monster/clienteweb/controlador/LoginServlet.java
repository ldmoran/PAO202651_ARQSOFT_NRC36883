package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
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
        RespuestaServicio r = Servicios.cliente(getServletContext())
                .login(req.getParameter("usuario"), req.getParameter("clave"));
        if (r.esExitosa() && r.getToken() != null) {
            HttpSession anterior = req.getSession(false);
            if (anterior != null) {
                anterior.invalidate(); // nueva sesion en cada login
            }
            req.getSession(true).setAttribute(Servicios.ATRIBUTO_TOKEN, r.getToken());
            Json.escribir(resp, HttpServletResponse.SC_OK, Json.de(r));
        } else {
            int http = RespuestaServicio.COD_RED.equals(r.getCodigo())
                    ? HttpServletResponse.SC_BAD_GATEWAY : HttpServletResponse.SC_UNAUTHORIZED;
            Json.escribir(resp, http, Json.de(r));
        }
    }
}
