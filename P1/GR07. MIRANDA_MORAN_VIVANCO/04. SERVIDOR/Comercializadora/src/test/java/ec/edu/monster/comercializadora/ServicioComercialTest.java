package ec.edu.monster.comercializadora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.comercializadora.banco.ClienteBanco;
import ec.edu.monster.comercializadora.banco.CuotaAmortizacion;
import ec.edu.monster.comercializadora.banco.DetalleMontoMaximo;
import ec.edu.monster.comercializadora.banco.EvaluacionCredito;
import ec.edu.monster.comercializadora.banco.ResultadoCredito;
import ec.edu.monster.comercializadora.db.FuenteDatos;
import ec.edu.monster.comercializadora.modelos.ResultadoFactura;
import ec.edu.monster.comercializadora.repositorios.ComercioRepositorio;
import ec.edu.monster.comercializadora.servicios.ServicioComercial;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioComercialTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneId.of("UTC"));

    /** Banco falso: aprueba hasta un monto maximo fijo y recuerda lo que le pidieron. */
    static class BancoFalso implements ClienteBanco {
        double maximo = 1000;
        boolean caido;
        int llamadasOtorgar;

        @Override
        public EvaluacionCredito verificarSujetoCredito(String cedula) {
            return new EvaluacionCredito(true, 1, "Cliente", "ok");
        }

        @Override
        public DetalleMontoMaximo calcularMontoMaximo(String cedula) {
            DetalleMontoMaximo d = new DetalleMontoMaximo();
            d.setMontoMaximo(maximo);
            return d;
        }

        @Override
        public ResultadoCredito otorgarCredito(String cedula, double monto, int plazoMeses) {
            llamadasOtorgar++;
            if (caido) {
                throw new BancoNoDisponible("sin conexion", null);
            }
            ResultadoCredito r = new ResultadoCredito();
            if (monto > maximo) {
                r.setMensaje("Credito rechazado: monto mayor al maximo.");
                return r;
            }
            r.setAprobado(true);
            r.setCodCredito(77);
            r.setMensaje("Credito aprobado.");
            return r;
        }

        @Override
        public List<CuotaAmortizacion> obtenerTablaAmortizacion(int codCredito) {
            return new ArrayList<>();
        }
    }

    private BancoFalso banco;
    private ServicioComercial servicio;

    @BeforeEach
    void preparar(org.junit.jupiter.api.TestInfo info) throws SQLException {
        String url = "jdbc:h2:mem:com_" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        FuenteDatos fuente = () -> DriverManager.getConnection(url, "sa", "");
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement()) {
            st.execute("CREATE TABLE CLIENTE (COD_CLIENTE INT AUTO_INCREMENT PRIMARY KEY, CEDULA VARCHAR(20) UNIQUE, NOMBRE VARCHAR(100))");
            st.execute("CREATE TABLE TELEFONO (COD_TELEFONO INT AUTO_INCREMENT PRIMARY KEY, MARCA VARCHAR(50), MODELO VARCHAR(100), "
                    + "PRECIO DECIMAL(10,2), DISPONIBLE BOOLEAN DEFAULT TRUE)");
            st.execute("CREATE TABLE FACTURA (NUM_FACTURA INT AUTO_INCREMENT PRIMARY KEY, FECHA TIMESTAMP, COD_CLIENTE INT, "
                    + "COD_TELEFONO INT, FORMA_PAGO VARCHAR(10), PRECIO_LISTA DECIMAL(10,2), DESCUENTO DECIMAL(10,2), "
                    + "TOTAL DECIMAL(10,2), NUM_CUOTAS INT NULL, COD_CREDITO INT NULL)");
            st.execute("CREATE TABLE DETALLE_FACTURA (NUM_FACTURA INT, NUM_LINEA INT, COD_TELEFONO INT, CANTIDAD INT, "
                    + "PRECIO_UNITARIO DECIMAL(10,2), PRIMARY KEY (NUM_FACTURA, NUM_LINEA))");
            st.execute("INSERT INTO TELEFONO (MARCA, MODELO, PRECIO) VALUES ('Samsung','A15',200.00)");
            st.execute("INSERT INTO TELEFONO (MARCA, MODELO, PRECIO) VALUES ('Apple','iPhone 15',899.00)");
            st.execute("INSERT INTO TELEFONO (MARCA, MODELO, PRECIO, DISPONIBLE) VALUES ('Viejo','Modelo',50.00, FALSE)");
        }
        banco = new BancoFalso();
        servicio = new ServicioComercial(new ComercioRepositorio(fuente), banco, RELOJ);
    }

    @Test
    void listaElCatalogo() {
        assertEquals(3, servicio.listarTelefonos().size());
    }

    @Test
    void crudDelCatalogo() {
        int cod = servicio.crearTelefono("Xiaomi", "Note 13", 229);
        assertTrue(cod > 0);
        assertEquals("Note 13", servicio.buscarTelefono(cod).getModelo());
        assertTrue(servicio.actualizarTelefono(cod, "Xiaomi", "Note 13 Pro", 249, true));
        assertEquals(249.0, servicio.buscarTelefono(cod).getPrecio(), 0.0);
        assertTrue(servicio.eliminarTelefono(cod));
        assertNull(servicio.buscarTelefono(cod));
    }

    @Test
    void noCreaTelefonoConDatosInvalidos() {
        assertEquals(0, servicio.crearTelefono("", "X", 10));
        assertEquals(0, servicio.crearTelefono("A", "X", 0));
    }

    @Test
    void efectivoAplicaDescuentoDel42PorCiento() {
        ResultadoFactura r = servicio.facturarEfectivo("1712345601", "Carlos", 2);
        assertTrue(r.isExito(), r.getMensaje());
        assertEquals(899.00, r.getFactura().getPrecioLista(), 0.0);
        assertEquals(377.58, r.getFactura().getDescuento(), 0.0);
        assertEquals(521.42, r.getFactura().getTotal(), 0.0);
        assertEquals("EFECTIVO", r.getFactura().getFormaPago());
        assertEquals(0, r.getFactura().getCodCredito());
        assertEquals(0, banco.llamadasOtorgar);
    }

    @Test
    void creditoSinDescuentoYConCodigoDeCreditoDelBanco() {
        ResultadoFactura r = servicio.facturarCredito("1712345601", "Carlos", 1, 6);
        assertTrue(r.isExito(), r.getMensaje());
        assertEquals(200.00, r.getFactura().getTotal(), 0.0);
        assertEquals(0.0, r.getFactura().getDescuento(), 0.0);
        assertEquals(77, r.getFactura().getCodCredito());
        assertEquals(6, r.getFactura().getNumCuotas());
    }

    @Test
    void creditoRechazadoPorElBancoNoGuardaFactura() {
        banco.maximo = 100;
        ResultadoFactura rechazado = servicio.facturarCredito("1722222222", "Otra", 1, 6);
        assertFalse(rechazado.isExito());
        assertNull(rechazado.getFactura());
        assertTrue(servicio.consultarFacturas("1722222222").isEmpty());
    }

    @Test
    void bancoCaidoNoGuardaFactura() {
        banco.caido = true;
        ResultadoFactura r = servicio.facturarCredito("1712345601", "Carlos", 1, 6);
        assertFalse(r.isExito());
        assertTrue(r.getMensaje().contains("banco"));
        assertTrue(servicio.consultarFacturas("1712345601").isEmpty());
    }

    @Test
    void validaCedulaTelefonoYNombre() {
        assertFalse(servicio.facturarEfectivo("123", "X", 1).isExito());
        assertFalse(servicio.facturarEfectivo("1712345601", "X", 999).isExito());
        assertFalse(servicio.facturarEfectivo("1712345601", "X", 3).isExito()); // no disponible
        assertFalse(servicio.facturarEfectivo("1799999999", "", 1).isExito()); // cliente nuevo sin nombre
    }

    @Test
    void clienteExistenteNoNecesitaNombreYSeReutiliza() {
        assertTrue(servicio.facturarEfectivo("1712345601", "Carlos", 1).isExito());
        ResultadoFactura r = servicio.facturarEfectivo("1712345601", "", 2);
        assertTrue(r.isExito(), r.getMensaje());
        assertEquals(2, servicio.consultarFacturas("1712345601").size());
        assertNotNull(r.getFactura().getFecha());
    }

    @Test
    void telefonoFacturadoSeMarcaNoDisponibleEnLugarDeBorrarse() {
        servicio.facturarEfectivo("1712345601", "Carlos", 1);
        assertTrue(servicio.eliminarTelefono(1));
        assertFalse(servicio.buscarTelefono(1).isDisponible());
    }

    // ---------- varios celulares en una sola venta ----------

    @Test
    void variosCelularesEnEfectivoGeneranUnaSolaFacturaConDescuento() {
        ResultadoFactura r = servicio.facturarEfectivoVarios("1712345601", "Carlos", List.of(1, 2));
        assertTrue(r.isExito(), r.getMensaje());
        assertEquals(1099.00, r.getFactura().getPrecioLista(), 0.0);          // 200 + 899
        assertEquals(461.58, r.getFactura().getDescuento(), 0.0);             // 42 %
        assertEquals(637.42, r.getFactura().getTotal(), 0.0);
        assertEquals(2, r.getFactura().getLineas().size());
        assertEquals(1, servicio.consultarFacturas("1712345601").size());     // una sola factura
        assertEquals("Samsung A15, Apple iPhone 15", r.getFactura().getTelefono());
    }

    @Test
    void codigoRepetidoEsUnaCantidadMayor() {
        ResultadoFactura r = servicio.facturarEfectivoVarios("1712345601", "Carlos", List.of(1, 1, 1));
        assertTrue(r.isExito(), r.getMensaje());
        assertEquals(1, r.getFactura().getLineas().size());
        assertEquals(3, r.getFactura().getLineas().get(0).getCantidad());
        assertEquals(600.00, r.getFactura().getPrecioLista(), 0.0);
        assertEquals("Samsung A15 x3", r.getFactura().getTelefono());
    }

    @Test
    void variosCelularesACreditoPiden_AlBancoElTotalYGuardanUnSoloCredito() {
        banco.maximo = 1500;
        ResultadoFactura r = servicio.facturarCreditoVarios("1712345601", "Carlos", List.of(1, 2), 6);
        assertTrue(r.isExito(), r.getMensaje());
        assertEquals(1099.00, r.getFactura().getTotal(), 0.0);
        assertEquals(1, banco.llamadasOtorgar);                               // un solo credito por el total
        assertEquals(6, r.getFactura().getNumCuotas());
    }

    @Test
    void creditoDeVariosCelularesSeRechazaSiElTotalSuperaElMaximo() {
        banco.maximo = 500;
        ResultadoFactura r = servicio.facturarCreditoVarios("1712345601", "Carlos", List.of(1, 2), 6);   // total 1099
        assertFalse(r.isExito());
        assertNull(r.getFactura());
        assertEquals(0, servicio.consultarFacturas("1712345601").size());
    }

    @Test
    void validaLaListaDeCelulares() {
        assertFalse(servicio.facturarEfectivoVarios("1712345601", "Carlos", List.of()).isExito());
        assertFalse(servicio.facturarEfectivoVarios("1712345601", "Carlos", List.of(1, 99)).isExito());   // 99 no existe
        assertFalse(servicio.facturarEfectivoVarios("1712345601", "Carlos", List.of(1, 3)).isExito());    // 3 no disponible
        assertEquals("Maximo 20 celulares por venta.",
                servicio.facturarEfectivoVarios("1712345601", "Carlos", java.util.Collections.nCopies(21, 1)).getMensaje());
        assertEquals(0, servicio.consultarFacturas("1712345601").size());
    }

    @Test
    void laVentaDeUnSoloCelularSigueIgualYGuardaSuLinea() {
        ResultadoFactura r = servicio.facturarEfectivo("1712345601", "Carlos", 2);
        assertEquals(1, r.getFactura().getLineas().size());
        assertEquals("Apple iPhone 15", r.getFactura().getTelefono());
    }
}
