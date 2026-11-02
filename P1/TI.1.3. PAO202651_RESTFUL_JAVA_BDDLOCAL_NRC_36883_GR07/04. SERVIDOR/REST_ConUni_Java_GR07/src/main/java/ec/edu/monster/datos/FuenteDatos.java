package ec.edu.monster.datos;

import java.sql.Connection;
import java.sql.SQLException;

/** Entrega conexiones a la base de datos. Las pruebas usan otra implementacion (H2 en memoria). */
@FunctionalInterface
public interface FuenteDatos {

    Connection obtenerConexion() throws SQLException;
}
