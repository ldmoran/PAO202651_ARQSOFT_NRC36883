package ec.edu.monster.servicios;

import ec.edu.monster.modelos.entidades.Unidad;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Convierte entre dos unidades de una misma categoria usando los datos de la tabla UNIDAD.
 * Pasa el valor a la unidad base de la categoria y de ahi a la unidad de destino:
 * base = (valor + desplazamiento) x factor, y destino = base / factor - desplazamiento.
 * Longitud y masa tienen desplazamiento 0; la temperatura usa el celsius como base.
 */
public final class Conversor {

    private Conversor() {
    }

    public static double convertir(double valor, Unidad origen, Unidad destino) {
        double base = (valor + origen.desplazamiento()) * origen.factor();
        return base / destino.factor() - destino.desplazamiento();
    }

    /** Pasa a minusculas, sin tildes y sin espacios sobrantes. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String s = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
