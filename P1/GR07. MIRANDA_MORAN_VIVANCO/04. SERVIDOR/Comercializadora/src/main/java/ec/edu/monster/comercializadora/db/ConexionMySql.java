package ec.edu.monster.comercializadora.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexion a MySQL (esquema "comercializadora").
 *
 * Los datos de conexion no estan en el codigo: se leen, en este orden, de
 * 1) propiedades del sistema (-Dcomercializadora.db.url, -Dcomercializadora.db.usuario, -Dcomercializadora.db.clave),
 * 2) variables de entorno (COMERCIALIZADORA_DB_URL, COMERCIALIZADORA_DB_USUARIO, COMERCIALIZADORA_DB_CLAVE),
 * 3) el archivo comercializadora.properties del classpath (valores por defecto de desarrollo).
 */
public class ConexionMySql implements FuenteDatos {

    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionMySql() {
        Properties archivo = new Properties();
        try (InputStream in = ConexionMySql.class.getResourceAsStream("/comercializadora.properties")) {
            if (in != null) {
                archivo.load(in);
            }
        } catch (IOException e) {
            // se usan los valores por defecto
        }
        this.url = valor("comercializadora.db.url", "COMERCIALIZADORA_DB_URL", archivo.getProperty("db.url",
                "jdbc:mysql://localhost:3306/comercializadora?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil"));
        this.usuario = valor("comercializadora.db.usuario", "COMERCIALIZADORA_DB_USUARIO", archivo.getProperty("db.usuario", "banquito_app"));
        this.clave = valor("comercializadora.db.clave", "COMERCIALIZADORA_DB_CLAVE", archivo.getProperty("db.clave", ""));
    }

    private static String valor(String propiedad, String variable, String porDefecto) {
        String v = System.getProperty(propiedad);
        if (v == null || v.isBlank()) {
            v = System.getenv(variable);
        }
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    @Override
    public Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encuentra el driver de MySQL", e);
        }
        return DriverManager.getConnection(url, usuario, clave);
    }
}
