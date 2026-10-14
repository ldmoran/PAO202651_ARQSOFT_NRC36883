package ec.edu.monster.clienteconsola.controlador;

import ec.edu.monster.clienteconsola.modelo.RespuestaServicio;
import ec.edu.monster.clienteconsola.modelo.Sesion;
import ec.edu.monster.clienteconsola.modelo.SoapClient;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Controlador del catalogo y las conversiones: usa el token de la sesion, llama al
 * servicio SOAP y cierra la sesion local cuando el servidor rechaza el token.
 */
public class ControladorConversiones {

    private final SoapClient cliente;
    private final Sesion sesion;

    public ControladorConversiones(SoapClient cliente, Sesion sesion) {
        this.cliente = cliente;
        this.sesion = sesion;
    }

    public RespuestaServicio categorias() {
        return revisar(cliente.obtenerCategorias(sesion.getToken()));
    }

    public RespuestaServicio unidades(String categoria) {
        return revisar(cliente.obtenerUnidades(sesion.getToken(), categoria));
    }

    public RespuestaServicio convertir(String categoria, double valor, String origen, String destino) {
        return revisar(cliente.convertir(categoria, valor, origen, destino, sesion.getToken()));
    }

    /** Si el servidor dice que el token no sirve, se cierra la sesion local. */
    private RespuestaServicio revisar(RespuestaServicio r) {
        if (r.requiereNuevoLogin()) {
            sesion.cerrar();
        }
        return r;
    }

    /** Numero sin notacion cientifica y sin ceros sobrantes. */
    public static String formatear(double valor) {
        BigDecimal b = BigDecimal.valueOf(valor).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return b.scale() < 0 ? b.setScale(0).toPlainString() : b.toPlainString();
    }
}
