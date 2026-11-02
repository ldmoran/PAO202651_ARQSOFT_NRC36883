package ec.edu.monster.clienteweb;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.clienteweb.controlador.Formato;
import ec.edu.monster.clienteweb.controlador.Json;
import ec.edu.monster.clienteweb.modelo.RespuestaServicio;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void escapaCaracteresEspeciales() {
        assertEquals("a\\\"b\\\\c\\nd", Json.escapar("a\"b\\c\nd"));
        assertEquals("", Json.escapar(null));
    }

    @Test
    void respuestaNuncaIncluyeElToken() {
        RespuestaServicio r = new RespuestaServicio();
        r.setEstado("OK");
        r.setCodigo("OK-200");
        r.setMensaje("Autenticación exitosa");
        r.setToken("secreto-123");
        String json = Json.de(r);
        assertFalse(json.contains("secreto-123"));
        assertFalse(json.contains("token"));
        assertTrue(json.contains("\"estado\":\"OK\""));
    }

    @Test
    void incluyeResultadoYDatos() {
        RespuestaServicio r = new RespuestaServicio();
        r.setEstado("OK");
        r.setResultado(212.0);
        r.setUnidadOrigen("celsius");
        r.getDatos().add("Longitud");
        r.getDatos().add("Masa");
        String json = Json.de(r);
        assertTrue(json.contains("\"resultado\":212"));
        assertTrue(json.contains("\"resultadoTexto\":\"212\""));
        assertTrue(json.contains("\"datos\":[\"Longitud\",\"Masa\"]"));
        assertTrue(json.contains("\"unidadDestino\":null"));
    }

    @Test
    void formatoDeNumeros() {
        assertEquals("5.0292", Formato.numero(5.0292));
        assertEquals("212", Formato.numero(212.0));
        assertEquals("1000000000000", Formato.numero(1e12));
    }
}
