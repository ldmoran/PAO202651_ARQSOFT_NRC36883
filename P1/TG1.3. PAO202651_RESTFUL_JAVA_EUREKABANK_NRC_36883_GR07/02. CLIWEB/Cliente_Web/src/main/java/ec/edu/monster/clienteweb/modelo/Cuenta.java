package ec.edu.monster.clienteweb.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Una cuenta activa con los datos de su cliente (GET /api/cuentas). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Cuenta(String codigo, String nombreCliente, BigDecimal saldo, String estado, String moneda,
        String emailCliente, String telefonoCliente) {
}
