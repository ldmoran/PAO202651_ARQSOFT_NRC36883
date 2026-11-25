package ec.edu.monster.eureka.datos;

import ec.edu.monster.eureka.modelos.DatosCuenta;
import ec.edu.monster.eureka.modelos.MovimientoData;
import ec.edu.monster.eureka.modelos.ReglaNegocio;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/** Todo el SQL de EurekaBank. Las operaciones que mueven dinero corren en una sola transaccion. */
public class EurekaRepositorio {

    /** Credenciales guardadas de un usuario (la clave nunca se guarda: solo su hash con sal). */
    public record Credencial(String usuario, String codEmpleado, String sal, String claveHash, String estado) {
    }

    private record Paso(String cuenta, String tipo, boolean ingreso, String referencia) {
    }

    private final FuenteDatos fuente;

    public EurekaRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    public Credencial buscarUsuario(String usuario) throws SQLException {
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT usuario, cod_empleado, sal, clave_hash, estado FROM usuario WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new Credencial(rs.getString(1), rs.getString(2).trim(), rs.getString(3).trim(), rs.getString(4).trim(), rs.getString(5))
                        : null;
            }
        }
    }

    public void registrarIntento(String usuario, boolean exitoso) {
        try (Connection cn = fuente.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement("INSERT INTO intento_acceso (usuario, exitoso) VALUES (?, ?)")) {
            ps.setString(1, usuario);
            ps.setBoolean(2, exitoso);
            ps.executeUpdate();
        } catch (SQLException e) {
            // el registro de intentos no debe impedir el inicio de sesion
        }
    }

    public List<DatosCuenta> cuentasConClientes() throws SQLException {
        String sql = "SELECT c.cod_cuenta, m.descripcion, c.saldo, c.estado, cl.apellido_paterno, cl.apellido_materno, "
                + "cl.nombre, cl.email, cl.telefono FROM cuenta c JOIN cliente cl ON cl.cod_cliente = c.cod_cliente "
                + "JOIN moneda m ON m.cod_moneda = c.cod_moneda WHERE c.estado = 'ACTIVO' ORDER BY c.cod_cuenta";
        List<DatosCuenta> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DatosCuenta d = new DatosCuenta();
                d.setCodigo(rs.getString(1).trim());
                d.setMoneda(rs.getString(2));
                d.setSaldo(rs.getBigDecimal(3));
                d.setEstado(rs.getString(4));
                d.setNombreCliente((rs.getString(5) + " " + rs.getString(6) + ", " + rs.getString(7)).trim());
                d.setEmailCliente(rs.getString(8));
                d.setTelefonoCliente(rs.getString(9));
                lista.add(d);
            }
        }
        return lista;
    }

    public List<MovimientoData> movimientos(String cuenta) throws SQLException {
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
                    m.setCodigoCuenta(rs.getString(1).trim());
                    m.setNumero(rs.getInt(2));
                    m.setFecha(rs.getDate(3).toLocalDate().toString());
                    m.setTipo(rs.getString(4) + ("INGRESO".equals(rs.getString(5)) ? " Entrada" : " Salida"));
                    m.setImporte(rs.getBigDecimal(6));
                    m.setSaldoActual(rs.getBigDecimal(7));
                    String ref = rs.getString(8);
                    m.setReferencia(ref == null ? null : ref.trim());
                    m.setNombreCliente((rs.getString(9) + " " + rs.getString(10) + ", " + rs.getString(11)).trim());
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    public void depositar(String cuenta, BigDecimal importe, String empleado) throws SQLException {
        mover(List.of(new Paso(cuenta, "003", true, null)), importe, empleado);
    }

    public void retirar(String cuenta, BigDecimal importe, String empleado) throws SQLException {
        mover(List.of(new Paso(cuenta, "004", false, null)), importe, empleado);
    }

    public void transferir(String origen, String destino, BigDecimal importe, String empleado) throws SQLException {
        if (origen.equals(destino)) {
            throw new ReglaNegocio("No se puede transferir a la misma cuenta.");
        }
        mover(List.of(new Paso(origen, "009", false, destino), new Paso(destino, "008", true, origen)), importe, empleado);
    }

    /** Aplica todos los pasos o ninguno. */
    private void mover(List<Paso> pasos, BigDecimal importe, String empleado) throws SQLException {
        try (Connection cn = fuente.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM usuario WHERE cod_empleado = ? AND estado = 'ACTIVO'")) {
                    ps.setString(1, empleado);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocio("El empleado " + empleado + " no existe o no esta activo.");
                        }
                    }
                }
                for (Paso p : pasos) {
                    aplicar(cn, p, importe, empleado);
                }
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    private static void aplicar(Connection cn, Paso p, BigDecimal importe, String empleado) throws SQLException {
        BigDecimal saldo;
        int contador;
        try (PreparedStatement ps = cn.prepareStatement("SELECT saldo, contador_mov, estado FROM cuenta WHERE cod_cuenta = ? FOR UPDATE")) {
            ps.setString(1, p.cuenta());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ReglaNegocio("La cuenta " + p.cuenta() + " no existe.");
                }
                if (!"ACTIVO".equals(rs.getString(3))) {
                    throw new ReglaNegocio("La cuenta " + p.cuenta() + " no esta activa.");
                }
                saldo = rs.getBigDecimal(1);
                contador = rs.getInt(2);
            }
        }
        if (!p.ingreso() && saldo.compareTo(importe) < 0) {
            throw new ReglaNegocio("Saldo insuficiente en la cuenta " + p.cuenta() + ".");
        }
        BigDecimal nuevo = p.ingreso() ? saldo.add(importe) : saldo.subtract(importe);
        try (PreparedStatement ps = cn.prepareStatement("UPDATE cuenta SET saldo = ?, contador_mov = ? WHERE cod_cuenta = ?")) {
            ps.setBigDecimal(1, nuevo);
            ps.setInt(2, contador + 1);
            ps.setString(3, p.cuenta());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = cn.prepareStatement("INSERT INTO movimiento (cod_cuenta, numero, fecha, cod_empleado, cod_tipo, "
                + "importe, saldo_resultante, cuenta_referencia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, p.cuenta());
            ps.setInt(2, contador + 1);
            ps.setDate(3, Date.valueOf(LocalDate.now(Datos.reloj())));
            ps.setString(4, empleado);
            ps.setString(5, p.tipo());
            ps.setBigDecimal(6, importe);
            ps.setBigDecimal(7, nuevo);
            ps.setString(8, p.referencia());
            ps.executeUpdate();
        }
    }
}
