package ec.edu.monster.clienteweb.controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** GET: dice si hay sesion activa. POST: cierra la sesion. */
@WebServlet("/api/sesion")
public class SesionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Json.escribir(resp, HttpServletResponse.SC_OK, "{\"activa\":" + (Servicios.token(req) != null) + "}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.invalidate();
        }
        Json.escribir(resp, HttpServletResponse.SC_OK, "{\"activa\":false}");
    }
}
