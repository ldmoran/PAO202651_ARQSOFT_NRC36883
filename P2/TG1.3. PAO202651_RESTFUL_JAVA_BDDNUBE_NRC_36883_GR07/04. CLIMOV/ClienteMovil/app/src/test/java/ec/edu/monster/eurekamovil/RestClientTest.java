package ec.edu.monster.eurekamovil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.eurekamovil.modelo.Cuenta;
import ec.edu.monster.eurekamovil.modelo.ErrorServicio;
import ec.edu.monster.eurekamovil.modelo.Movimiento;
import ec.edu.monster.eurekamovil.modelo.Resultado;
import ec.edu.monster.eurekamovil.modelo.RestClient;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RestClientTest {

    private MockWebServer server;

    @Before
    public void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void stopServer() throws Exception {
        server.shutdown();
    }

    private RestClient client() {
        return new RestClient(server.url("/api").toString());
    }

    @Test
    public void loginEnviaJsonYLeeElResultado() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"resultado\":true,\"mensaje\":\"Inicio de sesión exitoso\"}"));
        Resultado r = client().login("lmoran", "Eureka2026!");
        assertTrue(r.ok);
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/sesion/login", request.getPath());
        assertTrue(request.getBody().readUtf8().contains("\"contrasena\":\"Eureka2026!\""));
    }

    @Test
    public void loginIncorrectoDevuelveElMensajeDelServidor() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"resultado\":false,\"mensaje\":\"Usuario o contraseña incorrectos\"}"));
        Resultado r = client().login("lmoran", "mala");
        assertFalse(r.ok);
        assertEquals("Usuario o contraseña incorrectos", r.mensaje);
    }

    @Test
    public void leeCuentasMovimientosYTipos() throws Exception {
        server.enqueue(new MockResponse().setBody("[{\"codigo\":\"00100001\",\"nombreCliente\":\"Coronel Castillo, Eric\",\"saldo\":7000.00,"
                + "\"estado\":\"ACTIVO\",\"moneda\":\"Soles\"}]"));
        List<Cuenta> cuentas = client().cuentas();
        assertEquals(1, cuentas.size());
        assertEquals(7000.0, cuentas.get(0).saldo, 0.001);

        server.enqueue(new MockResponse().setBody("[{\"codigoCuenta\":\"00100001\",\"numero\":3,\"fecha\":\"2026-02-02\","
                + "\"tipo\":\"Retiro Salida\",\"importe\":1000.00,\"saldoActual\":7000.00}]"));
        List<Movimiento> mov = client().movimientos("00100001");
        assertEquals(3, mov.get(0).numero);
        server.takeRequest();
        assertEquals("/api/movimientos?cuenta=00100001", server.takeRequest().getPath());

        server.enqueue(new MockResponse().setBody("[\"DEPOSITO\",\"RETIRO\",\"TRANSFERENCIA\"]"));
        assertEquals(3, client().tipos().size());
    }

    @Test
    public void registrarEnviaLosCamposYMapeaLosEstados() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"codigo\":1,\"mensaje\":\"Operación exitosa\"}"));
        assertTrue(client().registrar("00100001", "00100002", "TRANSFERENCIA", 50).ok);
        String cuerpo = server.takeRequest().getBody().readUtf8();
        assertTrue(cuerpo.contains("\"cuentaDestino\":\"00100002\""));
        assertTrue(cuerpo.contains("\"tipo\":\"TRANSFERENCIA\""));

        server.enqueue(new MockResponse().setResponseCode(422).setBody("{\"codigo\":-1,\"mensaje\":\"Saldo insuficiente en la cuenta 00100001.\"}"));
        Resultado r = client().registrar("00100001", null, "RETIRO", 1e9);
        assertFalse(r.ok);
        assertEquals("Saldo insuficiente en la cuenta 00100001.", r.mensaje);
    }

    @Test(expected = ErrorServicio.class)
    public void sinServidorLanzaErrorServicio() {
        new RestClient("http://127.0.0.1:1/api").cuentas();
    }

    @Test
    public void errorHttpUsaElMensajeDelServidor() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"codigo\":-1,\"mensaje\":\"Error interno del servidor\"}"));
        try {
            client().cuentas();
        } catch (ErrorServicio e) {
            assertEquals("Error interno del servidor", e.getMessage());
            return;
        }
        throw new AssertionError("debia lanzar ErrorServicio");
    }
}
