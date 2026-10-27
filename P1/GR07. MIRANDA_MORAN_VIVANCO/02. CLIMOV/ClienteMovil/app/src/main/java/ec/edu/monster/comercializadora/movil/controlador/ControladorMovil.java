package ec.edu.monster.comercializadora.movil.controlador;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import ec.edu.monster.comercializadora.movil.modelo.ComercializadoraSoap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Guarda la direccion del servicio y ejecuta las llamadas SOAP fuera del hilo principal
 * (Android prohibe usar la red en ese hilo), devolviendo el resultado al hilo de la pantalla.
 */
public class ControladorMovil {

    /** 10.0.2.2 es la PC anfitriona vista desde el emulador; en un celular real hay que poner la IP de la PC. */
    public static final String URL_EMULADOR = "http://10.0.2.2:8080/Comercializadora/WSComercializadora";

    public interface Resultado<T> {
        void alRecibir(T valor);
    }

    private final SharedPreferences preferencias;
    private final ExecutorService hilo = Executors.newSingleThreadExecutor();
    private final Handler principal = new Handler(Looper.getMainLooper());
    private ComercializadoraSoap servicio;

    public ControladorMovil(Context contexto) {
        preferencias = contexto.getSharedPreferences("comercializadora", Context.MODE_PRIVATE);
        servicio = new ComercializadoraSoap(url());
    }

    public String url() {
        return preferencias.getString("url", URL_EMULADOR);
    }

    public void cambiarUrl(String url) {
        String limpia = url == null ? "" : url.trim();
        if (limpia.isEmpty()) {
            limpia = URL_EMULADOR;
        }
        preferencias.edit().putString("url", limpia).apply();
        servicio = new ComercializadoraSoap(limpia);
    }

    public ComercializadoraSoap servicio() {
        return servicio;
    }

    public <T> void ejecutar(Callable<T> tarea, Resultado<T> exito, Resultado<String> fallo) {
        hilo.execute(() -> {
            try {
                T valor = tarea.call();
                principal.post(() -> exito.alRecibir(valor));
            } catch (Exception e) {
                String mensaje = e.getMessage() == null ? e.toString() : e.getMessage();
                principal.post(() -> fallo.alRecibir(mensaje));
            }
        });
    }
}
