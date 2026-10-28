package ec.edu.monster.datos;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.controladores.manejadoresws.ManejadorCatalogo;
import ec.edu.monster.controladores.manejadoresws.ManejadorConversion;
import ec.edu.monster.controladores.manejadoresws.ManejadorLogin;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.pruebas.BaseDatosPrueba;
import ec.edu.monster.seguridad.Claves;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Lo que el servidor deja guardado en la base: bitacora, sesiones, historial y catalogo editable. */
class PersistenciaTest {

    private FuenteDatos base;

    @BeforeEach
    void preparar() throws Exception {
        base = BaseDatosPrueba.crear("persistencia");
        Datos.configurar(base, BaseDatosPrueba.RELOJ);
    }

    @AfterAll
    static void soltar() {
        Datos.restablecer();
    }

    private long contar(String sql) throws SQLException {
        try (Connection cn = base.obtenerConexion(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @Test
    void laClaveNoSeGuardaEnClaro() throws Exception {
        assertEquals(0, contar("SELECT COUNT(*) FROM usuario WHERE hash_clave LIKE '%monster9%'"));
        assertTrue(Claves.coincide("monster9", "a3f1c9d27b4e5086d1f2a9b7c3e4d5f6",
                "4b6aa92fc59932c627242729eb132f7c3c6343e1aa6d907c00fda4dee58d3b30"));
        assertFalse(Claves.coincide("Monster9", "a3f1c9d27b4e5086d1f2a9b7c3e4d5f6",
                "4b6aa92fc59932c627242729eb132f7c3c6343e1aa6d907c00fda4dee58d3b30"));
        assertNotEquals(Claves.generarSal(), Claves.generarSal());
    }

    @Test
    void cadaIntentoDeLoginQuedaEnLaBitacora() throws Exception {
        ManejadorLogin login = new ManejadorLogin();
        assertTrue(login.login("monster", "monster9").esExitosa());
        assertEquals("AUTH-003", login.login("monster", "mala").getCodigo());
        assertEquals("AUTH-003", login.login("fantasma", "x").getCodigo());
        assertEquals(3, contar("SELECT COUNT(*) FROM intento_acceso"));
        assertEquals(1, contar("SELECT COUNT(*) FROM intento_acceso WHERE exitoso = 1"));
        assertEquals(1, contar("SELECT COUNT(*) FROM intento_acceso WHERE id_usuario IS NULL AND nombre_ingresado = 'fantasma'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM sesion"));
    }

    @Test
    void cadaConversionCorrectaQuedaEnElHistorial() throws Exception {
        String token = new ManejadorLogin().login("monster", "monster9").getToken();
        ManejadorConversion conversion = new ManejadorConversion();
        assertTrue(conversion.convertir("Longitud", 1, "millas", "metros", token).esExitosa());
        assertTrue(conversion.convertir("Temperatura", 100, "celsius", "fahrenheit", token).esExitosa());
        // las conversiones con error no se guardan
        assertFalse(conversion.convertir("Masa", 1, "toneladas", "libras", token).esExitosa());
        assertEquals(2, contar("SELECT COUNT(*) FROM conversion"));
        assertEquals(1, contar("SELECT COUNT(*) FROM v_historial_conversiones "
                + "WHERE nombre_usuario = 'monster' AND categoria = 'Temperatura' AND unidad_destino = 'fahrenheit' AND resultado = 212"));
    }

    @Test
    void agregarUnaCategoriaSoloRequiereInsertarFilas() throws Exception {
        try (Connection cn = base.obtenerConexion(); Statement st = cn.createStatement()) {
            st.execute("INSERT INTO categoria (nombre, descripcion) VALUES ('Volumen', 'Capacidad: base litro')");
            st.execute("INSERT INTO unidad (id_categoria, nombre, simbolo, factor_base, desplazamiento, orden) VALUES "
                    + "(4, 'mililitros', 'ml', 0.001, 0, 1), (4, 'litros', 'L', 1, 0, 2), (4, 'galones', 'gal', 3.785411784, 0, 3)");
        }
        String token = new ManejadorLogin().login("monster", "monster9").getToken();
        RespuestaServicio categorias = new ManejadorCatalogo().obtenerCategorias(token);
        assertEquals(List.of("Longitud", "Masa", "Temperatura", "Volumen"), categorias.getDatos());
        RespuestaServicio r = new ManejadorConversion().convertir("Volumen", 1, "galones", "litros", token);
        assertTrue(r.esExitosa());
        assertEquals(3.785411784, r.getResultado(), 1e-9);
    }

    @Test
    void unaUnidadDesactivadaDejaDeOfrecerse() throws Exception {
        try (Connection cn = base.obtenerConexion(); Statement st = cn.createStatement()) {
            st.execute("UPDATE unidad SET activa = 0 WHERE nombre = 'quintales'");
        }
        String token = new ManejadorLogin().login("monster", "monster9").getToken();
        assertEquals(List.of("gramos", "kilogramos", "onzas", "libras"),
                new ManejadorCatalogo().obtenerUnidades(token, "Masa").getDatos());
        assertEquals("UNIT-001", new ManejadorConversion().convertir("Masa", 1, "quintales", "libras", token).getCodigo());
    }

    @Test
    void siLaBaseNoRespondeElServicioDevuelveSrv500() {
        Datos.configurar(() -> {
            throw new SQLException("sin conexion");
        }, BaseDatosPrueba.RELOJ);
        assertEquals("SRV-500", new ManejadorLogin().login("monster", "monster9").getCodigo());
        assertEquals("SRV-500", new ManejadorCatalogo().obtenerCategorias("x").getCodigo());
    }
}
