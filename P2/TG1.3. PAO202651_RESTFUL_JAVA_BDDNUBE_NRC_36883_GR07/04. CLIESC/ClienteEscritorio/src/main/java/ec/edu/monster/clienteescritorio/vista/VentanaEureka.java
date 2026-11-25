package ec.edu.monster.clienteescritorio.vista;

import ec.edu.monster.clienteescritorio.controlador.ApiLocal;
import ec.edu.monster.clienteescritorio.modelo.RestClient;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import netscape.javascript.JSObject;

/**
 * Ventana del cliente de escritorio. Muestra la MISMA interfaz web de EurekaBank (login con foto de Monster, menu lateral,
 * cuentas, movimientos y operar) dentro de un WebView de JavaFX. Las llamadas a la API REST salen por el puente EurekaNative.
 */
public class VentanaEureka extends JFrame {

    private static final String[] ARCHIVOS = {"index.html", "panel.html", "monster.jpg", "css/monster.css", "css/eureka.css", "css/web.css",
        "js/comun.js", "js/login.js", "js/panel.js"};

    private final ApiLocal api = new ApiLocal();
    private final Puente puente = new Puente();
    private WebEngine motor;

    /** Objeto que la pagina ve como window.EurekaNative (clase y metodos publicos: lo exige el WebView). */
    public class Puente {

        public String urlPorDefecto() {
            return RestClient.urlConfigurada();
        }

        public void llamar(String id, String metodo, String ruta, String cuerpo) {
            Thread t = new Thread(() -> {
                ApiLocal.Respuesta r = api.atender(metodo, ruta, cuerpo);
                String js = "window.__eurekaResp('" + id + "'," + r.estado() + "," + literal(r.json()) + ")";
                Platform.runLater(() -> motor.executeScript(js));
            }, "api-eureka");
            t.setDaemon(true);
            t.start();
        }
    }

    static String literal(String texto) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case ' ' -> sb.append("\\u2028");
                case ' ' -> sb.append("\\u2029");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    public VentanaEureka() throws IOException {
        super("EurekaBank");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JFXPanel panel = new JFXPanel();
        panel.setPreferredSize(new Dimension(1180, 760));
        add(panel);
        pack();
        setLocationRelativeTo(null);

        Path carpeta = Files.createTempDirectory("eurekabank-ui");
        carpeta.toFile().deleteOnExit();
        for (String a : ARCHIVOS) {
            Path destino = carpeta.resolve(a);
            Files.createDirectories(destino.getParent());
            try (InputStream in = VentanaEureka.class.getResourceAsStream("/web/" + a)) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            destino.toFile().deleteOnExit();
        }
        String url = carpeta.resolve("index.html").toUri() + "?nativo=1";
        Platform.runLater(() -> {
            WebView vista = new WebView();
            motor = vista.getEngine();
            motor.documentProperty().addListener((obs, anterior, nuevo) -> {
                if (nuevo != null) {
                    ((JSObject) motor.executeScript("window")).setMember("EurekaNative", puente);
                }
            });
            panel.setScene(new Scene(vista));
            motor.load(url);
        });
    }
}
