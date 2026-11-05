package ec.edu.monster.eurekamovil.vista;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import ec.edu.monster.eurekamovil.controlador.ApiLocal;
import ec.edu.monster.eurekamovil.modelo.RestClient;

/**
 * Unica pantalla: un WebView con la MISMA interfaz web de EurekaBank (login con foto de Monster, menu, cuentas,
 * movimientos y operar). Las llamadas a la API REST salen por el puente EurekaNative.
 */
public class MainActivity extends Activity {

    private WebView vista;
    private final ApiLocal api = new ApiLocal();

    public class Puente {
        @JavascriptInterface
        public String urlPorDefecto() {
            return RestClient.URL_POR_DEFECTO;
        }

        @JavascriptInterface
        public void llamar(final String id, final String metodo, final String ruta, final String cuerpo) {
            Thread t = new Thread(() -> {
                ApiLocal.Respuesta r = api.atender(metodo, ruta, cuerpo);
                final String js = "window.__eurekaResp('" + id + "'," + r.estado + "," + literal(r.json) + ")";
                vista.post(() -> vista.evaluateJavascript(js, null));
            }, "api-eureka");
            t.setDaemon(true);
            t.start();
        }
    }

    static String literal(String texto) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case ' ': sb.append("\\u2028"); break;
                case ' ': sb.append("\\u2029"); break;
                default: sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        vista = new WebView(this);
        vista.setBackgroundColor(Color.parseColor("#F3F6FB"));
        WebSettings s = vista.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        vista.setWebViewClient(new WebViewClient());
        vista.addJavascriptInterface(new Puente(), "EurekaNative");
        setContentView(vista);
        vista.loadUrl("file:///android_asset/web/index.html?nativo=1");
    }

    @Override
    public void onBackPressed() {
        if (vista.canGoBack()) {
            vista.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
