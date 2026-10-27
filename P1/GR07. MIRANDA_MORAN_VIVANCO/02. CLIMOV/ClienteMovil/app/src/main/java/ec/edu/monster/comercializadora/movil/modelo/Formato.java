package ec.edu.monster.comercializadora.movil.modelo;


import java.util.List;
import java.util.Locale;

/** Formato de dinero y tablas, igual en todos los clientes. */
public final class Formato {

    private Formato() {
    }

    public static String dinero(double v) {
        return String.format(Locale.US, "$%,.2f", v);
    }

    /** Tabla de amortizacion en texto plano (consola y pruebas). */
    public static String tablaTexto(List<CuotaAmortizacion> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%-7s %-12s %10s %10s %10s %10s%n", "Cuota", "Fecha", "Valor", "Interes",
                "Capital", "Saldo"));
        for (CuotaAmortizacion c : filas) {
            sb.append(String.format(Locale.US, "%-7d %-12s %10.2f %10.2f %10.2f %10.2f%n", c.getNumCuota(),
                    c.getFechaPago(), c.getValorCuota(), c.getInteresPagado(), c.getCapitalPagado(), c.getSaldo()));
        }
        return sb.toString();
    }
}
