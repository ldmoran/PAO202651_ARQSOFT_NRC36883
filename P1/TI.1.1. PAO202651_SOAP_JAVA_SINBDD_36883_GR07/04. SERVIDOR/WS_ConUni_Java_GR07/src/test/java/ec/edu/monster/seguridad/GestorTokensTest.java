package ec.edu.monster.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GestorTokensTest {

    @AfterEach
    void limpiar() {
        System.clearProperty("conuni.token.minutos");
    }

    @Test
    void tokenRecienGeneradoEsValido() {
        assertNull(GestorTokens.validar(GestorTokens.generarToken()));
    }

    @Test
    void tokenInexistenteOAlterado() {
        String t = GestorTokens.generarToken();
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(t + "x"));
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(""));
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(null));
        assertEquals("AUTH-001", CodigoResultado.TOKEN_INVALIDO.getCodigo());
    }

    @Test
    void tokenExpiradoDaSesionExpiradaYSeElimina() throws Exception {
        System.setProperty("conuni.token.minutos", "0.0005"); // unos 30 ms
        String t = GestorTokens.generarToken();
        Thread.sleep(150);
        assertEquals(CodigoResultado.SESION_EXPIRADA, GestorTokens.validar(t));
        assertEquals(CodigoResultado.TOKEN_INVALIDO, GestorTokens.validar(t)); // ya no existe
    }
}
