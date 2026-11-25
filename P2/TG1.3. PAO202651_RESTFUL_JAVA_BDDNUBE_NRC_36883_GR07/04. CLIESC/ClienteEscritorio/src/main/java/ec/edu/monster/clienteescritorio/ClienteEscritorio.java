package ec.edu.monster.clienteescritorio;

import ec.edu.monster.clienteescritorio.vista.VentanaEureka;
import javax.swing.SwingUtilities;

/**
 * Cliente de escritorio de EurekaBank (Java Swing + WebView de JavaFX) con la misma interfaz del cliente web.
 * La URL del servidor se puede escribir en el campo «Servidor» del login (por defecto
 * http://localhost:8080/REST_EurekaBank_Java_GR07/api/ o la de -Deureka.url / EUREKA_URL).
 */
public class ClienteEscritorio {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new VentanaEureka().setVisible(true);
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo abrir la ventana", e);
            }
        });
    }
}
