package ec.edu.monster.clientemovil.modelo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cliente SOAP del servicio "Conversion de Unidades Monster" para Android.
 * Arma el mensaje XML (contrato WSDL del servidor), lo envia por HTTP con
 * HttpURLConnection y lee la respuesta estructurada. No contiene logica de
 * conversion: todo el calculo ocurre en el servidor.
 *
 * Nota: en el emulador de Android, el equipo anfitrion se ve como 10.0.2.2.
 */
public class SoapClient {

    /** URL por defecto para el emulador (10.0.2.2 = localhost del equipo). */
    public static final String URL_POR_DEFECTO = "http://10.0.2.2:8080/WS_ConUni_Java_GR07/";
    private static final String NS = "http://WebServices.monster.edu.ec/";

    private final String baseUrl;

    public SoapClient(String baseUrl) {
        String u = (baseUrl == null || baseUrl.trim().isEmpty()) ? URL_POR_DEFECTO : baseUrl.trim();
        this.baseUrl = u.endsWith("/") ? u : u + "/";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    // ---------------- operaciones ----------------

    public RespuestaServicio login(String usuario, String clave) {
        return llamar("WSLogin", "login", "usuario", usuario, "clave", clave);
    }

    public RespuestaServicio obtenerCategorias(String token) {
        return llamar("WSCatalogo", "obtenerCategorias", "token", token);
    }

    public RespuestaServicio obtenerUnidades(String token, String categoria) {
        return llamar("WSCatalogo", "obtenerUnidades", "token", token, "categoria", categoria);
    }

    /** Invoca el servicio de la categoria elegida (Longitud, Masa o Temperatura). */
    public RespuestaServicio convertir(String categoria, double valor, String origen, String destino, String token) {
        String c = categoria == null ? "" : categoria.trim();
        String servicio;
        String operacion;
        if (c.equalsIgnoreCase("Longitud")) {
            servicio = "WSLongitud";
            operacion = "convertirLongitud";
        } else if (c.equalsIgnoreCase("Masa")) {
            servicio = "WSMasa";
            operacion = "convertirMasa";
        } else if (c.equalsIgnoreCase("Temperatura")) {
            servicio = "WSTemperatura";
            operacion = "convertirTemperatura";
        } else {
            return RespuestaServicio.errorLocal("DATA-001", "Categoría no soportada");
        }
        return llamar(servicio, operacion, "valor", Double.toString(valor), "unidadOrigen", origen,
                "unidadDestino", destino, "token", token);
    }

    // ---------------- SOAP ----------------

    private RespuestaServicio llamar(String servicio, String operacion, String... parametros) {
        StringBuilder cuerpo = new StringBuilder();
        for (int i = 0; i + 1 < parametros.length; i += 2) {
            cuerpo.append('<').append(parametros[i]).append('>')
                    .append(escapar(parametros[i + 1]))
                    .append("</").append(parametros[i]).append('>');
        }
        String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"" + NS + "\">"
                + "<soapenv:Header/><soapenv:Body><ws:" + operacion + ">" + cuerpo + "</ws:" + operacion + ">"
                + "</soapenv:Body></soapenv:Envelope>";
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) URI.create(baseUrl + servicio).toURL().openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(15000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            try (OutputStream os = con.getOutputStream()) {
                os.write(envelope.getBytes(StandardCharsets.UTF_8));
            }
            int http = con.getResponseCode();
            InputStream in = http >= 400 ? con.getErrorStream() : con.getInputStream();
            byte[] cuerpoResp = in == null ? new byte[0] : leer(in);
            return interpretar(cuerpoResp, http);
        } catch (IOException e) {
            return RespuestaServicio.errorLocal(RespuestaServicio.COD_RED,
                    "No se pudo conectar con el servidor (" + baseUrl + "). Verifique que esté en ejecución.");
        } catch (RuntimeException e) {
            return RespuestaServicio.errorLocal("SRV-500", "Respuesta inesperada del servidor");
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static byte[] leer(InputStream in) throws IOException {
        try (InputStream entrada = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = entrada.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    /** Convierte el XML de respuesta en un {@link RespuestaServicio}. */
    public static RespuestaServicio interpretar(byte[] xml, int estadoHttp) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc = b.parse(new ByteArrayInputStream(xml));

            NodeList fallos = doc.getElementsByTagNameNS("*", "Fault");
            if (fallos.getLength() > 0) {
                String texto = textoDe((Element) fallos.item(0), "faultstring");
                return RespuestaServicio.errorLocal("SRV-500",
                        "Error del servidor" + (texto == null ? "" : ": " + texto));
            }
            NodeList retornos = doc.getElementsByTagName("return");
            if (retornos.getLength() == 0) {
                return RespuestaServicio.errorLocal("SRV-500", "Respuesta sin contenido (HTTP " + estadoHttp + ")");
            }
            RespuestaServicio r = new RespuestaServicio();
            for (Node n = retornos.item(0).getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                String valor = n.getTextContent();
                switch (n.getNodeName()) {
                    case "estado": r.setEstado(valor); break;
                    case "codigo": r.setCodigo(valor); break;
                    case "mensaje": r.setMensaje(valor); break;
                    case "resultado": r.setResultado(Double.valueOf(valor)); break;
                    case "unidadOrigen": r.setUnidadOrigen(valor); break;
                    case "unidadDestino": r.setUnidadDestino(valor); break;
                    case "token": r.setToken(valor); break;
                    case "datos": r.getDatos().add(valor); break;
                    default: break;
                }
            }
            return r;
        } catch (Exception e) {
            return RespuestaServicio.errorLocal("SRV-500", "No se pudo leer la respuesta del servidor");
        }
    }

    private static String textoDe(Element padre, String nombre) {
        NodeList l = padre.getElementsByTagName(nombre);
        return l.getLength() > 0 ? l.item(0).getTextContent() : null;
    }

    private static String escapar(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
