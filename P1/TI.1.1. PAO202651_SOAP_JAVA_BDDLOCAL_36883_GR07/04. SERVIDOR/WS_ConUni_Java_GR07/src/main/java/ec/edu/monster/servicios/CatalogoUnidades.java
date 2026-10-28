package ec.edu.monster.servicios;

import ec.edu.monster.datos.CatalogoRepositorio;
import ec.edu.monster.datos.Datos;
import ec.edu.monster.modelos.entidades.Categoria;
import ec.edu.monster.modelos.entidades.Unidad;
import java.util.List;
import java.util.Optional;

/**
 * Catalogo de categorias y unidades (CU-02 y CU-03), leido de las tablas CATEGORIA y UNIDAD.
 * Para agregar una categoria nueva (por ejemplo volumen) basta con insertar sus filas en la
 * base; el login, la validacion y la respuesta estructurada no cambian.
 */
public final class CatalogoUnidades {

    private final List<Categoria> categorias;

    private CatalogoUnidades(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    /** Lee el catalogo vigente de la base de datos. */
    public static CatalogoUnidades cargar() {
        return new CatalogoUnidades(new CatalogoRepositorio(Datos.fuente()).cargar());
    }

    /** Nombres de las categorias disponibles. */
    public List<String> categorias() {
        return categorias.stream().map(Categoria::nombre).toList();
    }

    /** Busca una categoria por nombre (sin distinguir mayusculas ni tildes). */
    public Optional<Categoria> buscarCategoria(String nombre) {
        String n = Conversor.normalizar(nombre);
        if (n == null || n.isEmpty()) {
            return Optional.empty();
        }
        return categorias.stream().filter(c -> Conversor.normalizar(c.nombre()).equals(n)).findFirst();
    }

    /** Categoria a la que pertenece una unidad (ya normalizada), si existe. */
    public Optional<Categoria> categoriaDeUnidad(String unidadNormalizada) {
        return categorias.stream()
                .filter(c -> c.unidades().stream().anyMatch(u -> u.nombre().equals(unidadNormalizada)))
                .findFirst();
    }

    /** Unidad de una categoria por su nombre normalizado. */
    public static Optional<Unidad> unidadDe(Categoria categoria, String unidadNormalizada) {
        return categoria.unidades().stream().filter(u -> u.nombre().equals(unidadNormalizada)).findFirst();
    }
}
