package ec.edu.monster.banco.servicios;

import ec.edu.monster.banco.modelos.Cliente;
import ec.edu.monster.banco.modelos.CuotaAmortizacion;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/** Reglas de negocio del credito. No toca la base de datos ni el reloj: todo entra por parametro. */
public final class ReglasCredito {

    public static final BigDecimal TASA_ANUAL = new BigDecimal("0.165");
    /** Porcentaje del flujo mensual (depositos - retiros) que se puede destinar a cuota. */
    public static final BigDecimal PORCENTAJE_CAPACIDAD = new BigDecimal("0.30");
    /** El monto maximo es la capacidad mensual por 6 meses. */
    public static final int MESES_MONTO_MAXIMO = 6;
    public static final int PLAZO_MINIMO = 3;
    public static final int PLAZO_MAXIMO = 18;
    public static final int EDAD_MINIMA_HOMBRE = 25;

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    private ReglasCredito() {
    }

    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static int edad(LocalDate nacimiento, LocalDate hoy) {
        return Period.between(nacimiento, hoy).getYears();
    }

    /** Un hombre menor de 25 anios no es sujeto de credito; una mujer lo es a cualquier edad. */
    public static boolean cumpleEdad(Cliente cliente, LocalDate hoy) {
        return !(cliente.esMasculino() && edad(cliente.getFechaNacimiento(), hoy) < EDAD_MINIMA_HOMBRE);
    }

    /** ((promedio depositos - promedio retiros) * 30%) * 6; nunca negativo. */
    public static BigDecimal montoMaximo(BigDecimal promedioDepositos, BigDecimal promedioRetiros) {
        BigDecimal flujo = promedioDepositos.subtract(promedioRetiros);
        if (flujo.signum() <= 0) {
            return redondear(BigDecimal.ZERO);
        }
        return redondear(flujo.multiply(PORCENTAJE_CAPACIDAD).multiply(BigDecimal.valueOf(MESES_MONTO_MAXIMO)));
    }

    public static boolean plazoValido(int plazoMeses) {
        return plazoMeses >= PLAZO_MINIMO && plazoMeses <= PLAZO_MAXIMO;
    }

    /** cuota = P * i / (1 - (1+i)^-n), con i = tasa anual / 12, redondeada a centavos. */
    public static BigDecimal cuotaFija(BigDecimal monto, int plazoMeses) {
        BigDecimal i = TASA_ANUAL.divide(BigDecimal.valueOf(12), MC);
        BigDecimal factor = BigDecimal.ONE.add(i).pow(plazoMeses, MC);
        BigDecimal denominador = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(factor, MC));
        return redondear(monto.multiply(i).divide(denominador, MC));
    }

    /**
     * Tabla de amortizacion con cuota fija. La fila 0 es el saldo inicial. Cada valor se redondea a
     * centavos y la ultima cuota absorbe la diferencia de redondeo para que el saldo final sea 0.00.
     */
    public static List<CuotaAmortizacion> tabla(BigDecimal monto, int plazoMeses, LocalDate inicio) {
        BigDecimal i = TASA_ANUAL.divide(BigDecimal.valueOf(12), MC);
        BigDecimal cuota = cuotaFija(monto, plazoMeses);
        List<CuotaAmortizacion> filas = new ArrayList<>();
        BigDecimal saldo = redondear(monto);
        filas.add(new CuotaAmortizacion(0, inicio.toString(), 0, 0, 0, saldo.doubleValue()));
        for (int k = 1; k <= plazoMeses; k++) {
            BigDecimal interes = redondear(saldo.multiply(i));
            BigDecimal capital;
            BigDecimal valor;
            if (k == plazoMeses) {
                capital = saldo;
                valor = redondear(capital.add(interes));
            } else {
                capital = redondear(cuota.subtract(interes));
                valor = cuota;
            }
            saldo = redondear(saldo.subtract(capital));
            filas.add(new CuotaAmortizacion(k, inicio.plusMonths(k).toString(), valor.doubleValue(),
                    interes.doubleValue(), capital.doubleValue(), saldo.doubleValue()));
        }
        return filas;
    }
}
