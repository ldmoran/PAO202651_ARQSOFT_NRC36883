package ec.edu.monster.clienteweb;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.clienteweb.controlador.Servicios;
import org.junit.jupiter.api.Test;

class ServiciosTest {

    @Test
    void aceptaDireccionesHttpYHttpsConHost() {
        assertTrue(Servicios.urlValida("http://localhost:8080/REST_ConUni_Java_GR07/api/"));
        assertTrue(Servicios.urlValida("https://conversor.example/api"));
    }

    @Test
    void rechazaEsquemasYUrlsIncompletos() {
        assertFalse(Servicios.urlValida(""));
        assertFalse(Servicios.urlValida("ftp://conversor.example/api"));
        assertFalse(Servicios.urlValida("http://"));
        assertFalse(Servicios.urlValida("http://conversor.example/api?other=value"));
    }
}
