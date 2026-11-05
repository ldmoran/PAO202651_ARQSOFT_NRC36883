package ec.edu.monster.eurekabank.movil.modelo;

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
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cliente SOAP de WSEurekaBank, comun a los clientes. Usa solo HttpURLConnection y DOM, por eso el mismo codigo funciona
 * en consola, escritorio, web y Android (donde se copia tal cual). Sirve con el servidor Java y con el .NET: los dos
 * publican el mismo contrato, solo cambia la URL.
 */
public class EurekaSoap {

    public static final String URL_POR_DEFECTO = "http://localhost:8080/WS_EurekaBank_SOAPJAVA_GR07/WSEurekaBank";
    private static final String NS = "http://ws.eurekabank.monster.edu.ec/";

    /** El servicio no responde o respondio con un error; el mensaje esta listo para mostrarse al usuario. */
    public static class ServicioNoDisponible extends RuntimeException {
        public ServicioNoDisponible(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    public static class Cuenta {
        public final String codigo;
        public final String nombreCliente;
        public final double saldo;
        public final String estado;
        public final String moneda;
        public final String email;
        public final String telefono;

        public Cuenta(String codigo, String nombreCliente, double saldo, String estado, String moneda, String email, String telefono) {
            this.codigo = codigo;
            this.nombreCliente = nombreCliente;
            this.saldo = saldo;
            this.estado = estado;
            this.moneda = moneda;
            this.email = email;
            this.telefono = telefono;
        }
    }

    public static class Movimiento {
        public final String codigoCuenta;
        public final int numero;
        public final String fecha;
        public final String tipo;
        public final double importe;
        public final String referencia;
        public final double saldoActual;
        public final String nombreCliente;

        public Movimiento(String codigoCuenta, int numero, String fecha, String tipo, double importe, String referencia,
                double saldoActual, String nombreCliente) {
            this.codigoCuenta = codigoCuenta;
            this.numero = numero;
            this.fecha = fecha;
            this.tipo = tipo;
            this.importe = importe;
            this.referencia = referencia;
            this.saldoActual = saldoActual;
            this.nombreCliente = nombreCliente;
        }

        public boolean esEntrada() {
            return tipo.toLowerCase(Locale.ROOT).endsWith("entrada");
        }
    }

    /** Resultado de registrar un movimiento: ok = codigo 1. */
    public static class Resultado {
        public final boolean ok;
        public final String mensaje;

        public Resultado(boolean ok, String mensaje) {
            this.ok = ok;
            this.mensaje = mensaje;
        }
    }

    private final String url;

    public EurekaSoap(String url) {
        this.url = url;
    }

    public String url() {
        return url;
    }

    /** Dinero con dos decimales y separador de miles, igual en todos los clientes. */
    public static String dinero(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }

    // ---------- operaciones ----------

    public boolean iniciarSesion(String usuario, String clave) {
        Element r = llamar("iniciarSesion", "<usuario>" + esc(usuario) + "</usuario><contrasena>" + esc(clave) + "</contrasena>");
        Element res = primero(r, "resultado");
        return res != null && "true".equalsIgnoreCase(res.getTextContent().trim());
    }

    public List<Cuenta> cuentas() {
        List<Cuenta> lista = new ArrayList<>();
        for (Element e : registros(llamar("traerCuentasConClientes", ""), "Codigo")) {
            lista.add(new Cuenta(texto(e, "Codigo"), texto(e, "NombreCliente"), decimal(texto(e, "Saldo")), texto(e, "Estado"),
                    texto(e, "Moneda"), texto(e, "EmailCliente"), texto(e, "TelefonoCliente")));
        }
        return lista;
    }

    /** Movimientos de una cuenta; si cuenta esta vacia, de todas. */
    public List<Movimiento> movimientos(String cuenta) {
        List<Movimiento> lista = new ArrayList<>();
        for (Element e : registros(llamar("traerMovimientos", "<cuenta>" + esc(cuenta == null ? "" : cuenta) + "</cuenta>"), "CodigoCuenta")) {
            lista.add(new Movimiento(texto(e, "CodigoCuenta"), (int) decimal(texto(e, "Numero")), texto(e, "Fecha"), texto(e, "Tipo"),
                    decimal(texto(e, "Importe")), texto(e, "Referencia"), decimal(texto(e, "SaldoActual")), texto(e, "NombreCliente")));
        }
        return lista;
    }

    public List<String> tipos() {
        List<String> lista = new ArrayList<>();
        Element r = llamar("tipoMovimientosPermitidos", "");
        NodeList todos = r.getElementsByTagName("*");
        for (int i = 0; i < todos.getLength(); i++) {
            Element e = (Element) todos.item(i);
            if (!tieneHijos(e) && !e.getTextContent().trim().isEmpty()) {
                lista.add(e.getTextContent().trim());
            }
        }
        return lista;
    }

    public Resultado registrar(String tipo, String origen, String destino, double importe) {
        Element r = llamar("regMovimiento", "<cuentaOrigen>" + esc(origen) + "</cuentaOrigen><cuentaDestino>"
                + esc(destino == null ? "" : destino) + "</cuentaDestino><tipo>" + esc(tipo) + "</tipo><importe>"
                + String.format(Locale.US, "%.2f", importe) + "</importe>");
        Element codigo = primero(r, "codigo");
        Element mensaje = primero(r, "mensaje");
        return new Resultado(codigo != null && "1".equals(codigo.getTextContent().trim()),
                mensaje == null ? "" : mensaje.getTextContent().trim());
    }

    // ---------- transporte y XML ----------

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
            cn.setRequestProperty("SOAPAction", "\"" + NS + "WSEurekaBank/" + operacion + "\"");
            try (OutputStream os = cn.getOutputStream()) {
                os.write(sobre.getBytes(StandardCharsets.UTF_8));
            }
            int codigo = cn.getResponseCode();
            InputStream in = codigo >= 400 ? cn.getErrorStream() : cn.getInputStream();
            Document doc = parsear(in == null ? new byte[0] : leerTodo(in));
            NodeList fallos = doc.getElementsByTagNameNS("*", "faultstring");
            if (fallos.getLength() > 0) {
                throw new ServicioNoDisponible("El servicio respondio con un error: " + fallos.item(0).getTextContent(), null);
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

    private static String nombre(Node n) {
        return n.getLocalName() != null ? n.getLocalName() : n.getNodeName();
    }

    private static boolean tieneHijos(Element e) {
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
                return true;
            }
        }
        return false;
    }

    /** Primer elemento descendiente con ese nombre local. */
    private static Element primero(Element raiz, String nombre) {
        NodeList todos = raiz.getElementsByTagName("*");
        for (int i = 0; i < todos.getLength(); i++) {
            if (nombre.equals(nombre(todos.item(i)))) {
                return (Element) todos.item(i);
            }
        }
        return null;
    }

    /** Registros: los elementos que tienen un hijo con el campo clave (el contenedor cambia segun el servidor). */
    private static List<Element> registros(Element raiz, String campoClave) {
        List<Element> lista = new ArrayList<>();
        NodeList todos = raiz.getElementsByTagName("*");
        for (int i = 0; i < todos.getLength(); i++) {
            Element e = (Element) todos.item(i);
            for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n instanceof Element && campoClave.equals(nombre(n))) {
                    lista.add(e);
                    break;
                }
            }
        }
        return lista;
    }

    private static String texto(Element padre, String nombre) {
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && nombre.equals(nombre(n))) {
                return n.getTextContent().trim();
            }
        }
        return "";
    }

    private static double decimal(String s) {
        try {
            return s.isEmpty() ? 0 : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String esc(String s) {
        return (s == null ? "" : s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
