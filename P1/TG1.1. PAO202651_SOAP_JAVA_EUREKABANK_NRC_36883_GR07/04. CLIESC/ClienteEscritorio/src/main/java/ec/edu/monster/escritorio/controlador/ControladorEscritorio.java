package ec.edu.monster.escritorio.controlador;

import ec.edu.monster.cliente.EurekaSoap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingWorker;

/** Ejecuta las llamadas SOAP fuera del hilo de la interfaz para que la ventana no se congele. */
public class ControladorEscritorio {

    private volatile EurekaSoap servicio;

    public ControladorEscritorio(EurekaSoap servicio) {
        this.servicio = servicio;
    }

    public EurekaSoap servicio() {
        return servicio;
    }

    /** Cambia el servidor al que se conecta la aplicacion (por ejemplo, el de otro equipo de la red). */
    public void conectarA(String url) {
        this.servicio = new EurekaSoap(url);
    }

    /** La direccion debe ser una URL http o https. */
    public static boolean urlValida(String url) {
        return url != null && (url.regionMatches(true, 0, "http://", 0, 7) || url.regionMatches(true, 0, "https://", 0, 8));
    }

    /** Llama a {@code tarea} en segundo plano y entrega el resultado (o el mensaje de error) en el hilo de Swing. */
    public <T> void ejecutar(Supplier<T> tarea, Consumer<T> alTerminar, Consumer<String> alFallar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    alFallar.accept(causa.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
