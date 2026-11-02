package ec.edu.monster.clienteweb.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Utilidades de presentacion de numeros. */
public final class Formato {

    private Formato() {
    }

    /** Numero sin notacion cientifica y sin ceros sobrantes. */
    public static String numero(double valor) {
        BigDecimal b = BigDecimal.valueOf(valor).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return b.scale() < 0 ? b.setScale(0).toPlainString() : b.toPlainString();
    }
}
