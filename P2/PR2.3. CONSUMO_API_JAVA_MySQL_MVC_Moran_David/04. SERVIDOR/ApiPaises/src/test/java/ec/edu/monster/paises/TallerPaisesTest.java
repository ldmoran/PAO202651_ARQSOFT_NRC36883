package ec.edu.monster.paises;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ec.edu.monster.paises.controlador.PaisController;
import ec.edu.monster.paises.modelo.Conexion;
import ec.edu.monster.paises.modelo.ConsumoAPI;
import ec.edu.monster.paises.modelo.ConversorPais;
import ec.edu.monster.paises.modelo.Pais;
import ec.edu.monster.paises.modelo.PaisDAO;
import ec.edu.monster.paises.vista.ServidorWeb;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del taller: conversion del JSON, DAO (H2 en modo MySQL con el script real), controlador y servidor HTTP. */
class TallerPaisesTest {

    static final String JSON = """
            [{"name":"Ecuador","capital":"Quito","region":"Americas","subregion":"South America","population":17643060,"area":276841,
              "alpha3Code":"ECU","currencies":[{"code":"USD","name":"United States dollar","symbol":"$"}],"flags":{"png":"https://flagcdn.com/w320/ec.png"}},
             {"name":"Peru","capital":"Lima","region":"Americas","subregion":"South America","population":32971846,"area":1285216,
              "alpha3Code":"PER","currencies":[{"code":"PEN","name":"Peruvian sol"}],"flags":{"png":"https://flagcdn.com/w320/pe.png"}},
             {"name":"Japan","capital":"Tokyo","region":"Asia","subregion":"Eastern Asia","population":125836021,"area":377930,
              "alpha3Code":"JPN","currencies":[{"code":"JPY","name":"Japanese yen"}]},
             {"name":"Kenya","capital":"Nairobi","region":"Africa","subregion":"Eastern Africa","population":53771300,"area":580367,"alpha3Code":"KEN"},
             {"name":"Sin codigo","region":"Europe","population":1}]
            """;

    static Conexion conexion;
    static PaisDAO dao;
    static PaisController controlador;
    static ConsumoAPI apiFalsa;

