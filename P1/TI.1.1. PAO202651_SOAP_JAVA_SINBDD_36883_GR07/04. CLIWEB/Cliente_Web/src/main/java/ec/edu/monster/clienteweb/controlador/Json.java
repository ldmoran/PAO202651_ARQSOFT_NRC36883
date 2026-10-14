package ec.edu.monster.clienteweb.controlador;

import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Genera el JSON que consume el JavaScript del navegador. El token nunca se incluye. */
public final class Json {

    private Json() {
    }

    public static String escapar(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String texto(String nombre, String valor) {
        return "\"" + nombre + "\":" + (valor == null ? "null" : "\"" + escapar(valor) + "\"");
    }

    /** Respuesta del servicio como JSON (sin token). */
    public static String de(RespuestaServicio r) {
        StringBuilder sb = new StringBuilder("{");
        sb.append(texto("estado", r.getEstado())).append(',');
        sb.append(texto("codigo", r.getCodigo())).append(',');
        sb.append(texto("mensaje", r.getMensaje())).append(',');
        sb.append("\"resultado\":").append(r.getResultado() == null ? "null" : Formato.numero(r.getResultado())).append(',');
        sb.append("\"resultadoTexto\":")
                .append(r.getResultado() == null ? "null" : "\"" + Formato.numero(r.getResultado()) + "\"").append(',');
        sb.append(texto("unidadOrigen", r.getUnidadOrigen())).append(',');
        sb.append(texto("unidadDestino", r.getUnidadDestino())).append(',');
        sb.append("\"datos\":[");
        List<String> datos = r.getDatos();
        for (int i = 0; i < datos.size(); i++) {
            sb.append(i == 0 ? "" : ",").append('"').append(escapar(datos.get(i))).append('"');
        }
        sb.append("]}");
        return sb.toString();
    }

    public static void escribir(HttpServletResponse resp, int estadoHttp, String json) throws IOException {
        resp.setStatus(estadoHttp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(json);
    }
}
