package ec.edu.monster.servicios;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Catalogo en memoria de categorias y unidades (CU-02 y CU-03). Para agregar una
 * categoria nueva (por ejemplo divisas) basta con implementar {@link Conversor}
 * y registrarla aqui; el login, la validacion y la respuesta estructurada no
 * cambian.
 */
public final class CatalogoUnidades {

    private static final List<Conversor> CONVERSORES = new CopyOnWriteArrayList<>();

    static {
        registrar(new ConversorLongitud());
        registrar(new ConversorMasa());
        registrar(new ConversorTemperatura());
    }

    private CatalogoUnidades() {
    }

    public static void registrar(Conversor conversor) {
        CONVERSORES.add(conversor);
    }

    /** Nombres de las categorias disponibles. */
    public static List<String> categorias() {
        List<String> r = new ArrayList<>();
        for (Conversor c : CONVERSORES) {
            r.add(c.categoria());
        }
        return r;
    }

    /** Busca una categoria por nombre (sin distinguir mayusculas ni tildes). */
    public static Optional<Conversor> buscarCategoria(String nombre) {
        String n = Conversor.normalizar(nombre);
        if (n == null || n.isEmpty()) {
            return Optional.empty();
        }
        for (Conversor c : CONVERSORES) {
            if (Conversor.normalizar(c.categoria()).equals(n)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    /** Categoria a la que pertenece una unidad (ya normalizada), si existe. */
    public static Optional<Conversor> categoriaDeUnidad(String unidadNormalizada) {
        for (Conversor c : CONVERSORES) {
            if (c.unidades().contains(unidadNormalizada)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }
}
