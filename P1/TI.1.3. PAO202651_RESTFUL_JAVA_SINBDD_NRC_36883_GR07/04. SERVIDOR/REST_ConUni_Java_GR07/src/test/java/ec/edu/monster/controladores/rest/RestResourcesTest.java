package ec.edu.monster.controladores.rest;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestResourcesTest {

    @Test
    void loginEntregaTokenYPermiteConsultarCatalogo() {
        Response respuestaLogin = new LoginResource().login(new SolicitudLogin("monster", "monster9"));

        assertEquals(200, respuestaLogin.getStatus());
        RespuestaServicio login = (RespuestaServicio) respuestaLogin.getEntity();
        assertNotNull(login.getToken());

        Response respuestaCatalogo = new CatalogoResource()
                .obtenerCategorias("Bearer " + login.getToken());

        assertEquals(200, respuestaCatalogo.getStatus());
        RespuestaServicio catalogo = (RespuestaServicio) respuestaCatalogo.getEntity();
        assertEquals("OK-200", catalogo.getCodigo());
        assertEquals(3, catalogo.getDatos().size());
    }

    @Test
    void catalogoRechazaTokenAusente() {
        Response respuesta = new CatalogoResource().obtenerCategorias(null);

        assertEquals(401, respuesta.getStatus());
        assertEquals("AUTH-001", ((RespuestaServicio) respuesta.getEntity()).getCodigo());
    }

    @Test
    void loginRechazaCredencialesIncorrectas() {
        Response respuesta = new LoginResource().login(new SolicitudLogin("monster", "incorrecta"));

        assertEquals(401, respuesta.getStatus());
        assertEquals("AUTH-003", ((RespuestaServicio) respuesta.getEntity()).getCodigo());
    }

    @Test
    void conversionValidaCategoriaUnidadYResultado() {
        String token = tokenValido();
        SolicitudConversion solicitud = new SolicitudConversion(10.0, "metros", "pies");

        Response respuesta = new ConversionResource().convertir("Longitud", solicitud, "Bearer " + token);

        assertEquals(200, respuesta.getStatus());
        RespuestaServicio conversion = (RespuestaServicio) respuesta.getEntity();
        assertEquals(32.8083989501, conversion.getResultado(), 0.000000001);
    }

    @Test
    void conversionSinValorDevuelveErrorDeValidacion() {
        Response respuesta = new ConversionResource().convertir(
                "Longitud", new SolicitudConversion(), "Bearer " + tokenValido());

        assertEquals(400, respuesta.getStatus());
        assertEquals("DATA-001", ((RespuestaServicio) respuesta.getEntity()).getCodigo());
    }

    @Test
    void unidadesDeCategoriasDistintasDevuelven400() {
        Response respuesta = new ConversionResource().convertir(
                "Longitud",
                new SolicitudConversion(1.0, "metros", "kilogramos"),
                "Bearer " + tokenValido());

        assertEquals(400, respuesta.getStatus());
        assertEquals("DATA-001", ((RespuestaServicio) respuesta.getEntity()).getCodigo());
    }

    @Test
    void unidadDesconocidaDevuelve422() {
        Response respuesta = new ConversionResource().convertir(
                "Longitud",
                new SolicitudConversion(1.0, "metros", "unidades-desconocidas"),
                "Bearer " + tokenValido());

        assertEquals(422, respuesta.getStatus());
        assertEquals("UNIT-001", ((RespuestaServicio) respuesta.getEntity()).getCodigo());
    }

    @Test
    void conversionDeCategoriaInexistenteDevuelve400() {
        Response respuesta = new ConversionResource().convertir(
                "Volumen",
                new SolicitudConversion(1.0, "metros", "metros"),
                "Bearer " + tokenValido());

        assertEquals(400, respuesta.getStatus());
        assertEquals("DATA-001", ((RespuestaServicio) respuesta.getEntity()).getCodigo());
    }

    private String tokenValido() {
        RespuestaServicio login = (RespuestaServicio) new LoginResource()
                .login(new SolicitudLogin("monster", "monster9"))
                .getEntity();
        return login.getToken();
    }
}
