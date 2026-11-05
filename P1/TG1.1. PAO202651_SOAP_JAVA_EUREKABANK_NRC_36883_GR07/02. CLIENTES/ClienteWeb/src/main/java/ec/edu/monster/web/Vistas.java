package ec.edu.monster.web;

import ec.edu.monster.cliente.EurekaSoap;
import ec.edu.monster.cliente.EurekaSoap.Cuenta;
import ec.edu.monster.cliente.EurekaSoap.Movimiento;
import java.util.List;

/** Arma el HTML de cada pagina. Todo texto que viene del usuario o del servicio pasa por {@link #e(String)}. */
public final class Vistas {

    private Vistas() {
    }

    public static String e(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String cabeza(String ruta, String titulo) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + e(titulo) + " - EurekaBank</title>"
                + "<link rel=\"stylesheet\" href=\"" + ruta + "/css/estilo.css\"></head><body>";
    }

    /** Pagina de inicio de sesion (mismo diseno que el resto de proyectos: foto de Monster, campos con icono y boton azul). */
    public static String login(String ruta, String mensaje, String servidor) {
        StringBuilder sb = new StringBuilder(cabeza(ruta, "Iniciar sesión"));
        sb.append("<main class=\"login\"><section class=\"login-tarjeta\">")
                .append("<img class=\"banner\" src=\"").append(ruta).append("/img/monster.jpg\" alt=\"Monster\">")
                .append("<div class=\"login-cuerpo\"><h1>EurekaBank</h1>")
                .append("<p>Cuentas · Movimientos · Operaciones. Ingrese sus credenciales para continuar.</p>");
        sb.append("<form method=\"post\" action=\"").append(ruta).append("/index\">")
                .append("<div class=\"campo-icono\"><span class=\"ico\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"12\" cy=\"8\" r=\"4\"/><path d=\"M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7\"/></svg></span>")
                .append("<input name=\"usuario\" placeholder=\"Usuario\" autocomplete=\"username\" required autofocus></div>")
                .append("<div class=\"campo-icono\"><span class=\"ico\"><svg viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><circle cx=\"8\" cy=\"15\" r=\"4\"/><path d=\"M11 12l9-9M16 7l3 3M14 9l2 2\"/></svg></span>")
                .append("<input name=\"clave\" type=\"password\" placeholder=\"Clave\" autocomplete=\"current-password\" required></div>")
                .append("<details><summary title=\"Servidor\"></summary><label>Servidor<input name=\"servidor\" value=\"")
                .append(e(servidor)).append("\"></label></details>")
                .append("<button class=\"entrar\">Ingresar</button></form>");
        sb.append("<div class=\"estado").append(mensaje != null && !mensaje.isEmpty() ? " error" : "").append("\">")
                .append(e(mensaje == null ? "" : mensaje)).append("</div>")
                .append("<small>Arquitectura de Software · ESPE · GR07</small></div></section></main></body></html>");
        return sb.toString();
    }

    public static String pagina(String ruta, String actual, String usuario, String mensaje, boolean error, String contenido) {
        StringBuilder sb = new StringBuilder(cabeza(ruta, actual));
        sb.append("<div class=\"app\"><aside class=\"menu\"><div class=\"marca\"><span class=\"icono\"><svg viewBox=\"0 0 24 24\" fill=\"none\" ")
                .append("stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M3 10l9-6 9 6M5 10v8M9 10v8M15 10v8M19 10v8M3 21h18\"/></svg></span>")
                .append("<div><strong>EurekaBank</strong><small>Banca en línea</small></div></div><nav>");
        for (String[] item : new String[][] {{"cuentas", "Cuentas"}, {"movimientos", "Movimientos"}, {"operar", "Operar"}}) {
            sb.append("<a href=\"").append(ruta).append('/').append(item[0]).append("\"")
                    .append(item[1].equals(actual) ? " class=\"activo\"" : "").append('>').append(item[1]).append("</a>");
        }
        sb.append("</nav><div class=\"pie\"><div class=\"usuario\">").append(e(usuario)).append("</div><a class=\"salir\" href=\"")
                .append(ruta).append("/salir\">Cerrar sesión</a></div></aside><section class=\"contenido\"><h2>").append(e(actual)).append("</h2>");
        if (mensaje != null && !mensaje.isEmpty()) {
            sb.append("<p class=\"aviso ").append(error ? "error" : "exito").append("\">").append(e(mensaje)).append("</p>");
        }
        sb.append(contenido).append("</section></div></body></html>");
        return sb.toString();
    }

