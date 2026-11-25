package ec.edu.monster.paises.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexion a la base api_paises.
 *
 * Los datos de conexion no estan en el codigo: se leen, en este orden, de
 * 1) propiedades del sistema (-Dpaises.db.url, -Dpaises.db.usuario, -Dpaises.db.clave),
 * 2) variables de entorno (PAISES_DB_URL, PAISES_DB_USUARIO, PAISES_DB_CLAVE),
 * 3) valores por defecto de desarrollo (MySQL local).
 * Para usar una base en la nube (por ejemplo Amazon RDS) solo se cambian esas variables.
 */
public class Conexion {

    private final String url;
    private final String usuario;
    private final String clave;

    public Conexion() {
        this(valor("paises.db.url", "PAISES_DB_URL",
                        "jdbc:mysql://localhost:3306/api_paises?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil"),
                valor("paises.db.usuario", "PAISES_DB_USUARIO", "paises_app"),
                valor("paises.db.clave", "PAISES_DB_CLAVE", "Paises2026!"));
    }

    public Conexion(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private static String valor(String propiedad, String variable, String porDefecto) {
        String v = System.getProperty(propiedad);
        if (v == null || v.isBlank()) {
            v = System.getenv(variable);
        }
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}
