package ec.edu.monster.web;

import ec.edu.monster.cliente.Formato;
import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.Factura;
import ec.edu.monster.cliente.modelos.Telefono;
import java.util.List;
import java.util.Locale;

/** Arma el HTML de cada pagina. Todo texto que viene del usuario o del servicio pasa por {@link #e(String)}. */
public final class Vistas {

    private Vistas() {
    }

    public static String e(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String pagina(String ruta, String titulo, String mensaje, boolean mensajeEsError, String contenido) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(e(titulo)).append(" - Comercializadora</title>")
                .append("<link rel=\"stylesheet\" href=\"").append(ruta).append("/css/estilo.css\"></head><body>");
        sb.append("<header><h1>Comercializadora <span>venta de celulares</span></h1><nav>");
        for (String[] item : new String[][] {{"catalogo", "Catalogo"}, {"venta", "Venta"}, {"credito", "Credito"},
            {"facturas", "Facturas"}}) {
            sb.append("<a href=\"").append(ruta).append('/').append(item[0]).append("\"")
                    .append(item[1].equals(titulo) ? " class=\"activo\"" : "").append('>').append(item[1]).append("</a>");
        }
        sb.append("</nav></header><main><h2>").append(e(titulo)).append("</h2>");
        if (mensaje != null && !mensaje.isEmpty()) {
            sb.append("<p class=\"").append(mensajeEsError ? "error" : "ok").append("\">").append(e(mensaje)).append("</p>");
        }
        sb.append(contenido).append("</main></body></html>");
        return sb.toString();
    }

    public static String inicio(String ruta) {
        return pagina(ruta, "Inicio", null, false, "<p>Elija una opcion del menu.</p>");
    }

    // ---------- Catalogo ----------

    public static String catalogo(List<Telefono> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr><th>Cod</th><th>Marca</th><th>Modelo</th><th>Precio</th><th>Disponible</th><th></th></tr>");
        for (Telefono t : lista) {
            sb.append("<tr><form method=\"post\"><td>").append(t.getCodTelefono())
                    .append("<input type=\"hidden\" name=\"cod\" value=\"").append(t.getCodTelefono()).append("\"></td>")
                    .append("<td><input name=\"marca\" value=\"").append(e(t.getMarca())).append("\"></td>")
                    .append("<td><input name=\"modelo\" value=\"").append(e(t.getModelo())).append("\"></td>")
                    .append("<td><input name=\"precio\" size=\"8\" value=\"")
                    .append(String.format(Locale.US, "%.2f", t.getPrecio())).append("\"></td>")
                    .append("<td><input type=\"checkbox\" name=\"disponible\" value=\"1\"")
                    .append(t.isDisponible() ? " checked" : "").append("></td>")
                    .append("<td><button name=\"accion\" value=\"guardar\">Guardar</button> ")
                    .append("<button class=\"peligro\" name=\"accion\" value=\"eliminar\">Eliminar</button></td></form></tr>");
        }
        sb.append("</table><h3>Agregar celular</h3><form method=\"post\" class=\"fila\">")
                .append("<input name=\"marca\" placeholder=\"Marca\" required>")
                .append("<input name=\"modelo\" placeholder=\"Modelo\" required>")
                .append("<input name=\"precio\" placeholder=\"Precio\" size=\"8\" required>")
                .append("<button name=\"accion\" value=\"agregar\">Agregar</button></form>");
        return sb.toString();
    }

    // ---------- Venta ----------

    public static String formularioVenta(List<Telefono> telefonos, String cedula, String nombre) {
        StringBuilder sb = new StringBuilder("<form method=\"post\" class=\"columna\">");
        sb.append("<label>Cedula <input name=\"cedula\" maxlength=\"10\" value=\"").append(e(cedula)).append("\" required></label>")
                .append("<label>Nombre (solo si es cliente nuevo) <input name=\"nombre\" value=\"").append(e(nombre))
                .append("\"></label><label>Celular <select name=\"cod\">");
        for (Telefono t : telefonos) {
            if (t.isDisponible()) {
                sb.append("<option value=\"").append(t.getCodTelefono()).append("\">").append(e(t.getMarca())).append(' ')
                        .append(e(t.getModelo())).append(" - ").append(Formato.dinero(t.getPrecio())).append("</option>");
            }
        }
        sb.append("</select></label><label><input type=\"radio\" name=\"forma\" value=\"efectivo\" checked> ")
                .append("Efectivo (42 % de descuento)</label>")
                .append("<label><input type=\"radio\" name=\"forma\" value=\"credito\"> Credito, plazo ")
                .append("<input type=\"number\" name=\"plazo\" min=\"3\" max=\"18\" value=\"6\" size=\"3\"> meses</label>")
                .append("<button>Vender</button></form>");
        return sb.toString();
    }