    public static String cuentas(String ruta, List<Cuenta> lista) {
        StringBuilder sb = new StringBuilder("<div class=\"tarjetas\">");
        for (Cuenta c : lista) {
            sb.append("<article class=\"cuenta\"><header><span class=\"codigo\">").append(e(c.codigo))
                    .append("</span><span class=\"chip ok\">").append(e(c.estado)).append("</span></header><h3>")
                    .append(e(c.nombreCliente)).append("</h3><div class=\"saldo\">").append(e(c.moneda)).append(' ')
                    .append(EurekaSoap.dinero(c.saldo)).append("</div><small>").append(e(c.email)).append(" · ").append(e(c.telefono))
                    .append("</small><p><a class=\"boton sec\" href=\"").append(ruta).append("/movimientos?cuenta=").append(e(c.codigo))
                    .append("\">Ver movimientos</a></p></article>");
        }
        return sb.append("</div>").toString();
    }

    public static String movimientos(List<Cuenta> cuentas, String seleccionada, List<Movimiento> lista) {
        StringBuilder sb = new StringBuilder("<form method=\"get\" class=\"barra\"><select name=\"cuenta\"><option value=\"\">Todas las cuentas</option>");
        for (Cuenta c : cuentas) {
            sb.append("<option value=\"").append(e(c.codigo)).append("\"").append(c.codigo.equals(seleccionada) ? " selected" : "")
                    .append('>').append(e(c.codigo)).append(" — ").append(e(c.nombreCliente)).append("</option>");
        }
        sb.append("</select><button>Actualizar</button></form><div class=\"tabla-caja\"><table><thead><tr><th>Cuenta</th><th>N.º</th><th>Fecha</th>")
                .append("<th>Tipo</th><th class=\"der\">Importe</th><th class=\"der\">Saldo</th><th>Referencia</th></tr></thead><tbody>");
        for (Movimiento m : lista) {
            sb.append("<tr><td>").append(e(m.codigoCuenta)).append("</td><td>").append(m.numero).append("</td><td>").append(e(m.fecha))
                    .append("</td><td><span class=\"chip ").append(m.esEntrada() ? "ok" : "mal").append("\">").append(e(m.tipo))
                    .append("</span></td><td class=\"der ").append(m.esEntrada() ? "pos" : "neg").append("\">").append(m.esEntrada() ? "+" : "−")
                    .append(EurekaSoap.dinero(m.importe)).append("</td><td class=\"der\">").append(EurekaSoap.dinero(m.saldoActual))
                    .append("</td><td>").append(e(m.referencia)).append("</td></tr>");
        }
        return sb.append("</tbody></table></div>").toString();
    }

    public static String operar(String ruta, List<Cuenta> cuentas, String tipo, String origen, String destino) {
        StringBuilder sb = new StringBuilder("<form method=\"post\" action=\"" + ruta + "/operar\" class=\"formulario\">");
        sb.append("<label>Tipo de operación<select name=\"tipo\">");
        for (String[] t : new String[][] {{"DEPOSITO", "Depósito"}, {"RETIRO", "Retiro"}, {"TRANSFERENCIA", "Transferencia"}}) {
            sb.append("<option value=\"").append(t[0]).append("\"").append(t[0].equals(tipo) ? " selected" : "").append('>').append(t[1]).append("</option>");
        }
        sb.append("</select></label><label>Cuenta (origen)<select name=\"origen\">").append(opciones(cuentas, origen)).append("</select></label>")
                .append("<label>Cuenta destino (solo transferencias)<select name=\"destino\">").append(opciones(cuentas, destino)).append("</select></label>")
                .append("<label>Importe<input name=\"importe\" inputmode=\"decimal\" placeholder=\"0.00\" required></label>")
                .append("<button class=\"principal\">Registrar operación</button></form>");
        return sb.toString();
    }

    private static String opciones(List<Cuenta> cuentas, String elegida) {
        StringBuilder sb = new StringBuilder();
        for (Cuenta c : cuentas) {
            sb.append("<option value=\"").append(e(c.codigo)).append("\"").append(c.codigo.equals(elegida) ? " selected" : "").append('>')
                    .append(e(c.codigo)).append(" — ").append(e(c.nombreCliente)).append(" (").append(EurekaSoap.dinero(c.saldo)).append(")</option>");
        }
        return sb.toString();
    }
}
