package ec.edu.monster.datos;

import ec.edu.monster.modelos.entidades.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Lee la tabla USUARIO. */
public class UsuarioRepositorio {

    private final FuenteDatos fuente;

    public UsuarioRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    /** Usuario activo con ese nombre (la base compara sin distinguir mayusculas; el servicio si lo hace). */
    public Optional<Usuario> buscarActivo(String nombre) {
        String sql = "SELECT id_usuario, nombre_usuario, sal_clave, hash_clave FROM usuario "
                + "WHERE nombre_usuario = ? AND activo = 1";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(rs.getInt("id_usuario"), rs.getString("nombre_usuario"),
                            rs.getString("sal_clave"), rs.getString("hash_clave")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo consultar el usuario", e);
        }
    }
}
