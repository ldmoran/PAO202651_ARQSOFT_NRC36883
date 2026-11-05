package ec.edu.monster.clienteconsola;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.clienteconsola.modelo.Cuenta;
import ec.edu.monster.clienteconsola.modelo.ErrorServicio;
import ec.edu.monster.clienteconsola.modelo.Movimiento;
import ec.edu.monster.clienteconsola.modelo.Resultado;
import ec.edu.monster.clienteconsola.modelo.RestClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Pruebas del cliente REST y del menu contra un servidor HTTP falso (no requiere Payara ni MySQL). */
class RestClientTest {

    private HttpServer servidor;
    private RestClient cliente;
    private String ultimoCuerpo = "";

    @BeforeEach
    void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/api/sesion/login", ex -> {
            ultimoCuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            boolean ok = ultimoCuerpo.contains("Eureka2026!");
            responder(ex, ok ? 200 : 401, ok ? "{\"resultado\":true,\"mensaje\":\"Inicio de sesión exitoso\"}"
                    : "{\"resultado\":false,\"mensaje\":\"Usuario o contraseña incorrectos\"}");
        });
        servidor.createContext("/api/cuentas", ex -> responder(ex, 200,
                "[{\"codigo\":\"00100001\",\"nombreCliente\":\"Coronel Castillo, Eric\",\"saldo\":7000.00,\"estado\":\"ACTIVO\",\"moneda\":\"Soles\",\"emailCliente\":\"eric@correo.ec\"}]"));
        servidor.createContext("/api/movimientos/tipos", ex -> responder(ex, 200, "[\"DEPOSITO\",\"RETIRO\",\"TRANSFERENCIA\"]"));
        servidor.createContext("/api/movimientos", ex -> {
            if ("POST".equals(ex.getRequestMethod())) {
                ultimoCuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                boolean ok = ultimoCuerpo.contains("\"importe\":100.0");
                responder(ex, ok ? 200 : 422, ok ? "{\"codigo\":1,\"mensaje\":\"Operación exitosa\"}"
                        : "{\"codigo\":-1,\"mensaje\":\"Saldo insuficiente en la cuenta 00100001.\"}");
            } else {
                ultimoCuerpo = ex.getRequestURI().toString();
                responder(ex, 200, "[{\"codigoCuenta\":\"00100001\",\"numero\":3,\"fecha\":\"2026-02-02\",\"tipo\":\"Retiro Salida\","
                        + "\"importe\":1000.00,\"saldoActual\":7000.00,\"nombreCliente\":\"Coronel Castillo, Eric\"}]");
            }
        });
        servidor.createContext("/api/falla", ex -> responder(ex, 500, "{\"codigo\":-1,\"mensaje\":\"Error interno del servidor\"}"));
        servidor.start();
        cliente = new RestClient("http://127.0.0.1:" + servidor.getAddress().getPort() + "/api");
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    private static void responder(com.sun.net.httpserver.HttpExchange ex, int estado, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(estado, b.length);
        ex.getResponseBody().write(b);
        ex.close();
    }

    @Test
    void loginCorrectoEIncorrecto() {
        Resultado ok = cliente.login("lmoran", "Eureka2026!");
        assertTrue(ok.ok());
        assertTrue(ultimoCuerpo.contains("\"usuario\":\"lmoran\""));
        Resultado mal = cliente.login("lmoran", "x");
        assertFalse(mal.ok());
        assertEquals("Usuario o contraseña incorrectos", mal.mensaje());
    }

    @Test
    void leeCuentasMovimientosYTipos() {
        List<Cuenta> cuentas = cliente.cuentas();
        assertEquals(1, cuentas.size());
        assertEquals("Coronel Castillo, Eric", cuentas.get(0).nombreCliente());
        assertEquals(0, new java.math.BigDecimal("7000").compareTo(cuentas.get(0).saldo()));
        List<Movimiento> mov = cliente.movimientos("00100001");
        assertEquals(3, mov.get(0).numero());
        assertTrue(ultimoCuerpo.endsWith("movimientos?cuenta=00100001"));
        assertEquals(List.of("DEPOSITO", "RETIRO", "TRANSFERENCIA"), cliente.tipos());
    }

    @Test
    void registrarDevuelveElMensajeDelServidorEnExitoYEnReglaDeNegocio() {
        assertTrue(cliente.registrar("00100001", null, "DEPOSITO", 100).ok());
        assertTrue(ultimoCuerpo.contains("\"tipo\":\"DEPOSITO\""));
        Resultado r = cliente.registrar("00100001", null, "RETIRO", 5);
        assertFalse(r.ok());
        assertEquals("Saldo insuficiente en la cuenta 00100001.", r.mensaje());
    }

    @Test
    void sinServidorOConErrorSeLanzaErrorServicio() {
        ErrorServicio sinConexion = assertThrows(ErrorServicio.class, () -> new RestClient("http://127.0.0.1:1/api").cuentas());
        assertEquals("No se pudo conectar con el servidor", sinConexion.getMessage());
        ErrorServicio e500 = assertThrows(ErrorServicio.class,
                () -> new RestClient("http://127.0.0.1:" + servidor.getAddress().getPort() + "/api/falla/").cuentas());
        assertEquals("Error interno del servidor", e500.getMessage());
    }

    @Test
    void menuDeConsolaIniciaSesionYMuestraCuentas() {
        String entradaTexto = "1\nlmoran\nEureka2026!\n2\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ClienteConsola(cliente, new Scanner(entradaTexto), new PrintStream(bytes, true, StandardCharsets.UTF_8)).ejecutar();
        String texto = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("[OK] Inicio de sesión exitoso"));
        assertTrue(texto.contains("Sesión: ACTIVA"));
        assertTrue(texto.contains("00100001"));
        assertTrue(texto.contains("7000.00"));
        assertTrue(texto.contains("Hasta luego."));
    }

    @Test
    void menuNoPermiteOperarSinSesion() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ClienteConsola(cliente, new Scanner("2\n0\n"), new PrintStream(bytes, true, StandardCharsets.UTF_8)).ejecutar();
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Opción no válida."));
    }
}
