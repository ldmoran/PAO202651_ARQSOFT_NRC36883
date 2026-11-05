package ec.edu.monster.clienteescritorio.modelo;

/** Resultado de iniciar sesion o de registrar un movimiento: ok y el mensaje que se muestra al usuario. */
public record Resultado(boolean ok, String mensaje) {
}
