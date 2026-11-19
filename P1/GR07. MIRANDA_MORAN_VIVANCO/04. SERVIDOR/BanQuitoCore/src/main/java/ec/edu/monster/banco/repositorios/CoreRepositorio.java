package ec.edu.monster.banco.repositorios;

import ec.edu.monster.banco.db.FuenteDatos;
import ec.edu.monster.banco.modelos.Cliente;
import ec.edu.monster.banco.modelos.CuotaAmortizacion;
import ec.edu.monster.banco.modelos.ResumenCredito;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos del Core (esquema banquito). Todas las fechas llegan como parametro:
 * asi las reglas no dependen de funciones propias de un motor y se pueden probar.
 */
public class CoreRepositorio {

    private final FuenteDatos fuente;

    public CoreRepositorio(FuenteDatos fuente) {
        this.fuente = fuente;
    }

    public Optional<Cliente> buscarPorCedula(String cedula) throws SQLException {
        return buscar("SELECT COD_CLIENTE, CEDULA, NOMBRE, GENERO, FECHA_NACIMIENTO FROM CLIENTE WHERE CEDULA = ?", cedula);
    }

    public Optional<Cliente> buscarPorCodigo(int codCliente) throws SQLException {
        return buscar("SELECT COD_CLIENTE, CEDULA, NOMBRE, GENERO, FECHA_NACIMIENTO FROM CLIENTE WHERE COD_CLIENTE = ?", codCliente);
    }

