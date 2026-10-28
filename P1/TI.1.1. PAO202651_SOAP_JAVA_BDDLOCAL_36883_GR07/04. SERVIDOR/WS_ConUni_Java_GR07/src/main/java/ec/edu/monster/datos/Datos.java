package ec.edu.monster.datos;

import java.time.Clock;

/**
 * Punto unico de acceso a la base de datos y al reloj del servidor. En produccion usa MySQL y
 * la hora del sistema; las pruebas lo cambian con {@link #configurar} para usar H2 y una hora fija.
 */
public final class Datos {

    private static volatile FuenteDatos fuente = new ConexionMySql();
    private static volatile Clock reloj = Clock.systemDefaultZone();

    private Datos() {
    }

    public static FuenteDatos fuente() {
        return fuente;
    }

    public static Clock reloj() {
        return reloj;
    }

    public static void configurar(FuenteDatos nuevaFuente, Clock nuevoReloj) {
        fuente = nuevaFuente;
        reloj = nuevoReloj;
    }

    /** Vuelve a MySQL y a la hora del sistema. */
    public static void restablecer() {
        configurar(new ConexionMySql(), Clock.systemDefaultZone());
    }
}
