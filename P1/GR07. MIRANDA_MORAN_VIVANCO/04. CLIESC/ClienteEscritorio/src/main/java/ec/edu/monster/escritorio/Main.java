package ec.edu.monster.escritorio;

import ec.edu.monster.cliente.ComercializadoraSoap;
import ec.edu.monster.escritorio.controlador.ControladorEscritorio;
import ec.edu.monster.escritorio.vista.Estilo;
import ec.edu.monster.escritorio.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;

/** Cliente de escritorio. Direccion del servicio: primer argumento o variable COMERCIALIZADORA_URL. */
public class Main {

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getenv("COMERCIALIZADORA_URL");
        if (url == null || url.isBlank()) {
            url = ComercializadoraSoap.URL_POR_DEFECTO;
        }
        String direccion = url;
        SwingUtilities.invokeLater(() -> {
            Estilo.instalar();
            new VentanaPrincipal(new ControladorEscritorio(new ComercializadoraSoap(direccion)), direccion).setVisible(true);
        });
    }
}
