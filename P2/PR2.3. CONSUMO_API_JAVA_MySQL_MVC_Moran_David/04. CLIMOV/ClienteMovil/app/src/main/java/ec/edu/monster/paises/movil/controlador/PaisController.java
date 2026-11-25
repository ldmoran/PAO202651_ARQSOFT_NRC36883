package ec.edu.monster.paises.movil.controlador;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ec.edu.monster.paises.movil.modelo.ClienteApi;

/**
 * Controlador: ejecuta las llamadas al servidor fuera del hilo principal y devuelve el resultado en el hilo de la pantalla.
 * Las vistas solo piden cosas aqui; no saben nada de HTTP.
 */
public class PaisController {

    public interface Resultado<T> {
        void ok(T valor);

        void error(String mensaje);
    }

    private final ClienteApi api;
    private final ExecutorService hilos = Executors.newFixedThreadPool(3);
    private final Handler principal = new Handler(Looper.getMainLooper());

    public PaisController(ClienteApi api) {
        this.api = api;
    }

    public ClienteApi api() {
        return api;
    }

    public <T> void ejecutar(final Callable<T> tarea, final Resultado<T> resultado) {
        hilos.execute(() -> {
            try {
                final T valor = tarea.call();
                principal.post(() -> resultado.ok(valor));
            } catch (final Exception e) {
                final String m = e.getMessage() == null ? "Algo salió mal" : e.getMessage();
                principal.post(() -> resultado.error(m));
            }
        });
    }

    public void enHiloPrincipal(Runnable r) {
        principal.post(r);
    }
}
