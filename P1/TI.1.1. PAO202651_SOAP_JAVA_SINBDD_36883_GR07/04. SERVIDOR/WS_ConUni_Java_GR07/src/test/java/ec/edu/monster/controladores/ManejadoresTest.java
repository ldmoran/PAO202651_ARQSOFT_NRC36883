package ec.edu.monster.controladores;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.controladores.manejadoresws.ManejadorCatalogo;
import ec.edu.monster.controladores.manejadoresws.ManejadorConversion;
import ec.edu.monster.controladores.manejadoresws.ManejadorLogin;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManejadoresTest {

    private final ManejadorLogin login = new ManejadorLogin();
    private final ManejadorCatalogo catalogo = new ManejadorCatalogo();
    private final ManejadorConversion conversion = new ManejadorConversion();
    private String token;

    @BeforeEach
    void iniciarSesion() {
        System.clearProperty("conuni.token.minutos");
        RespuestaServicio r = login.login("monster", "monster9");
        assertTrue(r.esExitosa());
        token = r.getToken();
    }

    private RespuestaServicio convertir(String categoria, double valor, String origen, String destino, String tk) {
        return conversion.convertir(categoria, valor, origen, destino, tk);
    }

    private void assertError(RespuestaServicio r, String codigo) {
        assertEquals("ERROR", r.getEstado());
        assertEquals(codigo, r.getCodigo());
        assertNull(r.getResultado());
    }

    // ---- CU-01 ----
    @Test
    void loginCorrectoDevuelveToken() {
        RespuestaServicio r = login.login("monster", "monster9");
        assertEquals("OK", r.getEstado());
        assertEquals("OK-200", r.getCodigo());
        assertNotNull(r.getToken());
    }

    @Test
    void loginIncorrectoNoDaToken() {
        RespuestaServicio r = login.login("monster", "mala");
        assertEquals("AUTH-003", r.getCodigo());
        assertNull(r.getToken());
    }

    @Test
    void loginConCamposVacios() {
        assertEquals("DATA-001", login.login("", "monster9").getCodigo());
        assertEquals("DATA-001", login.login("monster", null).getCodigo());
    }

    // ---- CU-02 / CU-03 ----
    @Test
    void categoriasYUnidades() {
        RespuestaServicio c = catalogo.obtenerCategorias(token);
        assertEquals(List.of("Longitud", "Masa", "Temperatura"), c.getDatos());
        RespuestaServicio u = catalogo.obtenerUnidades(token, "masa");
        assertEquals(List.of("gramos", "kilogramos", "onzas", "libras", "quintales"), u.getDatos());
        assertEquals("DATA-001", catalogo.obtenerUnidades(token, "volumen").getCodigo());
    }

    @Test
    void catalogoExigeToken() {
        assertEquals("AUTH-001", catalogo.obtenerCategorias("xyz").getCodigo());
        assertEquals("AUTH-001", catalogo.obtenerUnidades(null, "Masa").getCodigo());
    }

    // ---- CU-04 conversiones ----
    @Test
    void longitud() {
        RespuestaServicio r = convertir("Longitud", 1, "millas", "metros", token);
        assertEquals(1609.344, r.getResultado(), 1e-9);
        r = convertir("Longitud", 100, "centimetros", "metros", token);
        assertEquals(1.0, r.getResultado(), 1e-9);
        r = convertir("Longitud", 3, "pies", "yardas", token);
        assertEquals(1.0, r.getResultado(), 1e-9);
        assertEquals("OK", r.getEstado());
        assertEquals("pies", r.getUnidadOrigen());
        assertEquals("yardas", r.getUnidadDestino());
        // kilometros no esta en el catalogo del ERS
        assertError(convertir("Longitud", 5, "kilometros", "metros", token), "UNIT-001");
    }

    @Test
    void masa() {
        assertEquals(1000.0, convertir("Masa", 1, "kilogramos", "gramos", token).getResultado(), 1e-9);
        assertEquals(100.0, convertir("Masa", 1, "quintales", "libras", token).getResultado(), 1e-9);
        assertEquals(16.0, convertir("Masa", 1, "libras", "onzas", token).getResultado(), 1e-6);
    }

    @Test
    void temperatura() {
        assertEquals(212.0, convertir("Temperatura", 100, "celsius", "fahrenheit", token).getResultado(), 1e-9);
        assertEquals(373.15, convertir("Temperatura", 100, "celsius", "kelvin", token).getResultado(), 1e-9);
        assertEquals(0.0, convertir("Temperatura", 32, "fahrenheit", "celsius", token).getResultado(), 1e-9);
        assertEquals(33.0, convertir("Temperatura", 100, "celsius", "newton", token).getResultado(), 1e-9);
        assertEquals(80.0, convertir("Temperatura", 100, "celsius", "reaumur", token).getResultado(), 1e-9);
        assertEquals(100.0, convertir("Temperatura", 80, "reaumur", "celsius", token).getResultado(), 1e-9);
    }

    @Test
    void aceptaMayusculasEspaciosYTildes() {
        RespuestaServicio r = convertir("longitud", 1, " METROS ", "Centímetros".replace("í", "i"), token);
        assertEquals(100.0, r.getResultado(), 1e-9);
        r = convertir("Masa", 1, "KILOGRAMOS", "Gramos", token);
        assertEquals(1000.0, r.getResultado(), 1e-9);
    }

    // ---- errores CU-08 a CU-11 ----
    @Test
    void tokenInvalido() {
        assertError(convertir("Masa", 1, "gramos", "libras", "no-existe"), "AUTH-001");
        assertError(convertir("Masa", 1, "gramos", "libras", null), "AUTH-001");
    }

    @Test
    void unidadNoSoportada() {
        RespuestaServicio r = convertir("Masa", 1, "toneladas", "libras", token);
        assertError(r, "UNIT-001");
        assertEquals("Unidad de origen no soportada", r.getMensaje());
        r = convertir("Masa", 1, "gramos", "toneladas", token);
        assertEquals("Unidad de destino no soportada", r.getMensaje());
    }

    @Test
    void datosInconsistentes() {
        assertError(convertir("Masa", Double.NaN, "gramos", "libras", token), "DATA-001");
        assertError(convertir("Masa", 1, null, "libras", token), "DATA-001");
        assertError(convertir("Masa", 1, "gramos", " ", token), "DATA-001");
        // longitud -> temperatura: cada unidad existe, pero en categorias distintas
        assertError(convertir("Longitud", 1, "metros", "celsius", token), "DATA-001");
        // unidades de otra categoria usadas en una operacion que no es la suya
        assertError(convertir("Longitud", 1, "celsius", "kelvin", token), "DATA-001");
        assertError(convertir("Volumen", 1, "litros", "litros", token), "DATA-001");
    }

    @Test
    void primeroSeValidaElToken() {
        // con token malo y datos malos, gana el error de token (CU-05 antes que CU-06)
        assertEquals("AUTH-001", convertir("Masa", Double.NaN, null, null, "x").getCodigo());
    }
}
