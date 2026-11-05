package ec.edu.monster.web;

import ec.edu.monster.cliente.ComercializadoraSoap;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.ResultadoFactura;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Controlador unico del cliente web: recibe la peticion, llama al servicio SOAP y arma la pagina. */
@WebServlet(urlPatterns = {"/index", "/catalogo", "/venta", "/credito", "/facturas"})
public class ControladorWeb extends HttpServlet {

    private ComercializadoraSoap servicio;

    @Override
    public void init() {
        String url = getServletContext().getInitParameter("comercializadora.url");
        servicio = new ComercializadoraSoap(url == null || url.isBlank() ? ComercializadoraSoap.URL_POR_DEFECTO : url);
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

    private void atender(HttpServletRequest req, HttpServletResponse resp, boolean post) throws IOException {
        String ruta = req.getContextPath();
        String pagina = req.getServletPath();
        String html;
        try {
            html = switch (pagina) {
                case "/catalogo" -> catalogo(req, ruta, post);
                case "/venta" -> venta(req, ruta, post);
                case "/credito" -> credito(req, ruta, post);
                case "/facturas" -> facturas(req, ruta);
                default -> Vistas.inicio(ruta);
            };
        } catch (ComercializadoraSoap.ServicioNoDisponible e) {
            html = Vistas.pagina(ruta, "Inicio", e.getMessage(), true, "<p>Verifique que el servicio este encendido.</p>");
        }
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);
    }

    private static String param(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        return v == null ? "" : v.trim();
    }

    private static Double numero(String s) {
        try {
            return Double.valueOf(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer entero(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String catalogo(HttpServletRequest req, String ruta, boolean post) {
        String mensaje = null;
        boolean error = false;
        if (post) {
            String accion = param(req, "accion");
            Double precio = numero(param(req, "precio"));
            Integer cod = entero(param(req, "cod"));
            switch (accion) {
                case "agregar" -> {
                    int nuevo = precio == null ? 0 : servicio.crearTelefono(param(req, "marca"), param(req, "modelo"), precio);
                    error = nuevo == 0;
                    mensaje = error ? "Complete marca, modelo y un precio mayor a cero." : "Celular agregado con codigo " + nuevo + ".";
                }
                case "guardar" -> {
                    boolean ok = cod != null && precio != null && servicio.actualizarTelefono(cod, param(req, "marca"),
                            param(req, "modelo"), precio, req.getParameter("disponible") != null);
                    error = !ok;
                    mensaje = ok ? "Celular actualizado." : "No se pudo actualizar: revise los datos.";
                }
                case "eliminar" -> {
                    boolean ok = cod != null && servicio.eliminarTelefono(cod);
                    error = !ok;
                    mensaje = ok ? "Celular eliminado (si ya tenia facturas queda como no disponible)." : "No existe ese celular.";
                }
                default -> {
                    error = true;
                    mensaje = "Accion no valida.";
                }
            }
        }
        return Vistas.pagina(ruta, "Catalogo", mensaje, error, Vistas.catalogo(servicio.listarTelefonos()));
    }

    private String venta(HttpServletRequest req, String ruta, boolean post) {
        var telefonos = servicio.listarTelefonos();
        if (!post) {
            return Vistas.pagina(ruta, "Venta", null, false, Vistas.venta(telefonos, "", "", new java.util.HashMap<>(), false, 6, "", ""));
        }
        String cedula = param(req, "cedula");
        String nombre = param(req, "nombre");
        Integer plazo = entero(param(req, "plazo"));
        boolean credito = "credito".equals(param(req, "forma"));
        int plazoMostrado = plazo == null ? 6 : plazo;
        // cada celular tiene su cantidad; un codigo repetido en la lista es una unidad mas
        java.util.Map<Integer, Integer> cantidades = new java.util.LinkedHashMap<>();
        java.util.List<Integer> codigos = new java.util.ArrayList<>();
        for (ec.edu.monster.cliente.modelos.Telefono t : telefonos) {
            Integer n = entero(param(req, "cant_" + t.getCodTelefono()));
            if (n != null && n > 0) {
                cantidades.put(t.getCodTelefono(), n);
                for (int k = 0; k < n; k++) {
                    codigos.add(t.getCodTelefono());
                }
            }
        }
        if (codigos.isEmpty() || (credito && plazo == null)) {
            return Vistas.pagina(ruta, "Venta", codigos.isEmpty() ? "Escriba la cantidad de al menos un celular." : "Datos incompletos.", true,
                    Vistas.venta(telefonos, cedula, nombre, cantidades, credito, plazoMostrado, "", ""));
        }
        ResultadoFactura r = credito ? servicio.facturarCreditoVarios(cedula, nombre, codigos, plazo)
                : servicio.facturarEfectivoVarios(cedula, nombre, codigos);
        String factura = "";
        String amort = "";
        if (r.isExito() && r.getFactura() != null) {
            factura = Vistas.factura(r.getFactura());
            if (credito) {
                amort = Vistas.amortizacion(servicio.obtenerAmortizacion(r.getFactura().getCodCredito()));
            }
        }
        return Vistas.pagina(ruta, "Venta", r.getMensaje(), !r.isExito(),
                Vistas.venta(telefonos, cedula, nombre, r.isExito() ? new java.util.HashMap<>() : cantidades, credito, plazoMostrado, factura, amort));
    }

    private String credito(HttpServletRequest req, String ruta, boolean post) {
        String cedula = param(req, "cedula");
        EvaluacionCredito ev = null;
        DetalleMontoMaximo d = null;
        if (post) {
            ev = servicio.verificarSujetoCredito(cedula);
            d = ev.isSujetoCredito() ? servicio.calcularMontoMaximo(cedula) : null;
        }
        return Vistas.pagina(ruta, "Credito", ev == null ? null : ev.getMotivo(), ev != null && !ev.isSujetoCredito(),
                Vistas.credito(cedula, ev, d));
    }

    private String facturas(HttpServletRequest req, String ruta) {
        String cedula = param(req, "cedula");
        java.util.List<ec.edu.monster.cliente.modelos.Factura> lista = new java.util.ArrayList<>();
        String amort = "";
        String mensaje = null;
        if (!cedula.isEmpty()) {
            lista = servicio.consultarFacturas(cedula);
            mensaje = lista.isEmpty() ? "El cliente no tiene facturas." : lista.size() + " facturas encontradas.";
            Integer credito = entero(param(req, "credito"));
            if (credito != null) {
                amort = Vistas.amortizacion(servicio.obtenerAmortizacion(credito));
            }
        }
        return Vistas.pagina(ruta, "Facturas", mensaje, false, Vistas.facturas(ruta, cedula, lista, !cedula.isEmpty(), amort));
    }
}
