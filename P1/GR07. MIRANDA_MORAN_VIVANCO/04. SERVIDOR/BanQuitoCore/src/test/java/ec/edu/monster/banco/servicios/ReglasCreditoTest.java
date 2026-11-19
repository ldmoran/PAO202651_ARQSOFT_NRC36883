package ec.edu.monster.banco.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.banco.modelos.Cliente;
import ec.edu.monster.banco.modelos.CuotaAmortizacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReglasCreditoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 6, 15);

    private static Cliente cliente(String genero, LocalDate nacimiento) {
        return new Cliente(1, "0101010101", "Prueba", genero, nacimiento);
    }

    @Test
    void hombreMenorDe25NoCumpleEdad() {
        assertFalse(ReglasCredito.cumpleEdad(cliente("M", LocalDate.of(2001, 6, 16)), HOY)); // 24 anios, cumple manana
    }

    @Test
    void hombreDe25ExactosSiCumpleEdad() {
        assertTrue(ReglasCredito.cumpleEdad(cliente("M", LocalDate.of(2001, 6, 15)), HOY));
    }

    @Test
    void mujerJovenSiCumpleEdad() {
        assertTrue(ReglasCredito.cumpleEdad(cliente("F", LocalDate.of(2007, 1, 1)), HOY));
    }

    @Test
    void montoMaximoEsFlujoPor30PorCientoPorSeis() {
        assertEquals(new BigDecimal("1080.00"),
                ReglasCredito.montoMaximo(new BigDecimal("1000"), new BigDecimal("400")));
    }

    @Test
    void montoMaximoNuncaEsNegativo() {
        assertEquals(new BigDecimal("0.00"), ReglasCredito.montoMaximo(new BigDecimal("100"), new BigDecimal("400")));
    }

    @Test
    void plazoDebeEstarEntre3y18() {
        assertFalse(ReglasCredito.plazoValido(2));
        assertTrue(ReglasCredito.plazoValido(3));
        assertTrue(ReglasCredito.plazoValido(18));
        assertFalse(ReglasCredito.plazoValido(19));
    }

    @Test
    void cuotaFijaDelEjemploDelExamen() {
        assertEquals(new BigDecimal("78.65"), ReglasCredito.cuotaFija(new BigDecimal("450"), 6));
    }

    @Test
    void tablaDelEjemploDelExamen() {
        List<CuotaAmortizacion> t = ReglasCredito.tabla(new BigDecimal("450"), 6, HOY);
        assertEquals(7, t.size());
        assertEquals(0, t.get(0).getNumCuota());
        assertEquals(450.00, t.get(0).getSaldo(), 0.0);
        assertEquals(6.19, t.get(1).getInteresPagado(), 0.0);
        assertEquals(72.46, t.get(1).getCapitalPagado(), 0.0);
        assertEquals(377.54, t.get(1).getSaldo(), 0.0);
        assertEquals(0.0, t.get(6).getSaldo(), 0.0);
        assertEquals("2026-07-15", t.get(1).getFechaPago());
    }

    @Test
    void ultimaCuotaDejaSaldoCeroParaCualquierMontoYPlazo() {
        for (int plazo = 3; plazo <= 18; plazo++) {
            for (String monto : new String[] {"100.00", "333.33", "1080.00", "2999.99"}) {
                List<CuotaAmortizacion> t = ReglasCredito.tabla(new BigDecimal(monto), plazo, HOY);
                assertEquals(plazo + 1, t.size());
                assertEquals(0.0, t.get(plazo).getSaldo(), 0.0, monto + " a " + plazo);
                double capital = t.stream().skip(1).mapToDouble(CuotaAmortizacion::getCapitalPagado).sum();
                assertEquals(Double.parseDouble(monto), capital, 0.005);
            }
        }
    }
}
