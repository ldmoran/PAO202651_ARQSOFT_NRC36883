package ec.edu.monster.escritorio;

import ec.edu.monster.cliente.EurekaSoap;
import ec.edu.monster.escritorio.controlador.ControladorEscritorio;
import ec.edu.monster.escritorio.vista.Estilo;
import ec.edu.monster.escritorio.vista.VentanaPrincipal;
import javax.swing.SwingUtilities;

/** Cliente de escritorio de EurekaBank. Direccion del servicio: primer argumento o variable EUREKA_URL. */
public class Main {

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getenv("EUREKA_URL");
        if (url == null || url.isBlank()) {
            url = EurekaSoap.URL_POR_DEFECTO;
        }
        String direccion = url;
        SwingUtilities.invokeLater(() -> {
            Estilo.instalar();
            new VentanaPrincipal(new ControladorEscritorio(new EurekaSoap(direccion)), direccion).setVisible(true);
        });
    }
}
