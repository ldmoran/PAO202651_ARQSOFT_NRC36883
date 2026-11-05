package ec.edu.monster.eureka.repositorios;

import ec.edu.monster.eureka.db.FuenteDatos;
import ec.edu.monster.eureka.modelos.DatosCuenta;
import ec.edu.monster.eureka.modelos.MovimientoData;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Todo el SQL de EurekaBank. Las operaciones que mueven dinero corren en una sola transaccion. */
public class EurekaRepositorio {

    /** Error de regla de negocio (saldo insuficiente, cuenta inexistente...). Su mensaje se muestra al usuario. */
    public static class ReglaNegocio extends RuntimeException {
        public ReglaNegocio(String mensaje) {
            super(mensaje);
        }
    }

    /** Usuario con su sal y el hash guardado. */
    public record CredencialUsuario(String usuario, String codEmpleado, String sal, String claveHash, String estado) {
    }

    private final FuenteDatos fuente;

    public EurekaRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    public Optional<CredencialUsuario> buscarUsuario(String usuario) {
        String sql = "SELECT usuario, cod_empleado, sal, clave_hash, estado FROM usuario WHERE usuario = ?";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CredencialUsuario(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el usuario: " + e.getMessage(), e);
        }
    }

    public void registrarIntento(String usuario, boolean exitoso) {
        String sql = "INSERT INTO intento_acceso (usuario, exitoso) VALUES (?, ?)";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, exitoso ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            // el registro de intentos no debe impedir el inicio de sesion
        }
    }

    public List<DatosCuenta> cuentasConClientes() {
        String sql = "SELECT c.cod_cuenta, m.descripcion AS moneda, c.saldo, c.estado, cl.apellido_paterno, "
                + "cl.apellido_materno, cl.nombre, cl.email, cl.telefono FROM cuenta c "
                + "JOIN cliente cl ON cl.cod_cliente = c.cod_cliente JOIN moneda m ON m.cod_moneda = c.cod_moneda "
                + "WHERE c.estado = 'ACTIVO' ORDER BY c.cod_cuenta";
        List<DatosCuenta> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DatosCuenta d = new DatosCuenta();
                d.setCodigo(rs.getString("cod_cuenta"));
                d.setMoneda(rs.getString("moneda"));
                d.setSaldo(rs.getBigDecimal("saldo"));
                d.setEstado(rs.getString("estado"));
                d.setNombreCliente((rs.getString("apellido_paterno") + " " + rs.getString("apellido_materno") + ", "
                        + rs.getString("nombre")).trim());
                d.setEmailCliente(rs.getString("email"));
                d.setTelefonoCliente(rs.getString("telefono"));
                lista.add(d);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron leer las cuentas: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Movimientos de una cuenta (o de todas si cuenta es vacia), del mas reciente al mas antiguo. */
    public List<MovimientoData> movimientos(String cuenta) {
        boolean filtrar = cuenta != null && !cuenta.isBlank();
        String sql = "SELECT mv.cod_cuenta, mv.numero, mv.fecha, t.descripcion, t.accion, mv.importe, mv.saldo_resultante, "
                + "mv.cuenta_referencia, cl.apellido_paterno, cl.apellido_materno, cl.nombre FROM movimiento mv "
                + "JOIN tipo_movimiento t ON t.cod_tipo = mv.cod_tipo JOIN cuenta c ON c.cod_cuenta = mv.cod_cuenta "
                + "JOIN cliente cl ON cl.cod_cliente = c.cod_cliente " + (filtrar ? "WHERE mv.cod_cuenta = ? " : "")
                + "ORDER BY mv.fecha DESC, mv.cod_cuenta, mv.numero DESC";
        List<MovimientoData> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            if (filtrar) {
                ps.setString(1, cuenta.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MovimientoData m = new MovimientoData();
                    m.setCodigoCuenta(rs.getString("cod_cuenta"));
                    m.setNumero(rs.getInt("numero"));
                    m.setFecha(rs.getDate("fecha").toString());
                    m.setTipo(rs.getString("descripcion") + ("INGRESO".equals(rs.getString("accion")) ? " Entrada" : " Salida"));
                    m.setImporte(rs.getBigDecimal("importe"));
                    m.setSaldoActual(rs.getBigDecimal("saldo_resultante"));
                    String ref = rs.getString("cuenta_referencia");
                    m.setReferencia(ref == null || ref.isBlank() ? null : ref.trim());
                    m.setNombreCliente((rs.getString("apellido_paterno") + " " + rs.getString("apellido_materno") + ", "
                            + rs.getString("nombre")).trim());
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron leer los movimientos: " + e.getMessage(), e);
        }
        return lista;
    }

    public void depositar(String cuenta, BigDecimal importe, String empleado) {
        mover(List.of(new Paso(cuenta, "003", true, null)), importe, empleado);
    }

    public void retirar(String cuenta, BigDecimal importe, String empleado) {
        mover(List.of(new Paso(cuenta, "004", false, null)), importe, empleado);
    }

    public void transferir(String origen, String destino, BigDecimal importe, String empleado) {
        if (origen.equals(destino)) {
            throw new ReglaNegocio("No se puede transferir a la misma cuenta.");
        }
        mover(List.of(new Paso(origen, "009", false, destino), new Paso(destino, "008", true, origen)), importe, empleado);
    }

    private record Paso(String cuenta, String tipo, boolean ingreso, String referencia) {
    }

    /** Aplica todos los pasos o ninguno: si uno falla se deshace todo. */
    private void mover(List<Paso> pasos, BigDecimal importe, String empleado) {
        try (Connection cn = fuente.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                verificarEmpleado(cn, empleado);
                for (Paso p : pasos) {
                    aplicar(cn, p, importe, empleado);
                }
                cn.commit();
            } catch (RuntimeException | SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    private void verificarEmpleado(Connection cn, String empleado) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT 1 FROM usuario WHERE cod_empleado = ? AND estado = 'ACTIVO'")) {
            ps.setString(1, empleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocio("El empleado " + empleado + " no existe o no esta activo.");
                }
            }
        }
    }

    private void aplicar(Connection cn, Paso p, BigDecimal importe, String empleado) throws SQLException {
        BigDecimal saldo;
        int contador;
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT saldo, contador_mov, estado FROM cuenta WHERE cod_cuenta = ? FOR UPDATE")) {
            ps.setString(1, p.cuenta());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocio("La cuenta " + p.cuenta() + " no existe.");
                }
                if (!"ACTIVO".equals(rs.getString("estado"))) {
                    throw new ReglaNegocio("La cuenta " + p.cuenta() + " no esta activa.");
                }
                saldo = rs.getBigDecimal("saldo");
                contador = rs.getInt("contador_mov");
            }
        }
        if (!p.ingreso() && saldo.compareTo(importe) < 0) {
            throw new ReglaNegocio("Saldo insuficiente en la cuenta " + p.cuenta() + ".");
        }
        BigDecimal nuevo = p.ingreso() ? saldo.add(importe) : saldo.subtract(importe);
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE cuenta SET saldo = ?, contador_mov = ? WHERE cod_cuenta = ?")) {
            ps.setBigDecimal(1, nuevo);
            ps.setInt(2, contador + 1);
            ps.setString(3, p.cuenta());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO movimiento (cod_cuenta, numero, fecha, cod_empleado, "
                + "cod_tipo, importe, saldo_resultante, cuenta_referencia) VALUES (?, ?, CURRENT_DATE, ?, ?, ?, ?, ?)")) {
            ps.setString(1, p.cuenta());
            ps.setInt(2, contador + 1);
            ps.setString(3, empleado);
            ps.setString(4, p.tipo());
            ps.setBigDecimal(5, importe);
            ps.setBigDecimal(6, nuevo);
            ps.setString(7, p.referencia());
            ps.executeUpdate();
        }
    }
}
