package ec.edu.monster.clienteweb.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Validacion de la direccion del servidor que se escribe en el login (solo http/https con host). */
class ApiServletTest {

    @Test
    void urlVaciaUsaLaDelDescriptor() {
        assertEquals("", ApiServlet.urlValida(""));
        assertEquals("", ApiServlet.urlValida("   "));
    }

    @Test
    void aceptaHttpYHttps() {
        assertEquals("http://192.168.0.10:8080/REST_EurekaBank_Java_GR07/api/", ApiServlet.urlValida(" http://192.168.0.10:8080/REST_EurekaBank_Java_GR07/api/ "));
        assertEquals("https://servidor/api/", ApiServlet.urlValida("https://servidor/api/"));
    }

    @Test
    void rechazaOtrosEsquemasYTextoSinHost() {
        assertNull(ApiServlet.urlValida("file:///etc/passwd"));
        assertNull(ApiServlet.urlValida("ftp://servidor/api/"));
        assertNull(ApiServlet.urlValida("no es una url"));
        assertNull(ApiServlet.urlValida("http://"));
    }
}
