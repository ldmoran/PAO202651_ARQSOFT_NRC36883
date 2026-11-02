package ec.edu.monster.clienteescritorio;

import ec.edu.monster.clienteescritorio.vista.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Cliente de escritorio (Java Swing) de "Conversion de Unidades Monster".
 *
 * La URL del servidor se escribe en la ventana de login (por defecto
 * http://localhost:8080/REST_ConUni_Java_GR07/api/ o la de -Dconuni.url / CONUNI_URL).
 */
public class ClienteEscritorio {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // se usa el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
