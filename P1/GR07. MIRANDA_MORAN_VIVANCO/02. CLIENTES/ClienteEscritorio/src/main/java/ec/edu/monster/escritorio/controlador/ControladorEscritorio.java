package ec.edu.monster.escritorio.controlador;

import ec.edu.monster.cliente.ComercializadoraSoap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingWorker;

/** Ejecuta las llamadas SOAP fuera del hilo de la interfaz para que la ventana no se congele. */
public class ControladorEscritorio {

    private final ComercializadoraSoap servicio;

    public ControladorEscritorio(ComercializadoraSoap servicio) {
        this.servicio = servicio;
    }

    public ComercializadoraSoap servicio() {
        return servicio;
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
