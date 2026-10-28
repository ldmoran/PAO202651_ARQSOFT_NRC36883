package ec.edu.monster.controladores.manejadoresws;

import ec.edu.monster.modelos.entidades.Categoria;
import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import ec.edu.monster.seguridad.GestorTokens;
import ec.edu.monster.servicios.CatalogoUnidades;
import java.util.Optional;

/** Maneja los pedidos del catalogo (CU-02 y CU-03) que recibe el servicio WSCatalogo. */
public class ManejadorCatalogo {

    /** CU-02: categorias disponibles. */
    public RespuestaServicio obtenerCategorias(String token) {
        try {
            RespuestaServicio errorToken = GestorTokens.verificar(token);
            if (errorToken != null) {
                return errorToken;
            }
            return RespuestaMapeador.exitoLista(CatalogoUnidades.cargar().categorias());
        } catch (RuntimeException e) {
            return RespuestaMapeador.error(CodigoResultado.ERROR_SERVIDOR, "Error interno del servicio");
        }
    }

    /** CU-03: unidades de una categoria. */
    public RespuestaServicio obtenerUnidades(String token, String categoria) {
        try {
            RespuestaServicio errorToken = GestorTokens.verificar(token);
            if (errorToken != null) {
                return errorToken;
            }
            Optional<Categoria> encontrada = CatalogoUnidades.cargar().buscarCategoria(categoria);
            if (encontrada.isEmpty()) {
                return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES, "Categoría no soportada");
            }
            return RespuestaMapeador.exitoLista(encontrada.get().nombresUnidades());
        } catch (RuntimeException e) {
            return RespuestaMapeador.error(CodigoResultado.ERROR_SERVIDOR, "Error interno del servicio");
        }
    }
}
