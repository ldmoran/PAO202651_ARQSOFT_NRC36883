package ec.edu.monster.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.datos.Datos;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.pruebas.BaseDatosPrueba;
import ec.edu.monster.pruebas.RelojManual;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GestorTokensTest {

    private static final int ID_MONSTER = 1;
    private RelojManual reloj;

    @BeforeEach
    void preparar() throws Exception {
        reloj = new RelojManual(Instant.parse("2026-10-27T12:00:00Z"));
        Datos.configurar(BaseDatosPrueba.crear("tokens"), reloj);
    }

    @AfterEach
    void limpiar() {
        System.clearProperty("conuni.token.minutos");
    }

    @AfterAll
    static void soltar() {
        Datos.restablecer();
    }

    @Test
    void tokenRecienGeneradoEsValido() {
        assertNull(GestorTokens.validar(GestorTokens.generarToken(ID_MONSTER)));
    }

    @Test
    void tokenInexistenteOAlterado() {
        String t = GestorTokens.generarToken(ID_MONSTER);
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(t + "x"));
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(""));
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(null));
        assertEquals("AUTH-001", CodigoResultado.TOKEN_INVALIDO.getCodigo());
    }

    @Test
    void tokenExpiradoDaSesionExpiradaYQuedaCerradoEnLaBase() {
        String t = GestorTokens.generarToken(ID_MONSTER);
        reloj.avanzar(Duration.ofMinutes(29));
        assertNull(GestorTokens.validar(t)); // aun vale: la vigencia por defecto es de 30 minutos
        reloj.avanzar(Duration.ofMinutes(2));
        assertEquals(CodigoResultado.SESION_EXPIRADA, GestorTokens.validar(t));
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(t)); // la sesion ya quedo inactiva
    }

    @Test
    void vigenciaConfigurable() {
        System.setProperty("conuni.token.minutos", "1");
        String t = GestorTokens.generarToken(ID_MONSTER);
        reloj.avanzar(Duration.ofSeconds(61));
        assertEquals(CodigoResultado.SESION_EXPIRADA, GestorTokens.validar(t));
    }
}
