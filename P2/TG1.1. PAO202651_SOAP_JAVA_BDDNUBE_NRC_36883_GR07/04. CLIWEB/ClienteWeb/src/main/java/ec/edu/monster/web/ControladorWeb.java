package ec.edu.monster.web;

import ec.edu.monster.cliente.EurekaSoap;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/** Controlador unico del cliente web: recibe la peticion, llama al servicio SOAP y arma la pagina. */
@WebServlet(urlPatterns = {"/index", "/cuentas", "/movimientos", "/operar", "/salir"})
public class ControladorWeb extends HttpServlet {

    private String urlServicio;

    @Override
    public void init() {
        String url = getServletContext().getInitParameter("eureka.url");
        urlServicio = url == null || url.isBlank() ? EurekaSoap.URL_POR_DEFECTO : url;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        atender(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        atender(req, resp, true);
    }

    private static String param(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        return v == null ? "" : v.trim();
    }

    private void atender(HttpServletRequest req, HttpServletResponse resp, boolean post) throws IOException {
        String ruta = req.getContextPath();
        String pagina = req.getServletPath();
        HttpSession sesion = req.getSession();
        if ("/salir".equals(pagina)) {
            sesion.invalidate();
            resp.sendRedirect(ruta + "/index");
            return;
        }
        String html;
        try {
            if ("/index".equals(pagina)) {
                if (sesion.getAttribute("usuario") != null) {
                    resp.sendRedirect(ruta + "/cuentas");
                    return;
                }
                html = post ? iniciarSesion(req, resp, sesion, ruta) : Vistas.login(ruta, "", urlServicio);
                if (html == null) {
                    return;
                }
            } else if (sesion.getAttribute("usuario") == null) {
                resp.sendRedirect(ruta + "/index");
                return;
            } else {
                String usuario = (String) sesion.getAttribute("usuario");
                EurekaSoap servicio = new EurekaSoap((String) sesion.getAttribute("url"));
                html = switch (pagina) {
                    case "/movimientos" -> movimientos(req, ruta, usuario, servicio);
                    case "/operar" -> operar(req, ruta, usuario, servicio, post);
                    default -> Vistas.pagina(ruta, "Cuentas", usuario, "", false, Vistas.cuentas(ruta, servicio.cuentas()));
                };
            }
        } catch (EurekaSoap.ServicioNoDisponible e) {
            html = Vistas.login(ruta, e.getMessage(), urlServicio);
        }
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }

    /** Devuelve null cuando ya redirigio (inicio de sesion correcto). */
    private String iniciarSesion(HttpServletRequest req, HttpServletResponse resp, HttpSession sesion, String ruta) throws IOException {
        String usuario = param(req, "usuario");
        String servidor = param(req, "servidor");
        String url = servidor.isEmpty() ? urlServicio : servidor;
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return Vistas.login(ruta, "La dirección del servicio debe empezar con http:// o https://", urlServicio);
        }
        if (new EurekaSoap(url).iniciarSesion(usuario, req.getParameter("clave") == null ? "" : req.getParameter("clave"))) {
            sesion.setAttribute("usuario", usuario);
            sesion.setAttribute("url", url);
            resp.sendRedirect(ruta + "/cuentas");
            return null;
        }
        return Vistas.login(ruta, "Usuario o clave incorrectos.", url);
    }

    private String movimientos(HttpServletRequest req, String ruta, String usuario, EurekaSoap servicio) {
        String cuenta = param(req, "cuenta");
        List<EurekaSoap.Movimiento> lista = servicio.movimientos(cuenta);
        return Vistas.pagina(ruta, "Movimientos", usuario, lista.size() + " movimientos.", false,
                Vistas.movimientos(servicio.cuentas(), cuenta, lista));
    }

    private String operar(HttpServletRequest req, String ruta, String usuario, EurekaSoap servicio, boolean post) {
        String mensaje = "";
        boolean error = false;
        String tipo = post ? param(req, "tipo") : "DEPOSITO";
        String origen = param(req, "origen");
        String destino = param(req, "destino");
        if (post) {
            double importe;
            try {
                importe = Double.parseDouble(param(req, "importe").replace(',', '.'));
            } catch (NumberFormatException e) {
                importe = 0;
            }
            if (importe <= 0) {
                mensaje = "Ingrese un importe mayor a 0 (por ejemplo 150.50).";
                error = true;
            } else {
                EurekaSoap.Resultado r = servicio.registrar(tipo, origen, "TRANSFERENCIA".equals(tipo) ? destino : "", importe);
                mensaje = r.mensaje;
                error = !r.ok;
            }
        }
        return Vistas.pagina(ruta, "Operar", usuario, mensaje, error, Vistas.operar(ruta, servicio.cuentas(), tipo, origen, destino));
    }
}
