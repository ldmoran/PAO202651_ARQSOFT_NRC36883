package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** CU-04: realiza una conversion llamando al servicio SOAP de la categoria elegida. */
@WebServlet("/api/convertir")
public class ConvertirServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String token = Servicios.exigirSesion(req, resp);
        if (token == null) {
            return;
        }
        double valor;
        try {
            String v = req.getParameter("valor");
            valor = Double.parseDouble(v == null ? "" : v.trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Servicios.responder(req, resp, RespuestaServicio.errorLocal("DATA-001", "El valor debe ser numérico"));
            return;
        }
        RespuestaServicio r = Servicios.cliente(getServletContext()).convertir(
                req.getParameter("categoria"), valor, req.getParameter("origen"), req.getParameter("destino"), token);
        Servicios.responder(req, resp, r);
    }
}
