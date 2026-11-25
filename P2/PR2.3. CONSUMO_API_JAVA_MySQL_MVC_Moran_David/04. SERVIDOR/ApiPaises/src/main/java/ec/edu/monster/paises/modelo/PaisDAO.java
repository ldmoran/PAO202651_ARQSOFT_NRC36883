package ec.edu.monster.paises.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Acceso a datos de la tabla pais (patron DAO). Todo el SQL esta aqui y usa parametros. */
public class PaisDAO {

    private static final String COLUMNAS = "id, codigo, nombre, capital, region, subregion, poblacion, area, moneda, bandera";
    private final Conexion conexion;

    public PaisDAO(Conexion conexion) {
        this.conexion = conexion;
    }

    private static Pais fila(ResultSet rs) throws SQLException {
        Pais p = new Pais();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setCapital(rs.getString("capital"));
        p.setRegion(rs.getString("region"));
        p.setSubregion(rs.getString("subregion"));
        p.setPoblacion(rs.getLong("poblacion"));
        p.setArea(rs.getDouble("area"));
        p.setMoneda(rs.getString("moneda"));
        p.setBandera(rs.getString("bandera"));
        return p;
    }

    /** Importacion: inserta el pais o, si ya existe su codigo, actualiza sus datos. Devuelve true si fue nuevo. */
    public boolean guardarOActualizar(Pais p) throws SQLException {
        try (Connection c = conexion.abrir()) {
            Optional<Integer> existente = idPorCodigo(c, p.getCodigo());
            if (existente.isPresent()) {
                p.setId(existente.get());
                actualizar(c, p);
                return false;
            }
            insertar(c, p);
            return true;
        }
    }

    private Optional<Integer> idPorCodigo(Connection c, String codigo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM pais WHERE codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            }
        }
    }

    public int insertar(Pais p) throws SQLException {
        try (Connection c = conexion.abrir()) {
            return insertar(c, p);
        }
    }

    private int insertar(Connection c, Pais p) throws SQLException {
        String sql = "INSERT INTO pais (codigo, nombre, capital, region, subregion, poblacion, area, moneda, bandera) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
        return p.getId();
    }

    public boolean actualizar(Pais p) throws SQLException {
        try (Connection c = conexion.abrir()) {
            return actualizar(c, p);
        }
    }

    private boolean actualizar(Connection c, Pais p) throws SQLException {
        String sql = "UPDATE pais SET codigo=?, nombre=?, capital=?, region=?, subregion=?, poblacion=?, area=?, moneda=?, bandera=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            parametros(ps, p);
            ps.setInt(10, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private static void parametros(PreparedStatement ps, Pais p) throws SQLException {
        ps.setString(1, p.getCodigo());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getCapital());
        ps.setString(4, p.getRegion());
        ps.setString(5, p.getSubregion());
        ps.setLong(6, p.getPoblacion());
        ps.setDouble(7, p.getArea());
        ps.setString(8, p.getMoneda());
        ps.setString(9, p.getBandera());
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM pais WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Pais> buscarPorId(int id) throws SQLException {
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNAS + " FROM pais WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(fila(rs)) : Optional.empty();
            }
        }
    }

    /** Lista con filtros opcionales: nombre contiene, capital contiene y region exacta. Orden por nombre. */
    public List<Pais> listar(String nombre, String capital, String region) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS + " FROM pais WHERE 1=1");
        List<String> valores = new ArrayList<>();
        if (nombre != null && !nombre.isBlank()) {
            sql.append(" AND nombre LIKE ?");
            valores.add("%" + escapar(nombre.trim()) + "%");
        }
        if (capital != null && !capital.isBlank()) {
            sql.append(" AND capital LIKE ?");
            valores.add("%" + escapar(capital.trim()) + "%");
        }
        if (region != null && !region.isBlank()) {
            sql.append(" AND region = ?");
            valores.add(region.trim());
        }
        sql.append(" ORDER BY nombre");
        List<Pais> lista = new ArrayList<>();
        try (Connection c = conexion.abrir(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < valores.size(); i++) {
                ps.setString(i + 1, valores.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(fila(rs));
                }
            }
        }
        return lista;
    }

    private static String escapar(String t) {
        return t.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public long contar() throws SQLException {
        try (Connection c = conexion.abrir(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM pais")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    // ------------------------------------------------------------------ reportes
    /** Reporte 1 y 3: por region, cantidad de paises, poblacion total y promedio poblacional. */
    public List<Map<String, Object>> porRegion() throws SQLException {
        String sql = "SELECT region, COUNT(*) AS paises, SUM(poblacion) AS poblacion_total, AVG(poblacion) AS promedio FROM pais GROUP BY region ORDER BY paises DESC, region";
        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection c = conexion.abrir(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("region", rs.getString("region"));
                m.put("paises", rs.getLong("paises"));
                m.put("poblacionTotal", rs.getLong("poblacion_total"));
                m.put("promedio", Math.round(rs.getDouble("promedio")));
                lista.add(m);
            }
        }
        return lista;
    }

    /** Reporte 2: el pais mas poblado. */
    public Optional<Pais> masPoblado() throws SQLException {
        try (Connection c = conexion.abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT " + COLUMNAS + " FROM pais ORDER BY poblacion DESC, nombre LIMIT 1")) {
            return rs.next() ? Optional.of(fila(rs)) : Optional.empty();
        }
    }
}
