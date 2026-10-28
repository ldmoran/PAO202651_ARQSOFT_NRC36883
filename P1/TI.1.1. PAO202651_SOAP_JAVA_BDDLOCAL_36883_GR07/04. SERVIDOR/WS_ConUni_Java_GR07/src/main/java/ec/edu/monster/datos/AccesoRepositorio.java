package ec.edu.monster.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/** Escribe en la bitacora INTENTO_ACCESO. */
public class AccesoRepositorio {

    private final FuenteDatos fuente;

    public AccesoRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    /** Registra un intento de inicio de sesion; idUsuario es null si el usuario no existe. */
    public void registrar(Integer idUsuario, String nombreIngresado, boolean exitoso, String detalle, Instant ahora) {
        String sql = "INSERT INTO intento_acceso (id_usuario, nombre_ingresado, exitoso, detalle, fecha_hora) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (idUsuario == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, idUsuario);
            }
            ps.setString(2, recortar(nombreIngresado, 50));
            ps.setBoolean(3, exitoso);
            ps.setString(4, detalle);
            ps.setTimestamp(5, Timestamp.from(ahora));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo registrar el intento de acceso", e);
        }
    }

    private static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