    @BeforeAll
    static void preparar() throws Exception {
        conexion = new Conexion("jdbc:h2:mem:paises;MODE=MySQL;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        dao = new PaisDAO(conexion);
        apiFalsa = new ConsumoAPI("http://localhost") {
            @Override
            public String obtenerTodos() {
                return JSON;
            }

            @Override
            public String obtenerPorNombre(String nombre) {
                JsonArray salida = new JsonArray();
                new Gson().fromJson(JSON, JsonArray.class).forEach(e -> {
                    if (e.getAsJsonObject().get("name").getAsString().toLowerCase().contains(nombre.toLowerCase())) {
                        salida.add(e);
                    }
                });
                return salida.toString();
            }
        };
        controlador = new PaisController(dao, apiFalsa);
    }

    @BeforeEach
    void tablaLimpia() throws Exception {
        // se usa el script real del proyecto (sin las lineas de CREATE DATABASE y USE, que H2 no necesita)
        Path script = Path.of("..", "..", "03. BDD", "02_CrearTablas.sql");
        StringBuilder sql = new StringBuilder();
        for (String ln : Files.readAllLines(script)) {
            String t = ln.trim().toUpperCase();
            if (t.startsWith("CREATE DATABASE") || t.startsWith("USE ") || t.startsWith("--")) {
                continue;
            }
            sql.append(ln.replaceAll("--.*$", "")).append('\n');
        }
        try (Connection c = conexion.abrir(); Statement st = c.createStatement()) {
            for (String s : sql.toString().split(";")) {
                if (!s.isBlank()) {
                    st.execute(s);
                }
            }
        }
    }

    // ---------------------------------------------------------------- conversion
    @Test
    void conviertePaisesYTraduceLaRegion() {
        List<Pais> lista = ConversorPais.desdeJson(JSON);
        assertEquals(4, lista.size(), "el registro sin codigo alfa-3 se descarta");
        Pais ec = lista.get(0);
        assertEquals("ECU", ec.getCodigo());
        assertEquals("Quito", ec.getCapital());
        assertEquals("América", ec.getRegion());
        assertEquals(17643060L, ec.getPoblacion());
        assertEquals("United States dollar (USD)", ec.getMoneda());
        assertEquals("África", lista.get(3).getRegion());
        assertEquals("", lista.get(3).getMoneda());
    }

    // ---------------------------------------------------------------- importacion
    @Test
    void importarGuardaYNoDuplica() throws Exception {
        Map<String, Object> r1 = controlador.importarPaises();
        assertEquals(4, r1.get("nuevos"));
        assertEquals(4L, r1.get("total"));
        Map<String, Object> r2 = controlador.importarPaises();
        assertEquals(0, r2.get("nuevos"));
        assertEquals(4, r2.get("actualizados"));
        assertEquals(4L, dao.contar(), "importar dos veces no duplica");
    }

    // ---------------------------------------------------------------- busquedas y filtros
    @Test
    void busquedaPorNombreCapitalYRegion() throws Exception {
        controlador.importarPaises();
        assertEquals(1, controlador.listar("ecua", null, null).size());
        assertEquals("Peru", controlador.listar(null, "lima", null).get(0).getNombre());
        assertEquals(2, controlador.listar(null, null, "América").size());
        assertEquals(1, controlador.listar("a", null, "Asia").size());
        assertEquals(0, controlador.listar("zzz", null, null).size());
        assertEquals(0, controlador.listar("100%", null, null).size(), "el % se escapa y no actua como comodin");
    }

    // ---------------------------------------------------------------- CRUD
    @Test
    void crudCompleto() throws Exception {
        Pais p = new Pais();
        p.setCodigo("chl");
        p.setNombre("Chile");
        p.setCapital("Santiago");
        p.setRegion("América");
        p.setPoblacion(19116209);
        Pais creado = controlador.crear(p);
        assertTrue(creado.getId() > 0);
        assertEquals("CHL", creado.getCodigo());

        Pais cambio = new Pais();
        cambio.setCodigo("CHL");
        cambio.setNombre("Chile");
        cambio.setRegion("América");
        cambio.setCapital("Santiago de Chile");
        cambio.setPoblacion(19500000);
        assertEquals("Santiago de Chile", controlador.actualizar(creado.getId(), cambio).getCapital());
        assertEquals(19500000L, controlador.obtener(creado.getId()).getPoblacion());

        controlador.eliminar(creado.getId());
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.obtener(creado.getId()));
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.eliminar(creado.getId()));
    }

    @Test
    void validaciones() {
        Pais sinNombre = new Pais();
        sinNombre.setCodigo("ABC");
        sinNombre.setRegion("Asia");
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.crear(sinNombre));
        Pais codigoMalo = new Pais();
        codigoMalo.setNombre("X");
        codigoMalo.setCodigo("AB");
        codigoMalo.setRegion("Asia");
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.crear(codigoMalo));
        Pais negativo = new Pais();
        negativo.setNombre("X");
        negativo.setCodigo("ABC");
        negativo.setRegion("Asia");
        negativo.setPoblacion(-5);
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.crear(negativo));
    }

    // ---------------------------------------------------------------- actividad 1: consulta en la API
    @Test
    void consultaPorNombreEnLaApiYGuardado() throws Exception {
        List<Pais> encontrados = controlador.consultarEnApi("ecuador");
        assertEquals(1, encontrados.size());
        assertEquals(0, dao.contar(), "consultar no guarda");
        Pais guardado = controlador.guardarDesdeApi("ecuador", "ECU");
        assertEquals("Ecuador", guardado.getNombre());
        assertEquals(1, dao.contar());
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.guardarDesdeApi("ecuador", "XXX"));
        assertThrows(PaisController.ErrorDeNegocio.class, () -> controlador.consultarEnApi("  "));
    }

    // ---------------------------------------------------------------- actividad 5: reportes
    @Test
    @SuppressWarnings("unchecked")
    void reportes() throws Exception {
        controlador.importarPaises();
        Map<String, Object> r = controlador.reportes();
        List<Map<String, Object>> porRegion = (List<Map<String, Object>>) r.get("porRegion");
        assertEquals("América", porRegion.get(0).get("region"));
        assertEquals(2L, porRegion.get(0).get("paises"));
        assertEquals((17643060L + 32971846L) / 2, porRegion.get(0).get("promedio"));
        assertEquals("Japan", ((Pais) r.get("masPoblado")).getNombre());
    }

    // ---------------------------------------------------------------- servidor HTTP de extremo a extremo
    static ServidorWeb servidor;
    static int puerto;
    static final HttpClient http = HttpClient.newHttpClient();

    @BeforeAll
    static void iniciarServidor() throws IOException {
        servidor = new ServidorWeb(new PaisController(new PaisDAO(new Conexion("jdbc:h2:mem:paises;MODE=MySQL;DATABASE_TO_LOWER=TRUE;IGNORECASE=TRUE;DB_CLOSE_DELAY=-1", "sa", "")), apiFalsaSiempre()));
        puerto = servidor.iniciar("127.0.0.1", 0);
    }

    static ConsumoAPI apiFalsaSiempre() {
        return new ConsumoAPI("http://localhost") {
            @Override
            public String obtenerTodos() {
                return JSON;
            }

            @Override
            public String obtenerPorNombre(String n) {
                return "[]";
            }
        };
    }

    @AfterAll
    static void detenerServidor() {
        servidor.detener();
    }

    static HttpResponse<String> llamar(String metodo, String ruta, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + puerto + ruta));
        b.header("Content-Type", "application/json");
        b.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(cuerpo));
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void apiHttpCompleta() throws Exception {
        assertEquals(200, llamar("POST", "/api/importar", null).statusCode());
        HttpResponse<String> lista = llamar("GET", "/api/paises?region=" + java.net.URLEncoder.encode("América", "UTF-8"), null);
        assertEquals(200, lista.statusCode());
        assertEquals(2, new Gson().fromJson(lista.body(), JsonArray.class).size());

        HttpResponse<String> nuevo = llamar("POST", "/api/paises", "{\"codigo\":\"CHL\",\"nombre\":\"Chile\",\"region\":\"América\",\"capital\":\"Santiago\",\"poblacion\":19116209}");
        assertEquals(201, nuevo.statusCode());
        int id = new Gson().fromJson(nuevo.body(), JsonObject.class).get("id").getAsInt();
        assertEquals(200, llamar("PUT", "/api/paises/" + id, "{\"codigo\":\"CHL\",\"nombre\":\"Chile\",\"region\":\"América\",\"capital\":\"Stgo\"}").statusCode());
        assertEquals(400, llamar("POST", "/api/paises", "{\"codigo\":\"X\",\"nombre\":\"\"}").statusCode());
        assertEquals(400, llamar("POST", "/api/paises", "esto no es json").statusCode());
        assertEquals(200, llamar("DELETE", "/api/paises/" + id, null).statusCode());
        assertEquals(400, llamar("GET", "/api/paises/" + id, null).statusCode());

        HttpResponse<String> rep = llamar("GET", "/api/reportes", null);
        assertEquals(200, rep.statusCode());
        assertTrue(rep.body().contains("masPoblado"));
        assertEquals(404, llamar("GET", "/api/nada", null).statusCode());
        assertEquals(204, llamar("OPTIONS", "/api/paises", null).statusCode());
    }

    @Test
    void sirveLaInterfazWeb() throws Exception {
        HttpResponse<String> r = llamar("GET", "/", null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("<html"));
        assertEquals(404, llamar("GET", "/no-existe.js", null).statusCode());
    }
}
