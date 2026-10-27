package ec.edu.monster.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import ec.edu.monster.cliente.modelos.ResultadoFactura;
import ec.edu.monster.cliente.modelos.Telefono;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Prueba el cliente contra un servidor HTTP falso que devuelve XML tal como lo hace JAX-WS. */
class ComercializadoraSoapTest {

    private HttpServer servidor;
    private String ultimaPeticion;

    private ComercializadoraSoap conRespuesta(int estado, String cuerpoXml) throws Exception {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", ex -> {
            ultimaPeticion = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] cuerpo = cuerpoXml.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            ex.sendResponseHeaders(estado, cuerpo.length);
            ex.getResponseBody().write(cuerpo);
            ex.close();
        });
        servidor.start();
        return new ComercializadoraSoap("http://localhost:" + servidor.getAddress().getPort() + "/ws");
    }

    private static String sobre(String operacion, String interior) {
        return "<?xml version=\"1.0\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<ns2:" + operacion + "Response xmlns:ns2=\"http://WebServices.comercializadora.edu.ec/\">" + interior
                + "</ns2:" + operacion + "Response></S:Body></S:Envelope>";
    }

    @AfterEach
    void detener() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void leeLaListaDeTelefonos() throws Exception {
        ComercializadoraSoap ws = conRespuesta(200, sobre("listarTelefonos",
                "<return><codTelefono>1</codTelefono><disponible>true</disponible><marca>Samsung</marca>"
                        + "<modelo>A15</modelo><precio>189.9</precio></return>"
                        + "<return><codTelefono>2</codTelefono><disponible>false</disponible><marca>Apple</marca>"
                        + "<modelo>iPhone</modelo><precio>899.0</precio></return>"));
        List<Telefono> t = ws.listarTelefonos();
        assertEquals(2, t.size());
        assertEquals("Samsung", t.get(0).getMarca());
        assertEquals(189.9, t.get(0).getPrecio(), 0.0);
        assertFalse(t.get(1).isDisponible());
        assertTrue(ultimaPeticion.contains("<ns:listarTelefonos"));
    }

    @Test
    void telefonoInexistenteDevuelveNull() throws Exception {
        assertNull(conRespuesta(200, sobre("buscarTelefono", "")).buscarTelefono(99));
    }

    @Test
    void escapaLosCaracteresEspecialesAlEnviar() throws Exception {
        ComercializadoraSoap ws = conRespuesta(200, sobre("crearTelefono", "<return>5</return>"));
        assertEquals(5, ws.crearTelefono("A&B", "<x>", 10));
        assertTrue(ultimaPeticion.contains("A&amp;B"));
        assertTrue(ultimaPeticion.contains("&lt;x&gt;"));
    }

    @Test
    void leeUnaFacturaDeCredito() throws Exception {
        ComercializadoraSoap ws = conRespuesta(200, sobre("facturarCredito", "<return><exito>true</exito>"
                + "<factura><cedula>1723456702</cedula><codCredito>2</codCredito><codTelefono>1</codTelefono>"
                + "<descuento>0.0</descuento><fecha>2026-06-15 10:00:00</fecha><formaPago>CREDITO</formaPago>"
                + "<nombreCliente>Daniela</nombreCliente><numCuotas>6</numCuotas><numFactura>2</numFactura>"
                + "<precioLista>189.9</precioLista><telefono>Samsung A15</telefono><total>189.9</total></factura>"
                + "<mensaje>Factura emitida.</mensaje></return>"));
        ResultadoFactura r = ws.facturarCredito("1723456702", "Daniela", 1, 6);
        assertTrue(r.isExito());
        assertNotNull(r.getFactura());
        assertEquals(2, r.getFactura().getCodCredito());
        assertEquals(6, r.getFactura().getNumCuotas());
        assertEquals("Samsung A15", r.getFactura().getTelefono());
    }

    @Test
    void ventaRechazadaNoTraeFactura() throws Exception {
        ComercializadoraSoap ws = conRespuesta(200, sobre("facturarCredito",
                "<return><exito>false</exito><mensaje>Credito rechazado.</mensaje></return>"));
        ResultadoFactura r = ws.facturarCredito("1", "x", 1, 6);
        assertFalse(r.isExito());
        assertNull(r.getFactura());
        assertEquals("Credito rechazado.", r.getMensaje());
    }

    @Test
    void leeLaTablaDeAmortizacion() throws Exception {
        ComercializadoraSoap ws = conRespuesta(200, sobre("obtenerAmortizacion",
                "<return><capitalPagado>0.0</capitalPagado><fechaPago>2026-06-15</fechaPago><interesPagado>0.0</interesPagado>"
                        + "<numCuota>0</numCuota><saldo>450.0</saldo><valorCuota>0.0</valorCuota></return>"
                        + "<return><capitalPagado>72.46</capitalPagado><fechaPago>2026-07-15</fechaPago>"
                        + "<interesPagado>6.19</interesPagado><numCuota>1</numCuota><saldo>377.54</saldo>"
                        + "<valorCuota>78.65</valorCuota></return>"));
        List<CuotaAmortizacion> t = ws.obtenerAmortizacion(1);
        assertEquals(2, t.size());
        assertEquals(377.54, t.get(1).getSaldo(), 0.0);
        assertTrue(Formato.tablaTexto(t).contains("78.65"));
    }

    @Test
    void unFaultSeConvierteEnMensajeLegible() throws Exception {
        ComercializadoraSoap ws = conRespuesta(500, "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<S:Body><S:Fault><faultcode>S:Server</faultcode><faultstring>Boom</faultstring></S:Fault></S:Body></S:Envelope>");
        ComercializadoraSoap.ServicioNoDisponible e = assertThrows(ComercializadoraSoap.ServicioNoDisponible.class,
                ws::listarTelefonos);
        assertTrue(e.getMessage().contains("Boom"));
    }

    @Test
    void servicioApagadoDaMensajeClaro() {
        ComercializadoraSoap ws = new ComercializadoraSoap("http://localhost:1/ws");
        ComercializadoraSoap.ServicioNoDisponible e = assertThrows(ComercializadoraSoap.ServicioNoDisponible.class,
                ws::listarTelefonos);
        assertTrue(e.getMessage().startsWith("No se pudo conectar"));
    }
}
