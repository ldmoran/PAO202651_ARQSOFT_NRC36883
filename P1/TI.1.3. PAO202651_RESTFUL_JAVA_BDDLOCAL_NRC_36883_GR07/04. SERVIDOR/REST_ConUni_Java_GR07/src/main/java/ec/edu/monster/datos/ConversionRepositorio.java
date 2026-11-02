package ec.edu.monster.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/** Escribe en la tabla CONVERSION (historial de conversiones). */
public class ConversionRepositorio {

    private final FuenteDatos fuente;

    public ConversionRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    /** Guarda una conversion ligada a la sesion del token. */
    public void registrar(String token, int idUnidadOrigen, int idUnidadDestino,
            double valorEntrada, double resultado, Instant ahora) {
        String sql = "INSERT INTO conversion (id_sesion, id_unidad_origen, id_unidad_destino, valor_entrada, resultado, fecha_hora) "
                + "SELECT id_sesion, ?, ?, ?, ?, ? FROM sesion WHERE token = ?";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUnidadOrigen);
            ps.setInt(2, idUnidadDestino);
            ps.setDouble(3, valorEntrada);
            ps.setDouble(4, resultado);
            ps.setTimestamp(5, Timestamp.from(ahora));
            ps.setString(6, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo guardar la conversion en el historial", e);
        }
    }
}
