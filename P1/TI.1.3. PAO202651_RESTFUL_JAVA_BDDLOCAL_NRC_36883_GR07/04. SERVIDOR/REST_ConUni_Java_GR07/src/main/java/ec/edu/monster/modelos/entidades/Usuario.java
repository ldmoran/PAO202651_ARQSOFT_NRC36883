package ec.edu.monster.modelos.entidades;

/** Usuario del sistema; la clave se guarda como SHA-256(sal + clave). */
public record Usuario(int id, String nombre, String sal, String hashClave) {
}
