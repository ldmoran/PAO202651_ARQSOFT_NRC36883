package ec.edu.monster.clienteweb.modelo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cliente SOAP del servicio "Conversion de Unidades Monster". Arma el mensaje
 * XML (contrato WSDL del servidor), lo envia por HTTP y lee la respuesta
 * estructurada. No contiene logica de conversion: todo el calculo ocurre en el
 * servidor.
 */
public class SoapClient {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/WS_ConUni_Java_GR07/";
    private static final String NS = "http://WebServices.monster.edu.ec/";

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public SoapClient(String baseUrl) {
        String u = (baseUrl == null || baseUrl.isBlank()) ? URL_POR_DEFECTO : baseUrl.trim();
        this.baseUrl = u.endsWith("/") ? u : u + "/";
    }

    /** URL base: propiedad -Dconuni.url, variable CONUNI_URL o la URL por defecto. */
    public static String urlConfigurada() {
        String p = System.getProperty("conuni.url");
        if (p == null || p.isBlank()) {
            p = System.getenv("CONUNI_URL");
        }
        return (p == null || p.isBlank()) ? URL_POR_DEFECTO : p;
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
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + servicio))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8)).build();
        try {
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            return interpretar(resp.body(), resp.statusCode());
        } catch (IOException e) {
            return RespuestaServicio.errorLocal(RespuestaServicio.COD_RED,
                    "No se pudo conectar con el servidor (" + baseUrl + "). Verifique que esté en ejecución.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return RespuestaServicio.errorLocal(RespuestaServicio.COD_RED, "Operación interrumpida");
        } catch (RuntimeException e) {
            return RespuestaServicio.errorLocal("SRV-500", "Respuesta inesperada del servidor");
        }
    }

    /** Convierte el XML de respuesta en un {@link RespuestaServicio}. */
    static RespuestaServicio interpretar(byte[] xml, int estadoHttp) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
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
                    case "estado" -> r.setEstado(valor);
                    case "codigo" -> r.setCodigo(valor);
                    case "mensaje" -> r.setMensaje(valor);
                    case "resultado" -> r.setResultado(Double.valueOf(valor));
                    case "unidadOrigen" -> r.setUnidadOrigen(valor);
                    case "unidadDestino" -> r.setUnidadDestino(valor);
                    case "token" -> r.setToken(valor);
                    case "datos" -> r.getDatos().add(valor);
                    default -> { }
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
