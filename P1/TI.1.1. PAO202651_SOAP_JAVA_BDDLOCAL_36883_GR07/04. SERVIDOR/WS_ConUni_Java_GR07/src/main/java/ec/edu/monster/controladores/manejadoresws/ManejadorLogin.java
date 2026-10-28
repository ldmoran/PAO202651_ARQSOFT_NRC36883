package ec.edu.monster.controladores.manejadoresws;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import ec.edu.monster.seguridad.ServicioAutenticacion;

/** Maneja el pedido de login (CU-01) que recibe el servicio WSLogin. */
public class ManejadorLogin {

    public RespuestaServicio login(String usuario, String clave) {
        try {
            return ServicioAutenticacion.autenticar(usuario, clave);
        } catch (RuntimeException e) {
            return RespuestaMapeador.error(CodigoResultado.ERROR_SERVIDOR, "Error interno del servicio");
        }
    }
}
