package ec.edu.monster.eureka;

import ec.edu.monster.eureka.db.FuenteDatos;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Base H2 en memoria (modo MySQL) creada con los scripts reales de 03. BDD (tablas y datos). */
public final class BaseDatosPrueba {

    private static final Path SCRIPTS = Path.of("..", "..", "03. BDD");

    private BaseDatosPrueba() {
    }

    public static FuenteDatos crear(String nombre) throws SQLException {
        String url = "jdbc:h2:mem:" + nombre + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        FuenteDatos fuente = () -> DriverManager.getConnection(url, "sa", "");
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement()) {
            for (String script : new String[] {"02_CrearTablas.sql", "03_CargaDatos.sql"}) {
                for (String sentencia : leer(script).split(";\\s*(\\r?\\n|$)")) {
                    String s = sentencia.replaceAll("(?m)^\\s*--.*$", "").trim();
                    if (s.isEmpty() || s.toUpperCase().startsWith("USE ")) {
                        continue;
                    }
                    if (s.toUpperCase().startsWith("DROP TABLE")) {
                        continue;   // la base de prueba nace vacia
                    }
                    st.execute(s);
                }
            }
        }
        return fuente;
    }

    private static String leer(String script) {
        try {
            return Files.readString(SCRIPTS.resolve(script), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + script, e);
        }
    }
}
