package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CU-02 y CU-03: sin parametros devuelve las categorias; con ?categoria=X devuelve
 * las unidades de esa categoria.
 */
@WebServlet("/api/catalogo")
public class CatalogoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String token = Servicios.exigirSesion(req, resp);
        if (token == null) {
            return;
        }
        String categoria = req.getParameter("categoria");
        RespuestaServicio r = (categoria == null || categoria.isBlank())
                ? Servicios.cliente(getServletContext()).obtenerCategorias(token)
                : Servicios.cliente(getServletContext()).obtenerUnidades(token, categoria);
        Servicios.responder(req, resp, r);
    }
}
