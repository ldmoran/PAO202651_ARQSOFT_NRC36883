package ec.edu.monster.web;

import ec.edu.monster.cliente.Formato;
import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.Factura;
import ec.edu.monster.cliente.modelos.Telefono;
import java.util.List;
import java.util.Locale;

/**
 * Arma el HTML de cada pagina con el mismo diseno del cliente de escritorio: menu lateral azul, titulo con subtitulo,
 * tarjetas con titulo subrayado en dorado y barra de estado abajo. Todo texto que viene del usuario o del servicio
 * pasa por {@link #e(String)}.
 */
public final class Vistas {

    private static final String ICONO_CATALOGO = "<rect x=\"7\" y=\"2.5\" width=\"10\" height=\"19\" rx=\"2.5\"/><path d=\"M10.5 18.5h3\"/>";
    private static final String ICONO_VENTA = "<path d=\"M2.5 4h2.6l2.2 11h10.2\"/><path d=\"M6.2 8h14l-1.8 6.2H7.6\"/><circle cx=\"9\" cy=\"19\" r=\"1.4\"/><circle cx=\"17\" cy=\"19\" r=\"1.4\"/>";
    private static final String ICONO_CREDITO = "<path d=\"M3 10l9-6 9 6M5 10v8M9 10v8M15 10v8M19 10v8M3 21h18\"/>";
    private static final String ICONO_FACTURAS = "<path d=\"M6 2.5h12v19l-3-2-3 2-3-2-3 2z\"/><path d=\"M9 8h6M9 12h6\"/>";

    private Vistas() {
    }

    public static String e(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String titulo(String clave) {
        return switch (clave) {
            case "Catalogo" -> "Catálogo de celulares";
            case "Venta" -> "Nueva venta";
            case "Credito" -> "Consulta de crédito";
            case "Facturas" -> "Facturas del cliente";
            default -> "BanQuito";
        };
    }

    private static String subtitulo(String clave) {
        return switch (clave) {
            case "Catalogo" -> "Administre los teléfonos que se venden";
            case "Venta" -> "Venda de contado con 42 % de descuento o a crédito con el BanQuito";
            case "Credito" -> "Compruebe si un cliente es sujeto de crédito y hasta cuánto puede pedir";
            case "Facturas" -> "Consulte las compras y la tabla de amortización de cada crédito";
            default -> "Elija una opción del menú";
        };
    }

    private static String enlace(String ruta, String destino, String clave, String actual, String icono, String texto) {
        return "<a href=\"" + ruta + "/" + destino + "\"" + (clave.equals(actual) ? " class=\"activo\"" : "")
                + "><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.8\" stroke-linecap=\"round\" "
                + "stroke-linejoin=\"round\">" + icono + "</svg>" + texto + "</a>";
    }

    public static String pagina(String ruta, String actual, String mensaje, boolean mensajeEsError, String contenido) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(e(titulo(actual))).append(" - BanQuito</title>")
                .append("<link rel=\"stylesheet\" href=\"").append(ruta).append("/css/estilo.css\"></head><body>");
        sb.append("<div class=\"app\"><aside class=\"menu\"><div class=\"marca\"><span class=\"logo\">BQ</span>")
                .append("<div><strong>BanQuito</strong><small>Comercializadora</small></div></div><nav>")
                .append(enlace(ruta, "catalogo", "Catalogo", actual, ICONO_CATALOGO, "Catálogo"))
                .append(enlace(ruta, "venta", "Venta", actual, ICONO_VENTA, "Venta"))
                .append(enlace(ruta, "credito", "Credito", actual, ICONO_CREDITO, "Crédito"))
                .append(enlace(ruta, "facturas", "Facturas", actual, ICONO_FACTURAS, "Facturas"))
                .append("</nav></aside><div class=\"principal\"><header class=\"cabecera\"><h2>").append(e(titulo(actual)))
                .append("</h2><p>").append(e(subtitulo(actual))).append("</p></header><main class=\"cuerpo\">")
                .append(contenido).append("</main><footer class=\"estado\"><span class=\"punto ")
                .append(mensaje == null || mensaje.isEmpty() ? "" : mensajeEsError ? "mal" : "ok").append("\"></span>")
                .append(e(mensaje == null || mensaje.isEmpty() ? "Listo." : mensaje)).append("</footer></div></div></body></html>");
        return sb.toString();
    }

