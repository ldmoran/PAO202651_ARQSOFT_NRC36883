package ec.edu.monster.comercializadora.banco;

import java.util.List;

/** Lo que la Comercializadora necesita del Banco Core. Las pruebas usan una version falsa. */
public interface ClienteBanco {

    EvaluacionCredito verificarSujetoCredito(String cedula);

    DetalleMontoMaximo calcularMontoMaximo(String cedula);

    ResultadoCredito otorgarCredito(String cedula, double monto, int plazoMeses);

    List<CuotaAmortizacion> obtenerTablaAmortizacion(int codCredito);

    /** Se lanza cuando el banco no responde o responde con un error. */
    class BancoNoDisponible extends RuntimeException {
        public BancoNoDisponible(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
