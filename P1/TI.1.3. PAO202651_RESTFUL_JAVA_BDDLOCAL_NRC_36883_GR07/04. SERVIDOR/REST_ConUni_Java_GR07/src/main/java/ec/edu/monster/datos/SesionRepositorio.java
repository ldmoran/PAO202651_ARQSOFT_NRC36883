package ec.edu.monster.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/** Lee y escribe la tabla SESION (los tokens emitidos al iniciar sesion). */
public class SesionRepositorio {

    /** Estado de una sesion guardada. */
    public record Sesion(long id, Instant expiracion, boolean activa) {
    }

    private final FuenteDatos fuente;

    public SesionRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    public void crear(String token, int idUsuario, Instant emision, Instant expiracion) {
        String sql = "INSERT INTO sesion (token, id_usuario, fecha_emision, fecha_expiracion, activa) VALUES (?, ?, ?, ?, 1)";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, idUsuario);
            ps.setTimestamp(3, Timestamp.from(emision));
            ps.setTimestamp(4, Timestamp.from(expiracion));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo guardar la sesion", e);
        }
    }

    public Optional<Sesion> buscar(String token) {
        String sql = "SELECT id_sesion, fecha_expiracion, activa FROM sesion WHERE token = ?";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Sesion(rs.getLong("id_sesion"),
                            rs.getTimestamp("fecha_expiracion").toInstant(), rs.getBoolean("activa")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo consultar la sesion", e);
        }
    }

    /** Marca la sesion como no activa (por ejemplo, cuando se detecta que expiro). */
    public void desactivar(String token) {
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement("UPDATE sesion SET activa = 0 WHERE token = ?")) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo cerrar la sesion", e);
        }
    }
}
