package ec.edu.monster.eureka.servicios;

import ec.edu.monster.eureka.modelos.DatosCuenta;
import ec.edu.monster.eureka.modelos.MovimientoData;
import ec.edu.monster.eureka.modelos.ResultadoOperacion;
import ec.edu.monster.eureka.repositorios.EurekaRepositorio;
import ec.edu.monster.eureka.repositorios.EurekaRepositorio.CredencialUsuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/** Reglas de EurekaBank: validacion de datos, inicio de sesion y operaciones de dinero. */
public class ServicioEureka {

    public static final List<String> TIPOS_PERMITIDOS = List.of("DEPOSITO", "RETIRO", "TRANSFERENCIA");
    /** Empleado que registra las operaciones hechas desde los clientes (cajero por defecto). */
    public static final String EMPLEADO_POR_DEFECTO = "0001";

    private final EurekaRepositorio repo;

    public ServicioEureka(EurekaRepositorio repo) {
        this.repo = repo;
    }

    public boolean iniciarSesion(String usuario, String contrasena) {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isEmpty()) {
            return false;
        }
        Optional<CredencialUsuario> u = repo.buscarUsuario(usuario.trim());
        boolean ok = u.isPresent() && "ACTIVO".equals(u.get().estado())
                && Claves.coincide(u.get().sal(), contrasena, u.get().claveHash());
        repo.registrarIntento(usuario.trim(), ok);
        return ok;
    }

    public List<DatosCuenta> cuentas() {
        return repo.cuentasConClientes();
    }

    public List<MovimientoData> movimientos(String cuenta) {
        return repo.movimientos(cuenta);
    }

    public List<String> tiposPermitidos() {
        return TIPOS_PERMITIDOS;
    }

    public ResultadoOperacion registrar(String origen, String destino, String tipo, double importe) {
        if (origen == null || origen.isBlank()) {
            return ResultadoOperacion.error("Cuenta origen es requerida");
        }
        if (Double.isNaN(importe) || importe <= 0) {
            return ResultadoOperacion.error("El importe debe ser mayor a 0");
        }
        if (tipo == null || tipo.isBlank()) {
            return ResultadoOperacion.error("Tipo de movimiento es requerido");
        }
        BigDecimal monto = BigDecimal.valueOf(importe).setScale(2, RoundingMode.HALF_UP);
        if (monto.signum() <= 0) {
            return ResultadoOperacion.error("El importe debe ser mayor a 0");
        }
        String cuentaOrigen = origen.trim();
        boolean hayDestino = destino != null && !destino.isBlank();
        try {
            switch (tipo.trim().toUpperCase()) {
                case "DEPOSITO":
                    if (hayDestino) {
                        return ResultadoOperacion.error("Depósito no debe tener cuenta destino");
                    }
                    repo.depositar(cuentaOrigen, monto, EMPLEADO_POR_DEFECTO);
                    break;
                case "RETIRO":
                    if (hayDestino) {
                        return ResultadoOperacion.error("Retiro no debe tener cuenta destino");
                    }
                    repo.retirar(cuentaOrigen, monto, EMPLEADO_POR_DEFECTO);
                    break;
                case "TRANSFERENCIA":
                    if (!hayDestino) {
                        return ResultadoOperacion.error("Cuenta destino es requerida para transferencia");
                    }
                    repo.transferir(cuentaOrigen, destino.trim(), monto, EMPLEADO_POR_DEFECTO);
                    break;
                default:
                    return ResultadoOperacion.error("Tipo de movimiento no válido: " + tipo);
            }
            return ResultadoOperacion.exito();
        } catch (EurekaRepositorio.ReglaNegocio e) {
            return ResultadoOperacion.error(e.getMessage());
        } catch (RuntimeException e) {
            return ResultadoOperacion.error("Error interno del servidor");
        }
    }
}
