package ec.edu.monster.clientemovil.controlador;

import ec.edu.monster.clientemovil.modelo.RespuestaServicio;
import ec.edu.monster.clientemovil.modelo.SoapClient;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Controlador del cliente movil: guarda el token de la sesion, llama al
 * servicio SOAP y descarta el token cuando el servidor lo rechaza.
 */
public class ControladorConversiones {

    private final SoapClient cliente;
    private String token;

    public ControladorConversiones(SoapClient cliente) {
        this.cliente = cliente;
    }

    /** URL del servidor por defecto (para que la vista no toque el modelo). */
    public static String urlPorDefecto() {
        return SoapClient.URL_POR_DEFECTO;
    }

    /** Crea el controlador ya conectado a la URL indicada. */
    public static ControladorConversiones conectar(String url) {
        return new ControladorConversiones(new SoapClient(url));
    }

    public String getUrl() {
        return cliente.getBaseUrl();
    }

    public boolean haySesion() {
        return token != null;
    }

    public RespuestaServicio iniciarSesion(String usuario, String clave) {
        RespuestaServicio r = cliente.login(usuario, clave);
        token = (r.esExitosa() && r.getToken() != null) ? r.getToken() : null;
        return r;
    }

    public void cerrarSesion() {
        token = null;
    }

    public RespuestaServicio categorias() {
        return revisar(cliente.obtenerCategorias(token));
    }

    public RespuestaServicio unidades(String categoria) {
        return revisar(cliente.obtenerUnidades(token, categoria));
    }

    public RespuestaServicio convertir(String categoria, double valor, String origen, String destino) {
        return revisar(cliente.convertir(categoria, valor, origen, destino, token));
    }

    /** Si el servidor dice que el token no sirve, se cierra la sesion local. */
    private RespuestaServicio revisar(RespuestaServicio r) {
        if (r.requiereNuevoLogin()) {
            token = null;
        }
        return r;
    }

    /** Numero sin notacion cientifica y sin ceros sobrantes. */
    public static String formatear(double valor) {
        BigDecimal b = BigDecimal.valueOf(valor).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return b.scale() < 0 ? b.setScale(0).toPlainString() : b.toPlainString();
    }
}
