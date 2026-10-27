package ec.edu.monster.cliente;

import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.Factura;
import ec.edu.monster.cliente.modelos.ResultadoFactura;
import ec.edu.monster.cliente.modelos.Telefono;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cliente SOAP de WSComercializadora, comun a los cuatro clientes. Usa solo HttpURLConnection y DOM,
 * por eso el mismo codigo funciona en consola, escritorio, web y Android (donde se copia tal cual).
 */
public class ComercializadoraSoap {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/Comercializadora/WSComercializadora";
    private static final String NS = "http://WebServices.comercializadora.edu.ec/";

    /** El servicio no responde o respondio con un error; el mensaje esta listo para mostrarse al usuario. */
    public static class ServicioNoDisponible extends RuntimeException {
        public ServicioNoDisponible(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    private final String url;

    public ComercializadoraSoap(String url) {
        this.url = url;
    }

    // ---------- Catalogo ----------

    public List<Telefono> listarTelefonos() {
        List<Telefono> lista = new ArrayList<>();
        for (Element e : retornos(llamar("listarTelefonos", ""))) {
            lista.add(telefono(e));
        }
        return lista;
    }

    /** Devuelve null si el telefono no existe. */
    public Telefono buscarTelefono(int cod) {
        List<Element> r = retornos(llamar("buscarTelefono", "<codTelefono>" + cod + "</codTelefono>"));
        return r.isEmpty() ? null : telefono(r.get(0));
    }

    /** Devuelve el codigo nuevo, o 0 si los datos no son validos. */
    public int crearTelefono(String marca, String modelo, double precio) {
        Element r = llamar("crearTelefono", "<marca>" + esc(marca) + "</marca><modelo>" + esc(modelo)
                + "</modelo><precio>" + precio + "</precio>");
        return entero(valorDeRetorno(r));
    }

    public boolean actualizarTelefono(int cod, String marca, String modelo, double precio, boolean disponible) {
        Element r = llamar("actualizarTelefono", "<codTelefono>" + cod + "</codTelefono><marca>" + esc(marca)
                + "</marca><modelo>" + esc(modelo) + "</modelo><precio>" + precio + "</precio><disponible>"
                + disponible + "</disponible>");
        return Boolean.parseBoolean(valorDeRetorno(r));
    }

    public boolean eliminarTelefono(int cod) {
        return Boolean.parseBoolean(valorDeRetorno(llamar("eliminarTelefono", "<codTelefono>" + cod + "</codTelefono>")));
    }

    // ---------- Credito ----------

    public EvaluacionCredito verificarSujetoCredito(String cedula) {
        Element r = retorno(llamar("verificarSujetoCredito", "<cedula>" + esc(cedula) + "</cedula>"));
        EvaluacionCredito e = new EvaluacionCredito();
        e.setSujetoCredito(Boolean.parseBoolean(texto(r, "sujetoCredito")));
        e.setCodCliente(entero(texto(r, "codCliente")));
        e.setNombre(texto(r, "nombre"));
        e.setMotivo(texto(r, "motivo"));
        return e;
    }

    public DetalleMontoMaximo calcularMontoMaximo(String cedula) {
        Element r = retorno(llamar("calcularMontoMaximo", "<cedula>" + esc(cedula) + "</cedula>"));
        DetalleMontoMaximo d = new DetalleMontoMaximo();
        d.setCodCliente(entero(texto(r, "codCliente")));
        d.setNombre(texto(r, "nombre"));
        d.setPromedioDepositos(decimal(texto(r, "promedioDepositos")));
        d.setPromedioRetiros(decimal(texto(r, "promedioRetiros")));
        d.setMontoMaximo(decimal(texto(r, "montoMaximo")));
        d.setMensaje(texto(r, "mensaje"));
        return d;
    }

    public List<CuotaAmortizacion> obtenerAmortizacion(int codCredito) {
        List<CuotaAmortizacion> lista = new ArrayList<>();
        for (Element f : retornos(llamar("obtenerAmortizacion", "<codCredito>" + codCredito + "</codCredito>"))) {
            lista.add(new CuotaAmortizacion(entero(texto(f, "numCuota")), texto(f, "fechaPago"),
                    decimal(texto(f, "valorCuota")), decimal(texto(f, "interesPagado")),
                    decimal(texto(f, "capitalPagado")), decimal(texto(f, "saldo"))));
        }
        return lista;
    }

    // ---------- Facturacion ----------

    public ResultadoFactura facturarEfectivo(String cedula, String nombre, int codTelefono) {
        return resultadoFactura(llamar("facturarEfectivo", "<cedula>" + esc(cedula) + "</cedula><nombre>" + esc(nombre)
                + "</nombre><codTelefono>" + codTelefono + "</codTelefono>"));
    }

    public ResultadoFactura facturarCredito(String cedula, String nombre, int codTelefono, int plazoMeses) {
        return resultadoFactura(llamar("facturarCredito", "<cedula>" + esc(cedula) + "</cedula><nombre>" + esc(nombre)
                + "</nombre><codTelefono>" + codTelefono + "</codTelefono><plazoMeses>" + plazoMeses + "</plazoMeses>"));
    }

    public List<Factura> consultarFacturas(String cedula) {
        List<Factura> lista = new ArrayList<>();
        for (Element f : retornos(llamar("consultarFacturas", "<cedula>" + esc(cedula) + "</cedula>"))) {
            lista.add(factura(f));
        }
        return lista;
    }

    // ---------- Conversion de respuestas ----------

    private static Telefono telefono(Element e) {
        return new Telefono(entero(texto(e, "codTelefono")), texto(e, "marca"), texto(e, "modelo"),
                decimal(texto(e, "precio")), Boolean.parseBoolean(texto(e, "disponible")));
    }

    private static Factura factura(Element e) {
        Factura f = new Factura();
        f.setNumFactura(entero(texto(e, "numFactura")));
        f.setFecha(texto(e, "fecha"));
        f.setCedula(texto(e, "cedula"));
        f.setNombreCliente(texto(e, "nombreCliente"));
        f.setCodTelefono(entero(texto(e, "codTelefono")));
        f.setTelefono(texto(e, "telefono"));
        f.setFormaPago(texto(e, "formaPago"));
        f.setPrecioLista(decimal(texto(e, "precioLista")));
        f.setDescuento(decimal(texto(e, "descuento")));
        f.setTotal(decimal(texto(e, "total")));
        f.setNumCuotas(entero(texto(e, "numCuotas")));
        f.setCodCredito(entero(texto(e, "codCredito")));
        return f;
    }

    private static ResultadoFactura resultadoFactura(Element respuesta) {
        Element r = retorno(respuesta);
        ResultadoFactura x = new ResultadoFactura();
        x.setExito(Boolean.parseBoolean(texto(r, "exito")));
        x.setMensaje(texto(r, "mensaje"));
        Element f = hijo(r, "factura");
        x.setFactura(f == null ? null : factura(f));
        return x;
    }

    // ---------- Transporte y XML ----------

    private Element llamar(String operacion, String parametros) {
        String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>"
                + "<ns:" + operacion + " xmlns:ns=\"" + NS + "\">" + parametros + "</ns:" + operacion + ">"
                + "</soapenv:Body></soapenv:Envelope>";
        HttpURLConnection cn = null;
        try {
            cn = (HttpURLConnection) new URL(url).openConnection();
            cn.setConnectTimeout(5000);
            cn.setReadTimeout(20000);
            cn.setRequestMethod("POST");
            cn.setDoOutput(true);
            cn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            cn.setRequestProperty("SOAPAction", "\"\"");
            try (OutputStream os = cn.getOutputStream()) {
                os.write(sobre.getBytes(StandardCharsets.UTF_8));
            }
            int codigo = cn.getResponseCode();
            InputStream in = codigo >= 400 ? cn.getErrorStream() : cn.getInputStream();
            byte[] cuerpo = in == null ? new byte[0] : leerTodo(in);
            Document doc = parsear(cuerpo);
            NodeList fallos = doc.getElementsByTagNameNS("*", "faultstring");
            if (fallos.getLength() > 0) {
                throw new ServicioNoDisponible("El servicio respondio con un error: "
                        + fallos.item(0).getTextContent(), null);
            }
            NodeList respuestas = doc.getElementsByTagNameNS(NS, operacion + "Response");
            if (respuestas.getLength() == 0) {
                throw new ServicioNoDisponible("Respuesta inesperada del servicio (HTTP " + codigo + ")", null);
            }
            return (Element) respuestas.item(0);
        } catch (IOException e) {
            throw new ServicioNoDisponible("No se pudo conectar con el servicio en " + url, e);
        } finally {
            if (cn != null) {
                cn.disconnect();
            }
        }
    }

    private static byte[] leerTodo(InputStream in) throws IOException {
        try (InputStream entrada = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = entrada.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static Document parsear(byte[] xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new ServicioNoDisponible("La respuesta del servicio no es XML valido", e);
        }
    }

    /** Elementos &lt;return&gt; hijos directos de la respuesta (una lista puede traer varios). */
    private static List<Element> retornos(Element respuesta) {
        List<Element> lista = new ArrayList<>();
        for (Node n = respuesta.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && "return".equals(nombre(n))) {
                lista.add((Element) n);
            }
        }
        return lista;
    }

    private static Element retorno(Element respuesta) {
        List<Element> r = retornos(respuesta);
        if (r.isEmpty()) {
            throw new ServicioNoDisponible("El servicio no devolvio datos", null);
        }
        return r.get(0);
    }

    private static String valorDeRetorno(Element respuesta) {
        List<Element> r = retornos(respuesta);
        return r.isEmpty() ? "" : r.get(0).getTextContent().trim();
    }

    private static String nombre(Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }

    private static Element hijo(Element padre, String nombre) {
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && nombre.equals(nombre(n))) {
                return (Element) n;
            }
        }
        return null;
    }

    private static String texto(Element padre, String nombre) {
        Element h = hijo(padre, nombre);
        return h == null ? "" : h.getTextContent().trim();
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