    private Optional<Cliente> buscar(String sql, Object parametro) throws SQLException {
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(rs.getInt("COD_CLIENTE"), rs.getString("CEDULA"),
                            rs.getString("NOMBRE"), rs.getString("GENERO"), rs.getDate("FECHA_NACIMIENTO").toLocalDate()));
                }
                return Optional.empty();
            }
        }
    }

    /** true si el cliente tiene al menos un deposito en alguna de sus cuentas desde la fecha indicada. */
    public boolean tieneDepositoDesde(int codCliente, LocalDate desde) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MOVIMIENTO m JOIN CUENTA c ON c.NUM_CUENTA = m.NUM_CUENTA "
                + "WHERE c.COD_CLIENTE = ? AND m.TIPO = 'DEP' AND m.FECHA >= ?";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            ps.setDate(2, Date.valueOf(desde));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean tieneCreditoActivo(int codCliente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CREDITO WHERE COD_CLIENTE = ? AND ESTADO = 'ACTIVO'";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Promedio de los movimientos de un tipo (DEP o RET) desde la fecha indicada; cero si no hay ninguno. */
    public BigDecimal promedioMovimientos(int codCliente, String tipo, LocalDate desde) throws SQLException {
        String sql = "SELECT AVG(m.VALOR) FROM MOVIMIENTO m JOIN CUENTA c ON c.NUM_CUENTA = m.NUM_CUENTA "
                + "WHERE c.COD_CLIENTE = ? AND m.TIPO = ? AND m.FECHA >= ?";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            ps.setString(2, tipo);
            ps.setDate(3, Date.valueOf(desde));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    return rs.getBigDecimal(1);
                }
                return BigDecimal.ZERO;
            }
        }
    }

    /**
     * Guarda el credito y su tabla de amortizacion en una sola transaccion: o se guarda todo o nada.
     *
     * @return codigo del credito creado
     */
    public int crearCredito(int codCliente, BigDecimal monto, int plazoMeses, BigDecimal tasaAnual, BigDecimal cuotaFija,
            LocalDate inicio, List<CuotaAmortizacion> cuotas) throws SQLException {
        String insertCredito = "INSERT INTO CREDITO (COD_CLIENTE, MONTO_CREDITO, PLAZO_MESES, TASA_ANUAL, CUOTA_FIJA, "
                + "FECHA_INICIO, FECHA_FIN, ESTADO, SALDO_PENDIENTE) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVO', ?)";
        String insertCuota = "INSERT INTO AMORTIZACION (COD_CREDITO, NUM_CUOTA, FECHA_PAGO, VALOR_CUOTA, INTERES_PAGADO, "
                + "CAPITAL_PAGADO, SALDO) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = fuente.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                int codCredito;
                try (PreparedStatement ps = cn.prepareStatement(insertCredito, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, codCliente);
                    ps.setBigDecimal(2, monto);
                    ps.setInt(3, plazoMeses);
                    ps.setBigDecimal(4, tasaAnual);
                    ps.setBigDecimal(5, cuotaFija);
                    ps.setDate(6, Date.valueOf(inicio));
                    ps.setDate(7, Date.valueOf(inicio.plusMonths(plazoMeses)));
                    ps.setBigDecimal(8, monto);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("No se obtuvo el codigo del credito");
                        }
                        codCredito = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(insertCuota)) {
                    for (CuotaAmortizacion c : cuotas) {
                        if (c.getNumCuota() == 0) {
                            continue; // la cuota 0 (saldo inicial) no se guarda
                        }
                        ps.setInt(1, codCredito);
                        ps.setInt(2, c.getNumCuota());
                        ps.setDate(3, Date.valueOf(c.getFechaPago()));
                        ps.setBigDecimal(4, BigDecimal.valueOf(c.getValorCuota()));
                        ps.setBigDecimal(5, BigDecimal.valueOf(c.getInteresPagado()));
                        ps.setBigDecimal(6, BigDecimal.valueOf(c.getCapitalPagado()));
                        ps.setBigDecimal(7, BigDecimal.valueOf(c.getSaldo()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                cn.commit();
                return codCredito;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public List<CuotaAmortizacion> cuotasDelCredito(int codCredito) throws SQLException {
        String sql = "SELECT NUM_CUOTA, FECHA_PAGO, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO "
                + "FROM AMORTIZACION WHERE COD_CREDITO = ? ORDER BY NUM_CUOTA";
        List<CuotaAmortizacion> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCredito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CuotaAmortizacion(rs.getInt("NUM_CUOTA"), rs.getDate("FECHA_PAGO").toString(),
                            rs.getBigDecimal("VALOR_CUOTA").doubleValue(), rs.getBigDecimal("INTERES_PAGADO").doubleValue(),
                            rs.getBigDecimal("CAPITAL_PAGADO").doubleValue(), rs.getBigDecimal("SALDO").doubleValue()));
                }
            }
        }
        return lista;
    }

    /** Monto inicial del credito, para armar la fila 0 de la tabla; vacio si el credito no existe. */
    public Optional<BigDecimal> montoDelCredito(int codCredito) throws SQLException {
        String sql = "SELECT MONTO_CREDITO FROM CREDITO WHERE COD_CREDITO = ?";
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCredito);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getBigDecimal(1)) : Optional.empty();
            }
        }
    }

    public List<ResumenCredito> creditosDelCliente(int codCliente) throws SQLException {
        String sql = "SELECT COD_CREDITO, MONTO_CREDITO, PLAZO_MESES, CUOTA_FIJA, FECHA_INICIO, ESTADO "
                + "FROM CREDITO WHERE COD_CLIENTE = ? ORDER BY COD_CREDITO";
        List<ResumenCredito> lista = new ArrayList<>();
        try (Connection cn = fuente.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResumenCredito r = new ResumenCredito();
                    r.setCodCredito(rs.getInt("COD_CREDITO"));
                    r.setMontoCredito(rs.getBigDecimal("MONTO_CREDITO").doubleValue());
                    r.setPlazoMeses(rs.getInt("PLAZO_MESES"));
                    r.setCuotaFija(rs.getBigDecimal("CUOTA_FIJA").doubleValue());
                    r.setFechaInicio(rs.getDate("FECHA_INICIO").toString());
                    r.setEstado(rs.getString("ESTADO"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }
}
