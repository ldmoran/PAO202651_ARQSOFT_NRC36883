package ec.edu.monster.comercializadora.servicios;

import ec.edu.monster.comercializadora.banco.ClienteBanco;
import ec.edu.monster.comercializadora.banco.CuotaAmortizacion;
import ec.edu.monster.comercializadora.banco.DetalleMontoMaximo;
import ec.edu.monster.comercializadora.banco.EvaluacionCredito;
import ec.edu.monster.comercializadora.banco.ResultadoCredito;
import ec.edu.monster.comercializadora.modelos.Factura;
import ec.edu.monster.comercializadora.modelos.ResultadoFactura;
import ec.edu.monster.comercializadora.modelos.Telefono;
import ec.edu.monster.comercializadora.repositorios.ComercioRepositorio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Casos de uso de la Comercializadora. El descuento por pago en efectivo se calcula aqui, no en los clientes. */
public class ServicioComercial {

    /** 42 % de descuento al pagar en efectivo. */
    public static final BigDecimal DESCUENTO_EFECTIVO = new BigDecimal("0.42");

    private final ComercioRepositorio repositorio;
    private final ClienteBanco banco;
    private final Clock reloj;

    public ServicioComercial(ComercioRepositorio repositorio, ClienteBanco banco, Clock reloj) {
        this.repositorio = repositorio;
        this.banco = banco;
        this.reloj = reloj;
    }

    public static class ErrorServicio extends RuntimeException {
        public ErrorServicio(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    private static BigDecimal dinero(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    // ---------- Catalogo ----------

    public List<Telefono> listarTelefonos() {
        try {
            return repositorio.listarTelefonos();
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo listar el catalogo", e);
        }
    }

    public Telefono buscarTelefono(int cod) {
        try {
            return repositorio.buscarTelefono(cod).orElse(null);
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo buscar el telefono", e);
        }
    }

    /** Devuelve el codigo nuevo, o 0 si los datos no son validos. */
    public int crearTelefono(String marca, String modelo, double precio) {
        if (vacio(marca) || vacio(modelo) || precio <= 0) {
            return 0;
        }
        try {
            return repositorio.crearTelefono(marca.trim(), modelo.trim(), dinero(precio));
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo crear el telefono", e);
        }
    }

    public boolean actualizarTelefono(int cod, String marca, String modelo, double precio, boolean disponible) {
        if (vacio(marca) || vacio(modelo) || precio <= 0) {
            return false;
        }
        try {
            return repositorio.actualizarTelefono(cod, marca.trim(), modelo.trim(), dinero(precio), disponible);
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo actualizar el telefono", e);
        }
    }

    /**
     * Un telefono ya facturado no se borra (se perderia el historial): se marca como no disponible.
     *
     * @return true si el telefono existia
     */
    public boolean eliminarTelefono(int cod) {
        try {
            Optional<Telefono> t = repositorio.buscarTelefono(cod);
            if (t.isEmpty()) {
                return false;
            }
            if (repositorio.tieneFacturas(cod)) {
                Telefono x = t.get();
                return repositorio.actualizarTelefono(cod, x.getMarca(), x.getModelo(), dinero(x.getPrecio()), false);
            }
            return repositorio.eliminarTelefono(cod);
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo eliminar el telefono", e);
        }
    }

    // ---------- Consultas al banco ----------

    public EvaluacionCredito verificarSujetoCredito(String cedula) {
        return banco.verificarSujetoCredito(cedula);
    }

    public DetalleMontoMaximo calcularMontoMaximo(String cedula) {
        return banco.calcularMontoMaximo(cedula);
    }

    public List<CuotaAmortizacion> obtenerAmortizacion(int codCredito) {
        return banco.obtenerTablaAmortizacion(codCredito);
    }

    // ---------- Facturacion ----------

    public ResultadoFactura facturarEfectivo(String cedula, String nombre, int codTelefono) {
        ResultadoFactura invalido = validar(cedula, nombre, codTelefono);
        if (invalido != null) {
            return invalido;
        }
        try {
            Telefono t = repositorio.buscarTelefono(codTelefono).get();
            BigDecimal precio = dinero(t.getPrecio());
            BigDecimal descuento = precio.multiply(DESCUENTO_EFECTIVO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = precio.subtract(descuento);
            int num = repositorio.guardarFactura(cedula.trim(), nombre == null ? "" : nombre.trim(), codTelefono,
                    "EFECTIVO", precio, descuento, total, null, null, LocalDateTime.now(reloj));
            return resultado(num, "Factura emitida. Se aplico el 42 % de descuento por pago en efectivo.");
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo emitir la factura", e);
        }
    }

    public ResultadoFactura facturarCredito(String cedula, String nombre, int codTelefono, int plazoMeses) {
        ResultadoFactura invalido = validar(cedula, nombre, codTelefono);
        if (invalido != null) {
            return invalido;
        }
        try {
            Telefono t = repositorio.buscarTelefono(codTelefono).get();
            BigDecimal precio = dinero(t.getPrecio());
            ResultadoCredito credito;
            try {
                credito = banco.otorgarCredito(cedula.trim(), precio.doubleValue(), plazoMeses);
            } catch (ClienteBanco.BancoNoDisponible e) {
                return new ResultadoFactura(false, "No se pudo consultar al banco: " + e.getMessage(), null);
            }
            if (!credito.isAprobado()) {
                return new ResultadoFactura(false, credito.getMensaje(), null);
            }
            try {
                int num = repositorio.guardarFactura(cedula.trim(), nombre == null ? "" : nombre.trim(), codTelefono,
                        "CREDITO", precio, BigDecimal.ZERO.setScale(2), precio, plazoMeses, credito.getCodCredito(),
                        LocalDateTime.now(reloj));
                return resultado(num, "Factura emitida. Credito " + credito.getCodCredito() + " aprobado por el banco.");
            } catch (SQLException e) {
                // El banco ya registro el credito; hay que avisar su codigo para regularizarlo.
                throw new ErrorServicio("El banco aprobo el credito " + credito.getCodCredito()
                        + " pero no se pudo guardar la factura", e);
            }
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo emitir la factura", e);
        }
    }

    /** Comprueba cedula, nombre (si el cliente es nuevo) y telefono; devuelve null si todo esta bien. */
    private ResultadoFactura validar(String cedula, String nombre, int codTelefono) {
        if (vacio(cedula) || !cedula.trim().matches("\\d{10}")) {
            return new ResultadoFactura(false, "La cedula debe tener 10 digitos.", null);
        }
        try {
            if (vacio(nombre) && !repositorio.existeCliente(cedula.trim())) {
                return new ResultadoFactura(false, "Ingrese el nombre del cliente.", null);
            }
            Optional<Telefono> t = repositorio.buscarTelefono(codTelefono);
            if (t.isEmpty()) {
                return new ResultadoFactura(false, "El telefono no existe.", null);
            }
            if (!t.get().isDisponible()) {
                return new ResultadoFactura(false, "El telefono no esta disponible.", null);
            }
            return null;
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo validar la venta", e);
        }
    }

    private ResultadoFactura resultado(int numFactura, String mensaje) throws SQLException {
        return new ResultadoFactura(true, mensaje, repositorio.buscarFactura(numFactura).orElse(null));
    }

    public List<Factura> consultarFacturas(String cedula) {
        try {
            return repositorio.facturasDe(cedula == null ? "" : cedula.trim());
        } catch (SQLException e) {
            throw new ErrorServicio("No se pudo consultar las facturas", e);
        }
    }
}
