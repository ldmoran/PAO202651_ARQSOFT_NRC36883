package ec.edu.monster.modelos.entidades;

import java.util.List;

/** Categoria con sus unidades (por ejemplo Longitud: centimetros, metros, pies...). */
public record Categoria(int id, String nombre, List<Unidad> unidades) {

    /** Nombres de las unidades, en el orden definido en la base. */
    public List<String> nombresUnidades() {
        return unidades.stream().map(Unidad::nombre).toList();
    }
}
