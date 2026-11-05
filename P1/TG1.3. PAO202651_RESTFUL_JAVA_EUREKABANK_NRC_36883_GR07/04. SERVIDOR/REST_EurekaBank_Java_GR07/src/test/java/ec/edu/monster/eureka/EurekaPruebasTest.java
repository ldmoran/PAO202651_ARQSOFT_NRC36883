package ec.edu.monster.eureka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.eureka.controladores.CuentaResource;
import ec.edu.monster.eureka.controladores.MovimientoResource;
import ec.edu.monster.eureka.controladores.RespuestaSesion;
import ec.edu.monster.eureka.controladores.SesionResource;
import ec.edu.monster.eureka.controladores.SolicitudLogin;
import ec.edu.monster.eureka.controladores.SolicitudMovimiento;
import ec.edu.monster.eureka.datos.Datos;
import ec.edu.monster.eureka.datos.EurekaRepositorio;
import ec.edu.monster.eureka.datos.FuenteDatos;
import ec.edu.monster.eureka.modelos.DatosCuenta;
import ec.edu.monster.eureka.modelos.MovimientoData;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import ec.edu.monster.eureka.servicios.Claves;
import ec.edu.monster.eureka.servicios.ServicioEureka;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del servidor EurekaBank contra una base H2 creada con los scripts reales de "03. BDD". */
class EurekaPruebasTest {

    private FuenteDatos fuente;
    private ServicioEureka servicio;

    @BeforeEach
    void preparar() throws SQLException {
        fuente = BaseDatosPrueba.crear("eureka" + UUID.randomUUID().toString().replace("-", ""));
        Datos.configurar(fuente, BaseDatosPrueba.RELOJ);
        servicio = new ServicioEureka(new EurekaRepositorio(fuente));
    }

    @AfterEach
    void limpiar() {
        Datos.restablecer();
    }

