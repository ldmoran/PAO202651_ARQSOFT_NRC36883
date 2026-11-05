package ec.edu.monster.eureka.datos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexion a MySQL (esquema "eurekabank").
 *
 * Los datos de conexion no estan en el codigo: se leen, en este orden, de
 * 1) propiedades del sistema (-Deureka.db.url, -Deureka.db.usuario, -Deureka.db.clave),
 * 2) variables de entorno (EUREKA_DB_URL, EUREKA_DB_USUARIO, EUREKA_DB_CLAVE),
 * 3) el archivo eureka.properties del classpath (valores por defecto de desarrollo).
 */
public class ConexionMySql implements FuenteDatos {

    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionMySql() {
        Properties archivo = new Properties();
        try (InputStream in = ConexionMySql.class.getResourceAsStream("/eureka.properties")) {
            if (in != null) {
                archivo.load(in);
            }
        } catch (IOException e) {
            // se usan los valores por defecto
        }
        this.url = valor("eureka.db.url", "EUREKA_DB_URL", archivo.getProperty("db.url",
                "jdbc:mysql://localhost:3306/eurekabank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil"));
        this.usuario = valor("eureka.db.usuario", "EUREKA_DB_USUARIO", archivo.getProperty("db.usuario", "eureka_app"));
        this.clave = valor("eureka.db.clave", "EUREKA_DB_CLAVE", archivo.getProperty("db.clave", ""));
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
