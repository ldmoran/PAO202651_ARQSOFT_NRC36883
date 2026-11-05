package ec.edu.monster.eureka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.eureka.db.FuenteDatos;
import ec.edu.monster.eureka.modelos.MovimientoData;
import ec.edu.monster.eureka.repositorios.EurekaRepositorio;
import ec.edu.monster.eureka.servicios.Claves;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServicioEurekaTest {

    private static final AtomicInteger CONTADOR = new AtomicInteger();
    private FuenteDatos bd;
    private ServicioEureka servicio;

    @BeforeEach
    void preparar() throws Exception {
        bd = BaseDatosPrueba.crear("eureka_serv_" + CONTADOR.incrementAndGet());
        servicio = new ServicioEureka(new EurekaRepositorio(bd));
    }

    private BigDecimal saldo(String cuenta) throws Exception {
        try (Connection cn = bd.obtenerConexion(); Statement st = cn.createStatement();
                ResultSet rs = st.executeQuery("SELECT saldo FROM cuenta WHERE cod_cuenta = '" + cuenta + "'")) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private int contar(String tabla) throws Exception {
        try (Connection cn = bd.obtenerConexion(); Statement st = cn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------- sesion ----------
    @Test
    void loginCorrecto() {
        assertTrue(servicio.iniciarSesion("lmoran", "Eureka2026!"));
    }

    @Test
    void loginIncorrecto() {
        assertFalse(servicio.iniciarSesion("lmoran", "otra"));
        assertFalse(servicio.iniciarSesion("nadie", "Eureka2026!"));
        assertFalse(servicio.iniciarSesion("", ""));
        assertFalse(servicio.iniciarSesion(null, null));
    }

    @Test
    void loginRegistraIntentos() throws Exception {
        servicio.iniciarSesion("lmoran", "mala");
        servicio.iniciarSesion("lmoran", "Eureka2026!");
        assertEquals(2, contar("intento_acceso"));
    }

    @Test
    void laClaveNoEstaEnTextoPlano() {
        var u = new EurekaRepositorio(bd).buscarUsuario("lmoran").orElseThrow();
        assertNotEquals("Eureka2026!", u.claveHash());
        assertEquals(64, u.claveHash().length());
        assertEquals(Claves.hash(u.sal(), "Eureka2026!"), u.claveHash());
    }

    // ---------- consultas ----------
    @Test
    void cuentasActivasConCliente() {
        var l = servicio.cuentas();
        assertEquals(5, l.size());   // la sexta esta ANULADA
        assertTrue(l.stream().anyMatch(c -> c.getCodigo().equals("00100001") && c.getNombreCliente().startsWith("Coronel")));
    }

    @Test
    void movimientosDeUnaCuentaConSaldo() {
        List<MovimientoData> l = servicio.movimientos("00100001");
        assertEquals(3, l.size());
        assertEquals(3, l.get(0).getNumero());   // del mas reciente al mas antiguo
        assertEquals(0, new BigDecimal("7000.00").compareTo(l.get(0).getSaldoActual()));
        assertTrue(l.get(0).getTipo().endsWith("Salida"));
    }

    @Test
    void movimientosDeTodasLasCuentas() {
        assertEquals(9, servicio.movimientos("").size());
    }

    @Test
    void tiposPermitidos() {
        assertEquals(List.of("DEPOSITO", "RETIRO", "TRANSFERENCIA"), servicio.tiposPermitidos());
    }

    // ---------- operaciones ----------
    @Test
    void deposito() throws Exception {
        assertEquals(1, servicio.registrar("00100001", null, "DEPOSITO", 250.50).getCodigo());
        assertEquals(0, new BigDecimal("7250.50").compareTo(saldo("00100001")));
        assertEquals(4, servicio.movimientos("00100001").size());
    }

    @Test
    void retiro() throws Exception {
        assertEquals(1, servicio.registrar("00100001", "", "retiro", 1000).getCodigo());
        assertEquals(0, new BigDecimal("6000").compareTo(saldo("00100001")));
    }

    @Test
    void retiroConSaldoInsuficienteNoCambiaNada() throws Exception {
        var r = servicio.registrar("00100001", null, "RETIRO", 99999);
        assertEquals(-1, r.getCodigo());
        assertTrue(r.getMensaje().contains("Saldo insuficiente"));
        assertEquals(0, new BigDecimal("7000").compareTo(saldo("00100001")));
        assertEquals(9, contar("movimiento"));
    }

    @Test
    void transferencia() throws Exception {
        assertEquals(1, servicio.registrar("00100001", "00100002", "TRANSFERENCIA", 100).getCodigo());
        assertEquals(0, new BigDecimal("6900").compareTo(saldo("00100001")));
        assertEquals(0, new BigDecimal("6900").compareTo(saldo("00100002")));
        assertEquals(11, contar("movimiento"));
        assertEquals("00100001", servicio.movimientos("00100002").get(0).getReferencia());
    }

    @Test
    void transferenciaFallidaDeshaceTodo() throws Exception {
        var r = servicio.registrar("00100001", "99999999", "TRANSFERENCIA", 100);   // destino inexistente
        assertEquals(-1, r.getCodigo());
        assertEquals(0, new BigDecimal("7000").compareTo(saldo("00100001")));       // la salida se deshizo
        assertEquals(9, contar("movimiento"));
    }

    @Test
    void transferenciaALaMismaCuenta() {
        assertEquals(-1, servicio.registrar("00100001", "00100001", "TRANSFERENCIA", 10).getCodigo());
    }

    @Test
    void cuentaAnuladaNoOpera() {
        assertEquals(-1, servicio.registrar("00100004", null, "DEPOSITO", 10).getCodigo());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "'',,DEPOSITO,10", "00100001,,DEPOSITO,0", "00100001,,DEPOSITO,-5", "00100001,,'',10",
        "00100001,,PRESTAMO,10", "00100001,00100002,DEPOSITO,10", "00100001,,TRANSFERENCIA,10"})
    void validacionesDeEntrada(String origen, String destino, String tipo, double importe) {
        assertEquals(-1, servicio.registrar(origen, destino, tipo, importe).getCodigo());
    }
}