    private BigDecimal saldo(String cuenta) throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement();
                ResultSet rs = st.executeQuery("SELECT saldo FROM cuenta WHERE cod_cuenta = '" + cuenta + "'")) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private int contar(String tabla) throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------------------------------------------------------------- sesion
    @Test
    void loginCorrectoYRegistraElIntento() throws SQLException {
        assertTrue(servicio.iniciarSesion("lmoran", "Eureka2026!"));
        assertEquals(1, contar("intento_acceso"));
    }

    @Test
    void loginConClaveMalaOUsuarioInexistenteFalla() throws SQLException {
        assertFalse(servicio.iniciarSesion("lmoran", "otra"));
        assertFalse(servicio.iniciarSesion("nadie", "Eureka2026!"));
        assertFalse(servicio.iniciarSesion("", "x"));
        assertFalse(servicio.iniciarSesion("lmoran", null));
    }

    @Test
    void claveSeGuardaComoHashConSal() {
        String hash = Claves.hash("a1a6e7cbcfd7e9f1", "Eureka2026!");
        assertEquals("86a357fd59c6b18cbe85d78f5a5df101258900952f576d7bf82669fe91f023f0", hash);
        assertTrue(Claves.coincide("a1a6e7cbcfd7e9f1", "Eureka2026!", hash));
        assertFalse(Claves.coincide("a1a6e7cbcfd7e9f1", "eureka2026!", hash));
    }

    // ---------------------------------------------------------------- consultas
    @Test
    void cuentasSoloListaLasActivasConDatosDelCliente() throws SQLException {
        List<DatosCuenta> cuentas = servicio.cuentas();
        assertEquals(5, cuentas.size());
        DatosCuenta primera = cuentas.get(0);
        assertEquals("00100001", primera.getCodigo());
        assertEquals("Coronel Castillo, Eric", primera.getNombreCliente());
        assertEquals(0, new BigDecimal("7000.00").compareTo(primera.getSaldo()));
        assertEquals("eric@correo.ec", primera.getEmailCliente());
        assertTrue(cuentas.stream().noneMatch(c -> c.getCodigo().equals("00100004")));
    }

    @Test
    void movimientosFiltraPorCuentaYMuestraTodosSinFiltro() throws SQLException {
        List<MovimientoData> m = servicio.movimientos("00100001");
        assertEquals(3, m.size());
        assertEquals(3, m.get(0).getNumero());
        assertEquals("Retiro Salida", m.get(0).getTipo());
        assertEquals("2026-02-02", m.get(0).getFecha());
        assertEquals(9, servicio.movimientos(null).size());
    }

    // ---------------------------------------------------------------- operaciones
    @Test
    void depositoAumentaElSaldoYCreaElMovimiento() throws SQLException {
        ResultadoOperacion r = servicio.registrar("00100001", null, "deposito", 250.5);
        assertEquals(1, r.getCodigo());
        assertEquals(200, r.estadoHttp());
        assertEquals(0, new BigDecimal("7250.50").compareTo(saldo("00100001")));
        MovimientoData ultimo = servicio.movimientos("00100001").get(0);
        assertEquals(4, ultimo.getNumero());
        assertEquals("2026-11-05", ultimo.getFecha());
        assertEquals("Depósito Entrada", ultimo.getTipo());
    }

    @Test
    void retiroDisminuyeElSaldoYNoPermiteDejarloNegativo() throws SQLException {
        assertEquals(1, servicio.registrar("00100001", null, "RETIRO", 1000.0).getCodigo());
        assertEquals(0, new BigDecimal("6000.00").compareTo(saldo("00100001")));
        ResultadoOperacion r = servicio.registrar("00100001", null, "RETIRO", 999999.0);
        assertEquals(422, r.estadoHttp());
        assertEquals("Saldo insuficiente en la cuenta 00100001.", r.getMensaje());
        assertEquals(0, new BigDecimal("6000.00").compareTo(saldo("00100001")));
    }

    @Test
    void transferenciaMueveAmbasCuentasYGuardaLaReferencia() throws SQLException {
        assertEquals(1, servicio.registrar("00100001", "00100002", "TRANSFERENCIA", 500.0).getCodigo());
        assertEquals(0, new BigDecimal("6500.00").compareTo(saldo("00100001")));
        assertEquals(0, new BigDecimal("7300.00").compareTo(saldo("00100002")));
        assertEquals("00100002", servicio.movimientos("00100001").get(0).getReferencia());
        assertEquals("00100001", servicio.movimientos("00100002").get(0).getReferencia());
    }

    @Test
    void transferenciaAUnaCuentaInexistenteNoDejaCambiosParciales() throws SQLException {
        ResultadoOperacion r = servicio.registrar("00100001", "99999999", "TRANSFERENCIA", 500.0);
        assertEquals(422, r.estadoHttp());
        assertEquals("La cuenta 99999999 no existe.", r.getMensaje());
        assertEquals(0, new BigDecimal("7000.00").compareTo(saldo("00100001")));
        assertEquals(3, servicio.movimientos("00100001").size());
    }

    @Test
    void reglasDeNegocioYValidaciones() {
        assertEquals("No se puede transferir a la misma cuenta.", servicio.registrar("00100001", "00100001", "TRANSFERENCIA", 10.0).getMensaje());
        assertEquals("La cuenta 00100004 no esta activa.", servicio.registrar("00100004", null, "DEPOSITO", 10.0).getMensaje());
        assertEquals(400, servicio.registrar("00100001", null, "DEPOSITO", 0.0).estadoHttp());
        assertEquals(400, servicio.registrar("00100001", null, "DEPOSITO", null).estadoHttp());
        assertEquals(400, servicio.registrar("", null, "DEPOSITO", 10.0).estadoHttp());
        assertEquals(400, servicio.registrar("00100001", null, "", 10.0).estadoHttp());
        assertEquals("Tipo de movimiento no válido: PRESTAMO", servicio.registrar("00100001", null, "PRESTAMO", 10.0).getMensaje());
        assertEquals("Depósito no debe tener cuenta destino", servicio.registrar("00100001", "00100002", "DEPOSITO", 10.0).getMensaje());
        assertEquals("Cuenta destino es requerida para transferencia", servicio.registrar("00100001", null, "TRANSFERENCIA", 10.0).getMensaje());
    }

    // ---------------------------------------------------------------- recursos REST
    @Test
    void recursoLoginResponde200Y401() {
        SesionResource sesion = new SesionResource();
        Response ok = sesion.login(new SolicitudLogin("amiranda", "Eureka2026!"));
        assertEquals(200, ok.getStatus());
        assertTrue(((RespuestaSesion) ok.getEntity()).isResultado());
        assertEquals(401, sesion.login(new SolicitudLogin("amiranda", "mala")).getStatus());
        assertEquals(400, sesion.login(null).getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    void recursosDeCuentasMovimientosYTipos() {
        Response cuentas = new CuentaResource().cuentas();
        assertEquals(200, cuentas.getStatus());
        assertEquals(5, ((List<DatosCuenta>) cuentas.getEntity()).size());
        MovimientoResource mov = new MovimientoResource();
        assertEquals(2, ((List<MovimientoData>) mov.movimientos("00100002").getEntity()).size());
        assertEquals(List.of("DEPOSITO", "RETIRO", "TRANSFERENCIA"), mov.tipos().getEntity());
    }

    @Test
    void recursoDeMovimientosMapeaLosEstadosHttp() {
        MovimientoResource mov = new MovimientoResource();
        assertEquals(200, mov.registrar(new SolicitudMovimiento("00200001", null, "DEPOSITO", 100.0)).getStatus());
        assertEquals(422, mov.registrar(new SolicitudMovimiento("00200001", null, "RETIRO", 1.0e9)).getStatus());
        assertEquals(400, mov.registrar(new SolicitudMovimiento("00200001", null, "DEPOSITO", -5.0)).getStatus());
        assertEquals(400, mov.registrar(null).getStatus());
    }
}