    public static String inicio(String ruta) {
        return pagina(ruta, "Inicio", null, false, "<div class=\"tarjeta\"><h3>Bienvenido</h3><p>Elija una opción del menú.</p></div>");
    }

    private static String tarjeta(String titulo, String contenido) {
        return "<section class=\"tarjeta\">" + (titulo == null ? "" : "<h3>" + e(titulo) + "</h3>") + contenido + "</section>";
    }

    // ---------- Catalogo ----------

    public static String catalogo(List<Telefono> lista) {
        StringBuilder sb = new StringBuilder();
        StringBuilder filas = new StringBuilder("<div class=\"tabla-caja\"><table><thead><tr><th>Cód.</th><th>Marca</th><th>Modelo</th>"
                + "<th>Precio</th><th>Estado</th><th></th></tr></thead><tbody>");
        for (Telefono t : lista) {
            String f = "f" + t.getCodTelefono();
            sb.append("<form id=\"").append(f).append("\" method=\"post\"><input type=\"hidden\" name=\"cod\" value=\"")
                    .append(t.getCodTelefono()).append("\"></form>");
            filas.append("<tr><td>").append(t.getCodTelefono()).append("</td>")
                    .append("<td><input form=\"").append(f).append("\" name=\"marca\" value=\"").append(e(t.getMarca())).append("\"></td>")
                    .append("<td><input form=\"").append(f).append("\" name=\"modelo\" value=\"").append(e(t.getModelo())).append("\"></td>")
                    .append("<td><input form=\"").append(f).append("\" name=\"precio\" class=\"corto\" value=\"")
                    .append(String.format(Locale.US, "%.2f", t.getPrecio())).append("\"></td>")
                    .append("<td><label class=\"interruptor\"><input form=\"").append(f).append("\" type=\"checkbox\" name=\"disponible\" value=\"1\"")
                    .append(t.isDisponible() ? " checked" : "").append("><span class=\"chip ").append(t.isDisponible() ? "ok" : "mal")
                    .append("\">").append(t.isDisponible() ? "Disponible" : "Agotado").append("</span></label></td>")
                    .append("<td class=\"acciones\"><button form=\"").append(f).append("\" name=\"accion\" value=\"guardar\" class=\"sec\">Guardar</button> ")
                    .append("<button form=\"").append(f).append("\" name=\"accion\" value=\"eliminar\" class=\"peligro\">Eliminar</button></td></tr>");
        }
        filas.append("</tbody></table></div>");
        sb.append(tarjeta("Catálogo", filas.toString()));
        sb.append(tarjeta("Agregar celular", "<form method=\"post\" class=\"fila\">"
                + "<label>Marca<input name=\"marca\" placeholder=\"Marca\" required></label>"
                + "<label>Modelo<input name=\"modelo\" placeholder=\"Modelo\" required></label>"
                + "<label>Precio<input name=\"precio\" class=\"corto\" placeholder=\"0.00\" required></label>"
                + "<button name=\"accion\" value=\"agregar\" class=\"dorado\">Agregar</button></form>"));
        return sb.toString();
    }

    // ---------- Venta ----------

