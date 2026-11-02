package ec.edu.monster.modelos.entidades;

/**
 * Unidad de una categoria. Para pasar a la base se usa (valor + desplazamiento) x factor; para
 * volver, base / factor - desplazamiento.
 */
public record Unidad(int id, String nombre, double factor, double desplazamiento) {
}
