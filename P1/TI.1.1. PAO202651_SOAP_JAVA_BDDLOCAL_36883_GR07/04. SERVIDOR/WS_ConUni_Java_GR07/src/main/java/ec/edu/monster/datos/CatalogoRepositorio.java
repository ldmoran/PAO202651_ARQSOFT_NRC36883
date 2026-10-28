package ec.edu.monster.datos;

import ec.edu.monster.modelos.entidades.Categoria;
import ec.edu.monster.modelos.entidades.Unidad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lee las tablas CATEGORIA y UNIDAD (solo las filas activas). */
public class CatalogoRepositorio {

    private static final String SQL = "SELECT c.id_categoria, c.nombre AS categoria, u.id_unidad, u.nombre AS unidad, "
            + "u.factor_base, u.desplazamiento "
            + "FROM categoria c JOIN unidad u ON u.id_categoria = c.id_categoria "
            + "WHERE c.activa = 1 AND u.activa = 1 ORDER BY c.id_categoria, u.orden";

    private final FuenteDatos fuente;

    public CatalogoRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    /** Todas las categorias con sus unidades, en el orden definido en la base. */
    public List<Categoria> cargar() {
        Map<Integer, String> nombres = new LinkedHashMap<>();
        Map<Integer, List<Unidad>> unidades = new LinkedHashMap<>();
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idCategoria = rs.getInt("id_categoria");
                nombres.putIfAbsent(idCategoria, rs.getString("categoria"));
                unidades.computeIfAbsent(idCategoria, k -> new ArrayList<>()).add(new Unidad(
                        rs.getInt("id_unidad"), rs.getString("unidad"),
                        rs.getDouble("factor_base"), rs.getDouble("desplazamiento")));
            }
        } catch (SQLException e) {
            throw new ErrorDatos("No se pudo leer el catalogo de unidades", e);
        }
        List<Categoria> resultado = new ArrayList<>();
        nombres.forEach((id, nombre) -> resultado.add(new Categoria(id, nombre, List.copyOf(unidades.get(id)))));
        return resultado;
    }
}
