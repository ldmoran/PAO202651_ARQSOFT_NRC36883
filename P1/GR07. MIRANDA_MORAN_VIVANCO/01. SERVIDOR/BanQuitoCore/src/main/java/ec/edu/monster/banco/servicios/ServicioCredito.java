package ec.edu.monster.banco.servicios;

import ec.edu.monster.banco.modelos.Cliente;
import ec.edu.monster.banco.modelos.CuotaAmortizacion;
import ec.edu.monster.banco.modelos.DetalleMontoMaximo;
import ec.edu.monster.banco.modelos.EvaluacionCredito;
import ec.edu.monster.banco.modelos.ResultadoCredito;
import ec.edu.monster.banco.modelos.ResumenCredito;
import ec.edu.monster.banco.repositorios.CoreRepositorio;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Casos de uso del modulo de credito. El reloj se inyecta para poder probar con fechas fijas. */
public class ServicioCredito {

    private final CoreRepositorio repositorio;
    private final Clock reloj;

    public ServicioCredito(CoreRepositorio repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public static class ErrorServicio extends RuntimeException {
        public ErrorServicio(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    /** Verifica si el cliente es sujeto de credito. */
    public EvaluacionCredito evaluar(String cedula) {
        try {
            Optional<Cliente> encontrado = repositorio.buscarPorCedula(cedula == null ? "" : cedula.trim());
            if (encontrado.isEmpty()) {
                return new EvaluacionCredito(false, 0, "", "La cedula no corresponde a un cliente del banco.");
            }
            return evaluar(encontrado.get());
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo evaluar al cliente", e);
        }
    }

    private EvaluacionCredito evaluar(Cliente c) throws SQLException {
        LocalDate hoy = hoy();
        if (!ReglasCredito.cumpleEdad(c, hoy)) {
            return new EvaluacionCredito(false, c.getCodCliente(), c.getNombre(),
                    "Los clientes hombres menores de " + ReglasCredito.EDAD_MINIMA_HOMBRE + " anios no son sujetos de credito.");
        }
        if (!repositorio.tieneDepositoDesde(c.getCodCliente(), hoy.minusMonths(1))) {
            return new EvaluacionCredito(false, c.getCodCliente(), c.getNombre(),
                    "No registra ningun deposito en el ultimo mes.");
        }
        if (repositorio.tieneCreditoActivo(c.getCodCliente())) {
            return new EvaluacionCredito(false, c.getCodCliente(), c.getNombre(), "Ya tiene un credito activo.");
        }
        return new EvaluacionCredito(true, c.getCodCliente(), c.getNombre(), "Cliente sujeto de credito.");
    }

    /** Calcula el monto maximo; solo tiene sentido para un sujeto de credito. */
    public DetalleMontoMaximo montoMaximo(String cedula) {
        EvaluacionCredito ev = evaluar(cedula);
        DetalleMontoMaximo d = new DetalleMontoMaximo();
        d.setCodCliente(ev.getCodCliente());
        d.setNombre(ev.getNombre());
        if (!ev.isSujetoCredito()) {
            d.setMensaje("No es sujeto de credito: " + ev.getMotivo());
            return d;
        }
        try {
            LocalDate desde = hoy().minusMonths(3);
            BigDecimal dep = repositorio.promedioMovimientos(ev.getCodCliente(), "DEP", desde);
            BigDecimal ret = repositorio.promedioMovimientos(ev.getCodCliente(), "RET", desde);
            BigDecimal maximo = ReglasCredito.montoMaximo(dep, ret);
            d.setPromedioDepositos(ReglasCredito.redondear(dep).doubleValue());
            d.setPromedioRetiros(ReglasCredito.redondear(ret).doubleValue());
            d.setMontoMaximo(maximo.doubleValue());
            d.setMensaje(maximo.signum() > 0 ? "Monto maximo calculado."
                    : "Sus retiros superan sus depositos: el monto maximo es 0.");
            return d;
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo calcular el monto maximo", e);
        }
    }

    /** Aprueba y registra el credito si el cliente es sujeto, el plazo es valido y el monto no excede el maximo. */
    public ResultadoCredito otorgar(String cedula, double montoSolicitado, int plazoMeses) {
        ResultadoCredito r = new ResultadoCredito();
        r.setPlazoMeses(plazoMeses);
        BigDecimal monto = ReglasCredito.redondear(BigDecimal.valueOf(montoSolicitado));
        r.setMontoCredito(monto.doubleValue());
        if (monto.signum() <= 0) {
            r.setMensaje("El monto debe ser mayor a cero.");
            return r;
        }
        if (!ReglasCredito.plazoValido(plazoMeses)) {
            r.setMensaje("El plazo debe estar entre " + ReglasCredito.PLAZO_MINIMO + " y "
                    + ReglasCredito.PLAZO_MAXIMO + " meses.");
            return r;
        }
        EvaluacionCredito ev = evaluar(cedula);
        if (!ev.isSujetoCredito()) {
            r.setMensaje("Credito rechazado: " + ev.getMotivo());
            return r;
        }
        DetalleMontoMaximo detalle = montoMaximo(cedula);
        BigDecimal maximo = BigDecimal.valueOf(detalle.getMontoMaximo());
        r.setMontoMaximo(detalle.getMontoMaximo());
        if (monto.compareTo(maximo) > 0) {
            r.setMensaje("Credito rechazado: el monto solicitado supera el monto maximo de " + maximo + ".");
            return r;
        }
        try {
            LocalDate inicio = hoy();
            BigDecimal cuota = ReglasCredito.cuotaFija(monto, plazoMeses);
            List<CuotaAmortizacion> tabla = ReglasCredito.tabla(monto, plazoMeses, inicio);
            int cod = repositorio.crearCredito(ev.getCodCliente(), monto, plazoMeses, ReglasCredito.TASA_ANUAL,
                    cuota, inicio, tabla);
            r.setAprobado(true);
            r.setCodCredito(cod);
            r.setCuotaFija(cuota.doubleValue());
            r.setMensaje("Credito aprobado.");
            return r;
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo registrar el credito", e);
        }
    }

    /** Tabla de amortizacion de un credito guardado, con la fila 0 de saldo inicial; vacia si no existe. */
    public List<CuotaAmortizacion> tabla(int codCredito) {
        try {
            Optional<BigDecimal> monto = repositorio.montoDelCredito(codCredito);
            List<CuotaAmortizacion> filas = new ArrayList<>();
            if (monto.isEmpty()) {
                return filas;
            }
            List<CuotaAmortizacion> guardadas = repositorio.cuotasDelCredito(codCredito);
            String fecha0 = guardadas.isEmpty() ? hoy().toString()
                    : LocalDate.parse(guardadas.get(0).getFechaPago()).minusMonths(1).toString();
            filas.add(new CuotaAmortizacion(0, fecha0, 0, 0, 0, monto.get().doubleValue()));
            filas.addAll(guardadas);
            return filas;
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo consultar la tabla de amortizacion", e);
        }
    }

    public List<ResumenCredito> creditosDe(String cedula) {
        try {
            Optional<Cliente> c = repositorio.buscarPorCedula(cedula == null ? "" : cedula.trim());
            return c.isPresent() ? repositorio.creditosDelCliente(c.get().getCodCliente()) : new ArrayList<>();
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo consultar los creditos", e);
        }
    }
}