    public static String venta(List<Telefono> telefonos, String cedula, String nombre, int codElegido, boolean credito, int plazo,
            String facturaHtml, String amortHtml) {
        StringBuilder sb = new StringBuilder("<form method=\"post\" class=\"form-venta\">");
        sb.append("<label>Cédula del cliente<input name=\"cedula\" maxlength=\"10\" value=\"").append(e(cedula)).append("\" required></label>")
                .append("<label>Nombre (solo si es cliente nuevo)<input name=\"nombre\" value=\"").append(e(nombre))
                .append("\"></label><label>Celular<select name=\"cod\">");
        for (Telefono t : telefonos) {
            if (t.isDisponible()) {
                sb.append("<option value=\"").append(t.getCodTelefono()).append("\"")
                        .append(t.getCodTelefono() == codElegido ? " selected" : "").append('>').append(e(t.getMarca())).append(' ')
                        .append(e(t.getModelo())).append(" · ").append(Formato.dinero(t.getPrecio())).append("</option>");
            }
        }
        sb.append("</select></label><span class=\"etiqueta\">Forma de pago</span><div class=\"toggle\">")
                .append("<input type=\"radio\" id=\"fe\" name=\"forma\" value=\"efectivo\"").append(credito ? "" : " checked")
                .append("><label for=\"fe\">Efectivo · −42 %</label>")
                .append("<input type=\"radio\" id=\"fc\" name=\"forma\" value=\"credito\"").append(credito ? " checked" : "")
                .append("><label for=\"fc\">Crédito</label></div>")
                .append("<label>Plazo del crédito (meses)<input type=\"number\" name=\"plazo\" min=\"3\" max=\"18\" value=\"")
                .append(plazo).append("\"></label>")
                .append("<button class=\"dorado ancho\">Vender</button></form>");
        String factura = facturaHtml.isEmpty() ? "<p class=\"vacio\">La factura aparecerá aquí.</p>" : facturaHtml;
        String amort = amortHtml.isEmpty() ? "<p class=\"vacio\">La tabla de amortización aparecerá aquí.</p>" : amortHtml;
        return "<div class=\"dos\">" + tarjeta("Datos de la venta", sb.toString()) + "<div class=\"columna\">"
                + tarjeta("Factura", factura) + tarjeta("Tabla de amortización", amort) + "</div></div>";
    }

    public static String factura(Factura f) {
        return "<div class=\"factura\"><div class=\"cab\"><strong>Factura N.º " + f.getNumFactura() + "</strong><span class=\"chip "
                + ("CREDITO".equalsIgnoreCase(f.getFormaPago()) ? "az" : "ok") + "\">" + e(f.getFormaPago()) + "</span></div>"
                + "<small>" + e(f.getFecha()) + "</small><hr>"
                + "<p>" + e(f.getNombreCliente()) + " (" + e(f.getCedula()) + ")<br>" + e(f.getTelefono())
                + (f.getNumCuotas() > 0 ? " · " + f.getNumCuotas() + " cuotas (crédito " + f.getCodCredito() + ")" : "") + "</p>"
                + "<div class=\"par\"><span>Precio de lista</span><span>" + Formato.dinero(f.getPrecioLista()) + "</span></div>"
                + "<div class=\"par\"><span>Descuento</span><span>− " + Formato.dinero(f.getDescuento()) + "</span></div>"
                + "<div class=\"par total\"><span>TOTAL</span><span>" + Formato.dinero(f.getTotal()) + "</span></div></div>";
    }

    public static String amortizacion(List<CuotaAmortizacion> filas) {
        if (filas.isEmpty()) {
            return "<p class=\"vacio\">No existe ese crédito.</p>";
        }
        StringBuilder sb = new StringBuilder("<div class=\"tabla-caja\"><table><thead><tr><th>Cuota</th><th>Fecha</th>")
                .append("<th class=\"der\">Cuota $</th><th class=\"der\">Interés</th><th class=\"der\">Capital</th><th class=\"der\">Saldo</th></tr></thead><tbody>");
        for (CuotaAmortizacion c : filas) {
            sb.append("<tr><td>").append(c.getNumCuota()).append("</td><td>").append(e(c.getFechaPago())).append("</td>")
                    .append(num(c.getValorCuota())).append(num(c.getInteresPagado())).append(num(c.getCapitalPagado()))
                    .append(num(c.getSaldo())).append("</tr>");
        }
        return sb.append("</tbody></table></div>").toString();
    }

    private static String num(double v) {
        return "<td class=\"der\">" + String.format(Locale.US, "%,.2f", v) + "</td>";
    }

