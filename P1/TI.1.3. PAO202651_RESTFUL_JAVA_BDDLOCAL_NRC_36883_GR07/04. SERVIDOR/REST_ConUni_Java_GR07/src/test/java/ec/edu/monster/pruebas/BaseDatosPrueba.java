package ec.edu.monster.pruebas;

import ec.edu.monster.datos.FuenteDatos;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Base H2 en memoria (modo MySQL) creada con los MISMOS scripts de la carpeta "03. BDD", asi las
 * pruebas ejercitan el esquema y los datos reales. Los scripts se buscan desde la carpeta del proyecto.
 */
public final class BaseDatosPrueba {

    public static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-27T12:00:00Z"), ZoneId.of("UTC"));

    private static final Path BDD = Path.of("..", "..", "03. BDD");

    private BaseDatosPrueba() {
    }

    public static FuenteDatos crear(String nombre) throws SQLException {
        String url = "jdbc:h2:mem:" + nombre + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        FuenteDatos fuente = () -> DriverManager.getConnection(url, "sa", "");
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement()) {
            for (String archivo : new String[] {"02_CreaBD_ConversionUnidades.sql", "03_CargaDatos_ConversionUnidades.sql"}) {
                for (String sentencia : sentencias(BDD.resolve(archivo))) {
                    st.execute(sentencia);
                }
            }
        }
        return fuente;
    }

    /** Parte el script en sentencias, sin comentarios ni las lineas propias de MySQL (CREATE DATABASE, USE). */
    static java.util.List<String> sentencias(Path script) {
        String texto;
        try {
            texto = Files.readString(script, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se encuentra el script " + script.toAbsolutePath().normalize(), e);
        }
        StringBuilder limpio = new StringBuilder();
        for (String linea : texto.split("\\R")) {
            int comentario = linea.indexOf("--");
            limpio.append(comentario >= 0 ? linea.substring(0, comentario) : linea).append('\n');
        }
        java.util.List<String> resultado = new java.util.ArrayList<>();
        for (String sentencia : limpio.toString().split(";")) {
            String s = sentencia.trim();
            String mayus = s.toUpperCase();
            if (!s.isEmpty() && !mayus.startsWith("CREATE DATABASE") && !mayus.startsWith("USE ")) {
                resultado.add(s);
            }
        }
        return resultado;
    }
}
