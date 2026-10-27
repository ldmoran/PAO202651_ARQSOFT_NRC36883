package ec.edu.monster.escritorio;

import ec.edu.monster.cliente.ComercializadoraSoap;
import ec.edu.monster.escritorio.controlador.ControladorEscritorio;
import ec.edu.monster.escritorio.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Cliente de escritorio. Direccion del servicio: primer argumento o variable COMERCIALIZADORA_URL. */
public class Main {

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getenv("COMERCIALIZADORA_URL");
        if (url == null || url.isBlank()) {
            url = ComercializadoraSoap.URL_POR_DEFECTO;
        }
        String direccion = url;
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // se queda con el aspecto por defecto
            }
            new VentanaPrincipal(new ControladorEscritorio(new ComercializadoraSoap(direccion))).setVisible(true);
        });
    }
}
