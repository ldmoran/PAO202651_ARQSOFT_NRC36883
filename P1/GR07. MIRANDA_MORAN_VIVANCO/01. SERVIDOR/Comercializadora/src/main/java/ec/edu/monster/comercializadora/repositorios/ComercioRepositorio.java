package ec.edu.monster.comercializadora.repositorios;

import ec.edu.monster.comercializadora.db.FuenteDatos;
import ec.edu.monster.comercializadora.modelos.Factura;
import ec.edu.monster.comercializadora.modelos.Telefono;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Acceso a los datos de la Comercializadora (esquema comercializadora). */
public class ComercioRepositorio {

    private static final String SELECT_FACTURA = "SELECT f.NUM_FACTURA, f.FECHA, c.CEDULA, c.NOMBRE, f.COD_TELEFONO, "
            + "t.MARCA, t.MODELO, f.FORMA_PAGO, f.PRECIO_LISTA, f.DESCUENTO, f.TOTAL, f.NUM_CUOTAS, f.COD_CREDITO "
            + "FROM FACTURA f JOIN CLIENTE c ON c.COD_CLIENTE = f.COD_CLIENTE "
            + "JOIN TELEFONO t ON t.COD_TELEFONO = f.COD_TELEFONO ";

    private final FuenteDatos fuente;

    public ComercioRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    // ---------- Catalogo ----------

    public List<Telefono> listarTelefonos() throws SQLException {
        List<Telefono> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); Statement st = cn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COD_TELEFONO, MARCA, MODELO, PRECIO, DISPONIBLE FROM TELEFONO "
                        + "ORDER BY COD_TELEFONO")) {
            while (rs.next()) {
                lista.add(telefono(rs));
            }
        }
        return lista;
    }

    public Optional<Telefono> buscarTelefono(int cod) throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(
                "SELECT COD_TELEFONO, MARCA, MODELO, PRECIO, DISPONIBLE FROM TELEFONO WHERE COD_TELEFONO = ?")) {
            ps.setInt(1, cod);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(telefono(rs)) : Optional.empty();
            }
        }
    }

    private static Telefono telefono(ResultSet rs) throws SQLException {
        return new Telefono(rs.getInt("COD_TELEFONO"), rs.getString("MARCA"), rs.getString("MODELO"),
                rs.getBigDecimal("PRECIO").doubleValue(), rs.getBoolean("DISPONIBLE"));
    }

    public int crearTelefono(String marca, String modelo, BigDecimal precio) throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO TELEFONO (MARCA, MODELO, PRECIO, DISPONIBLE) VALUES (?, ?, ?, 1)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, marca);
            ps.setString(2, modelo);
            ps.setBigDecimal(3, precio);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean actualizarTelefono(int cod, String marca, String modelo, BigDecimal precio, boolean disponible)
            throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(
                "UPDATE TELEFONO SET MARCA = ?, MODELO = ?, PRECIO = ?, DISPONIBLE = ? WHERE COD_TELEFONO = ?")) {
            ps.setString(1, marca);
            ps.setString(2, modelo);
            ps.setBigDecimal(3, precio);
            ps.setBoolean(4, disponible);
            ps.setInt(5, cod);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean tieneFacturas(int codTelefono) throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(
                "SELECT COUNT(*) FROM FACTURA WHERE COD_TELEFONO = ?")) {
            ps.setInt(1, codTelefono);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean eliminarTelefono(int cod) throws SQLException {
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement("DELETE FROM TELEFONO WHERE COD_TELEFONO = ?")) {
            ps.setInt(1, cod);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Facturacion ----------

    /** Devuelve el codigo del cliente con esa cedula; si no existe lo crea con el nombre dado. */
    private int clienteDe(Connection cn, String cedula, String nombre) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT COD_CLIENTE FROM CLIENTE WHERE CEDULA = ?")) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO CLIENTE (CEDULA, NOMBRE) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cedula);
            ps.setString(2, nombre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public boolean existeCliente(String cedula) throws SQLException {
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM CLIENTE WHERE CEDULA = ?")) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Guarda la factura (creando el cliente si hace falta) y devuelve su numero. */
    public int guardarFactura(String cedula, String nombre, int codTelefono, String formaPago, BigDecimal precioLista,
            BigDecimal descuento, BigDecimal total, Integer numCuotas, Integer codCredito, LocalDateTime fecha)
            throws SQLException {
        try (Connection cn = fuente.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                int codCliente = clienteDe(cn, cedula, nombre);
                int num;
                try (PreparedStatement ps = cn.prepareStatement("INSERT INTO FACTURA (FECHA, COD_CLIENTE, COD_TELEFONO, "
                        + "FORMA_PAGO, PRECIO_LISTA, DESCUENTO, TOTAL, NUM_CUOTAS, COD_CREDITO) VALUES (?,?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, Timestamp.valueOf(fecha));
                    ps.setInt(2, codCliente);
                    ps.setInt(3, codTelefono);
                    ps.setString(4, formaPago);
                    ps.setBigDecimal(5, precioLista);
                    ps.setBigDecimal(6, descuento);
                    ps.setBigDecimal(7, total);
                    ps.setObject(8, numCuotas);
                    ps.setObject(9, codCredito);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        num = rs.getInt(1);
                    }
                }
                cn.commit();
                return num;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public Optional<Factura> buscarFactura(int numFactura) throws SQLException {
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SELECT_FACTURA + "WHERE f.NUM_FACTURA = ?")) {
            ps.setInt(1, numFactura);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(factura(rs)) : Optional.empty();
            }
        }
    }

    public List<Factura> facturasDe(String cedula) throws SQLException {
        List<Factura> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(
                SELECT_FACTURA + "WHERE c.CEDULA = ? ORDER BY f.NUM_FACTURA")) {
            ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(factura(rs));
                }
            }
        }
        return lista;
    }

    private static Factura factura(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setNumFactura(rs.getInt("NUM_FACTURA"));
        f.setFecha(rs.getTimestamp("FECHA").toLocalDateTime().withNano(0).toString().replace('T', ' '));
        f.setCedula(rs.getString("CEDULA"));
        f.setNombreCliente(rs.getString("NOMBRE"));
        f.setCodTelefono(rs.getInt("COD_TELEFONO"));
        f.setTelefono(rs.getString("MARCA") + " " + rs.getString("MODELO"));
        f.setFormaPago(rs.getString("FORMA_PAGO"));
        f.setPrecioLista(rs.getBigDecimal("PRECIO_LISTA").doubleValue());
        f.setDescuento(rs.getBigDecimal("DESCUENTO").doubleValue());
        f.setTotal(rs.getBigDecimal("TOTAL").doubleValue());
        f.setNumCuotas(rs.getInt("NUM_CUOTAS"));
        f.setCodCredito(rs.getInt("COD_CREDITO"));
        return f;
    }
}
