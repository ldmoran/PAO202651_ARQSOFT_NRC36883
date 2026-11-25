package ec.edu.monster.clienteweb.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Un movimiento de una cuenta (GET /api/movimientos). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Movimiento(String codigoCuenta, int numero, String fecha, String tipo, BigDecimal importe,
        String referencia, BigDecimal saldoActual, String nombreCliente) {
}