    // ---------- Credito ----------

    public static String credito(String cedula, EvaluacionCredito ev, DetalleMontoMaximo d) {
        StringBuilder sb = new StringBuilder(tarjeta(null, "<form method=\"post\" class=\"fila\"><label>Cédula del cliente"
                + "<input name=\"cedula\" placeholder=\"Cédula\" maxlength=\"10\" value=\"" + e(cedula) + "\" required></label>"
                + "<button class=\"primario\">Verificar sujeto de crédito</button></form>"));
        if (ev == null) {
            return sb.toString();
        }
        StringBuilder r = new StringBuilder("<div class=\"veredicto ").append(ev.isSujetoCredito() ? "ok" : "mal").append("\"><strong>")
                .append(ev.isSujetoCredito() ? "Sí es sujeto de crédito" : "No es sujeto de crédito").append("</strong><p>")
                .append(e(ev.getMotivo())).append("</p></div>");
        if (d != null) {
            r.append("<div class=\"cifras\">").append(cifra("Cliente", e(d.getNombre()), ""))
                    .append(cifra("Promedio de depósitos (3 meses)", Formato.dinero(d.getPromedioDepositos()), ""))
                    .append(cifra("Promedio de retiros (3 meses)", Formato.dinero(d.getPromedioRetiros()), ""))
                    .append(cifra("Monto máximo de crédito", Formato.dinero(d.getMontoMaximo()), "oro")).append("</div>");
        }
        sb.append(tarjeta("Resultado", r.toString()));
        return sb.toString();
    }

    private static String cifra(String titulo, String valor, String clase) {
        return "<div class=\"cifra " + clase + "\"><small>" + e(titulo) + "</small><strong>" + valor + "</strong></div>";
    }

    // ---------- Facturas ----------

    public static String facturas(String ruta, String cedula, List<Factura> lista, boolean buscado, String amortHtml) {
        StringBuilder sb = new StringBuilder(tarjeta(null, "<form method=\"post\" class=\"fila\"><label>Cédula del cliente"
                + "<input name=\"cedula\" placeholder=\"Cédula\" maxlength=\"10\" value=\"" + e(cedula) + "\" required></label>"
                + "<button class=\"primario\">Buscar facturas</button></form>"
                + "<p class=\"ayuda\">Elija una factura a crédito para ver su tabla de amortización.</p>"));
        if (!buscado) {
            return sb.toString();
        }
        StringBuilder t = new StringBuilder();
        if (lista.isEmpty()) {
            t.append("<p class=\"vacio\">El cliente no tiene facturas.</p>");
        } else {
            t.append("<div class=\"tabla-caja\"><table><thead><tr><th>N.º</th><th>Fecha</th><th>Celular</th><th>Pago</th>")
                    .append("<th class=\"der\">Total</th><th>Crédito</th></tr></thead><tbody>");
            for (Factura f : lista) {
                t.append("<tr><td>").append(f.getNumFactura()).append("</td><td>").append(e(f.getFecha())).append("</td><td>")
                        .append(e(f.getTelefono())).append("</td><td><span class=\"chip ")
                        .append("CREDITO".equalsIgnoreCase(f.getFormaPago()) ? "az" : "ok").append("\">").append(e(f.getFormaPago()))
                        .append("</span></td><td class=\"der\">").append(Formato.dinero(f.getTotal())).append("</td><td>");
                if (f.getCodCredito() > 0) {
                    t.append("<a class=\"boton sec\" href=\"").append(ruta).append("/facturas?cedula=").append(e(cedula))
                            .append("&amp;credito=").append(f.getCodCredito()).append("\">Ver cuotas (").append(f.getCodCredito()).append(")</a>");
                }
                t.append("</td></tr>");
            }
            t.append("</tbody></table></div>");
        }
        sb.append(tarjeta("Compras", t.toString()));
        sb.append(tarjeta("Tabla de amortización", amortHtml.isEmpty() ? "<p class=\"vacio\">Elija una factura a crédito.</p>" : amortHtml));
        return sb.toString();
    }
}