    public static String factura(Factura f) {
        return "<div class=\"factura\"><h3>Factura N. " + f.getNumFactura() + "</h3><p>Fecha: " + e(f.getFecha())
                + "<br>Cliente: " + e(f.getNombreCliente()) + " (" + e(f.getCedula()) + ")<br>Celular: " + e(f.getTelefono())
                + "<br>Pago: " + e(f.getFormaPago())
                + (f.getNumCuotas() > 0 ? " a " + f.getNumCuotas() + " cuotas (credito " + f.getCodCredito() + ")" : "")
                + "<br>Precio: " + Formato.dinero(f.getPrecioLista()) + "<br>Descuento: " + Formato.dinero(f.getDescuento())
                + "<br><strong>TOTAL: " + Formato.dinero(f.getTotal()) + "</strong></p></div>";
    }

    public static String amortizacion(List<CuotaAmortizacion> filas) {
        if (filas.isEmpty()) {
            return "<p>No existe ese credito.</p>";
        }
        StringBuilder sb = new StringBuilder("<h3>Tabla de amortizacion</h3><table><tr><th>Cuota</th><th>Fecha</th>")
                .append("<th>Valor cuota</th><th>Interes</th><th>Capital</th><th>Saldo</th></tr>");
        for (CuotaAmortizacion c : filas) {
            sb.append("<tr><td>").append(c.getNumCuota()).append("</td><td>").append(e(c.getFechaPago())).append("</td>")
                    .append(num(c.getValorCuota())).append(num(c.getInteresPagado())).append(num(c.getCapitalPagado()))
                    .append(num(c.getSaldo())).append("</tr>");
        }
        return sb.append("</table>").toString();
    }

    private static String num(double v) {
        return "<td class=\"der\">" + String.format(Locale.US, "%.2f", v) + "</td>";
    }

    // ---------- Credito ----------

    public static String formularioCedula(String cedula, String boton) {
        return "<form method=\"post\" class=\"fila\"><input name=\"cedula\" placeholder=\"Cedula\" maxlength=\"10\" value=\""
                + e(cedula) + "\" required><button>" + e(boton) + "</button></form>";
    }

    public static String evaluacion(EvaluacionCredito ev, DetalleMontoMaximo d) {
        StringBuilder sb = new StringBuilder("<div class=\"factura\"><h3>")
                .append(ev.isSujetoCredito() ? "SI es sujeto de credito" : "NO es sujeto de credito").append("</h3><p>")
                .append(e(ev.getMotivo())).append("</p>");
        if (d != null) {
            sb.append("<p>Cliente: ").append(e(d.getNombre())).append("<br>Promedio de depositos (3 meses): ")
                    .append(Formato.dinero(d.getPromedioDepositos())).append("<br>Promedio de retiros (3 meses): ")
                    .append(Formato.dinero(d.getPromedioRetiros())).append("<br><strong>Monto maximo de credito: ")
                    .append(Formato.dinero(d.getMontoMaximo())).append("</strong></p>");
        }
        return sb.append("</div>").toString();
    }

    // ---------- Facturas ----------

    public static String facturas(String ruta, String cedula, List<Factura> lista) {
        if (lista.isEmpty()) {
            return "<p>El cliente no tiene facturas.</p>";
        }
        StringBuilder sb = new StringBuilder("<table><tr><th>N.</th><th>Fecha</th><th>Celular</th><th>Pago</th>")
                .append("<th>Total</th><th>Credito</th></tr>");
        for (Factura f : lista) {
            sb.append("<tr><td>").append(f.getNumFactura()).append("</td><td>").append(e(f.getFecha())).append("</td><td>")
                    .append(e(f.getTelefono())).append("</td><td>").append(e(f.getFormaPago())).append("</td><td class=\"der\">")
                    .append(Formato.dinero(f.getTotal())).append("</td><td>");
            if (f.getCodCredito() > 0) {
                sb.append("<a href=\"").append(ruta).append("/facturas?cedula=").append(e(cedula)).append("&amp;credito=")
                        .append(f.getCodCredito()).append("\">ver cuotas (").append(f.getCodCredito()).append(")</a>");
            }
            sb.append("</td></tr>");
        }
        return sb.append("</table>").toString();
    }
}
