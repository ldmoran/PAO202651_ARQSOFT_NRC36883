package ec.edu.monster.eureka.servicios;

import ec.edu.monster.eureka.datos.EurekaRepositorio;
import ec.edu.monster.eureka.modelos.DatosCuenta;
import ec.edu.monster.eureka.modelos.MovimientoData;
import ec.edu.monster.eureka.modelos.ReglaNegocio;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/** Reglas de EurekaBank: validacion de datos, inicio de sesion y operaciones de dinero. */
public class ServicioEureka {

    public static final List<String> TIPOS_PERMITIDOS = List.of("DEPOSITO", "RETIRO", "TRANSFERENCIA");
    public static final String EMPLEADO_POR_DEFECTO = "0001";

    private final EurekaRepositorio repo;

    public ServicioEureka(EurekaRepositorio repo) {
        this.repo = repo;
    }

    public boolean iniciarSesion(String usuario, String contrasena) throws SQLException {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isEmpty()) {
            return false;
        }
        String u = usuario.trim();
        EurekaRepositorio.Credencial c = repo.buscarUsuario(u);
        boolean ok = c != null && "ACTIVO".equals(c.estado()) && Claves.coincide(c.sal(), contrasena, c.claveHash());
        repo.registrarIntento(u, ok);
        return ok;
    }

    public List<DatosCuenta> cuentas() throws SQLException {
        return repo.cuentasConClientes();
    }

    public List<MovimientoData> movimientos(String cuenta) throws SQLException {
        return repo.movimientos(cuenta);
    }

    public ResultadoOperacion registrar(String origen, String destino, String tipo, Double importe) {
        if (origen == null || origen.isBlank()) {
            return ResultadoOperacion.invalido("Cuenta origen es requerida");
        }
        if (importe == null || importe.isNaN() || importe.isInfinite() || importe <= 0) {
            return ResultadoOperacion.invalido("El importe debe ser mayor a 0");
        }
        if (tipo == null || tipo.isBlank()) {
            return ResultadoOperacion.invalido("Tipo de movimiento es requerido");
        }
        BigDecimal monto = BigDecimal.valueOf(importe).setScale(2, RoundingMode.HALF_UP);
        if (monto.signum() <= 0) {
            return ResultadoOperacion.invalido("El importe debe ser mayor a 0");
        }
        String o = origen.trim();
        boolean hayDestino = destino != null && !destino.isBlank();
        try {
            switch (tipo.trim().toUpperCase()) {
                case "DEPOSITO" -> {
                    if (hayDestino) {
                        return ResultadoOperacion.invalido("Depósito no debe tener cuenta destino");
                    }
                    repo.depositar(o, monto, EMPLEADO_POR_DEFECTO);
                }
                case "RETIRO" -> {
                    if (hayDestino) {
                        return ResultadoOperacion.invalido("Retiro no debe tener cuenta destino");
                    }
                    repo.retirar(o, monto, EMPLEADO_POR_DEFECTO);
                }
                case "TRANSFERENCIA" -> {
                    if (!hayDestino) {
                        return ResultadoOperacion.invalido("Cuenta destino es requerida para transferencia");
                    }
                    repo.transferir(o, destino.trim(), monto, EMPLEADO_POR_DEFECTO);
                }
                default -> {
                    return ResultadoOperacion.invalido("Tipo de movimiento no válido: " + tipo);
                }
            }
            return ResultadoOperacion.exito();
        } catch (ReglaNegocio e) {
            return ResultadoOperacion.regla(e.getMessage());
        } catch (SQLException | RuntimeException e) {
            return ResultadoOperacion.errorInterno();
        }
    }
}
