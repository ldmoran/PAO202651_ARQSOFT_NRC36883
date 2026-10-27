package ec.edu.monster.servicios;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Contrato de un conversor de unidades. Cada categoria (longitud, masa,
 * temperatura, y en el futuro otras categorias) implementa esta interfaz y se registra
 * en {@link CatalogoUnidades}; el resto del sistema no cambia.
 */
public interface Conversor {

    /** Nombre de la categoria, por ejemplo "Longitud". */
    String categoria();

    /** Unidades soportadas, en minusculas y sin tildes. */
    List<String> unidades();

    /**
     * Convierte un valor. Las unidades ya fueron validadas contra
     * {@link #unidades()} y vienen normalizadas.
     */
    double convertir(double valor, String origen, String destino);

    /** Pasa a minusculas, sin tildes y sin espacios sobrantes. */
    static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }
        String s = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
