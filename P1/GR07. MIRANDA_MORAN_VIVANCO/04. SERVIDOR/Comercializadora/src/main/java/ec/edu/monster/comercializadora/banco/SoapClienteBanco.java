package ec.edu.monster.comercializadora.banco;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cliente SOAP hacia WSCredito (otra aplicacion, otra base de datos). Arma el sobre SOAP a mano y
 * lee la respuesta con DOM: asi no depende de clases generadas ni de la version del servidor.
 */
public class SoapClienteBanco implements ClienteBanco {

    private static final String NS = "http://WebServices.banquito.edu.ec/";

    private final String url;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public SoapClienteBanco(String url) {
        this.url = url;
    }

    /** Lee la direccion de -Dcomercializadora.banco.url, COMERCIALIZADORA_BANCO_URL o comercializadora.properties. */
    public static SoapClienteBanco desdeConfiguracion() {
        String v = System.getProperty("comercializadora.banco.url");
        if (v == null || v.isBlank()) {
            v = System.getenv("COMERCIALIZADORA_BANCO_URL");
        }
        if (v == null || v.isBlank()) {
            Properties p = new Properties();
            try (InputStream in = SoapClienteBanco.class.getResourceAsStream("/comercializadora.properties")) {
                if (in != null) {
                    p.load(in);
                }
            } catch (IOException e) {
                // se usa el valor por defecto
            }
            v = p.getProperty("banco.url", "http://localhost:8080/BanQuitoCore/WSCredito");
        }
        return new SoapClienteBanco(v);
    }

    @Override
    public EvaluacionCredito verificarSujetoCredito(String cedula) {
        Element r = llamar("verificarSujetoCredito", "<cedula>" + esc(cedula) + "</cedula>");
        EvaluacionCredito e = new EvaluacionCredito();
        e.setSujetoCredito(Boolean.parseBoolean(texto(r, "sujetoCredito")));
        e.setCodCliente(entero(texto(r, "codCliente")));
        e.setNombre(texto(r, "nombre"));
        e.setMotivo(texto(r, "motivo"));
        return e;
    }

    @Override
    public DetalleMontoMaximo calcularMontoMaximo(String cedula) {
        Element r = llamar("calcularMontoMaximo", "<cedula>" + esc(cedula) + "</cedula>");
        DetalleMontoMaximo d = new DetalleMontoMaximo();
        d.setCodCliente(entero(texto(r, "codCliente")));
        d.setNombre(texto(r, "nombre"));
        d.setPromedioDepositos(decimal(texto(r, "promedioDepositos")));
        d.setPromedioRetiros(decimal(texto(r, "promedioRetiros")));
        d.setMontoMaximo(decimal(texto(r, "montoMaximo")));
        d.setMensaje(texto(r, "mensaje"));
        return d;
    }

    @Override
    public ResultadoCredito otorgarCredito(String cedula, double monto, int plazoMeses) {
        Element r = llamar("otorgarCredito", "<cedula>" + esc(cedula) + "</cedula><monto>" + monto
                + "</monto><plazoMeses>" + plazoMeses + "</plazoMeses>");
        ResultadoCredito x = new ResultadoCredito();
        x.setAprobado(Boolean.parseBoolean(texto(r, "aprobado")));
        x.setMensaje(texto(r, "mensaje"));
        x.setCodCredito(entero(texto(r, "codCredito")));
        x.setMontoMaximo(decimal(texto(r, "montoMaximo")));
        x.setMontoCredito(decimal(texto(r, "montoCredito")));
        x.setPlazoMeses(entero(texto(r, "plazoMeses")));
        x.setCuotaFija(decimal(texto(r, "cuotaFija")));
        return x;
    }

    @Override
    public List<CuotaAmortizacion> obtenerTablaAmortizacion(int codCredito) {
        Element r = llamar("obtenerTablaAmortizacion", "<codCredito>" + codCredito + "</codCredito>");
        List<CuotaAmortizacion> lista = new ArrayList<>();
        NodeList filas = r.getElementsByTagNameNS("*", "return");
        for (int i = 0; i < filas.getLength(); i++) {
            Element f = (Element) filas.item(i);
            lista.add(new CuotaAmortizacion(entero(texto(f, "numCuota")), texto(f, "fechaPago"),
                    decimal(texto(f, "valorCuota")), decimal(texto(f, "interesPagado")),
                    decimal(texto(f, "capitalPagado")), decimal(texto(f, "saldo"))));
        }
        return lista;
    }

    private Element llamar(String operacion, String parametros) {
        String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>"
                + "<ns:" + operacion + " xmlns:ns=\"" + NS + "\">" + parametros + "</ns:" + operacion + ">"
                + "</soapenv:Body></soapenv:Envelope>";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "text/xml; charset=utf-8").header("SOAPAction", "\"\"")
                    .POST(HttpRequest.BodyPublishers.ofString(sobre, StandardCharsets.UTF_8)).build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            Document doc = leer(resp.body());
            NodeList fallos = doc.getElementsByTagNameNS("*", "faultstring");
            if (fallos.getLength() > 0) {
                throw new BancoNoDisponible("El banco respondio con un error: " + fallos.item(0).getTextContent(), null);
            }
            NodeList respuestas = doc.getElementsByTagNameNS(NS, operacion + "Response");
            if (respuestas.getLength() == 0) {
                throw new BancoNoDisponible("Respuesta inesperada del banco (HTTP " + resp.statusCode() + ")", null);
            }
            return (Element) respuestas.item(0);
        } catch (IOException e) {
            throw new BancoNoDisponible("No se pudo conectar con el banco en " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BancoNoDisponible("Se interrumpio la llamada al banco", e);
        }
    }

    private static Document leer(byte[] xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder b = f.newDocumentBuilder();
            return b.parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new BancoNoDisponible("La respuesta del banco no es XML valido", e);
        }
    }

    /** Texto del primer hijo directo con ese nombre local (los DTO son planos). */
    private static String texto(Element padre, String nombre) {
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && nombre.equals(n.getLocalName() != null ? n.getLocalName() : n.getNodeName())) {
                return n.getTextContent().trim();
            }
        }
        NodeList todos = padre.getElementsByTagNameNS("*", nombre);
        return todos.getLength() > 0 ? todos.item(0).getTextContent().trim() : "";
    }

    private static int entero(String s) {
        return s.isEmpty() ? 0 : Integer.parseInt(s);
    }

    private static double decimal(String s) {
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private static String esc(String s) {
        return (s == null ? "" : s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
