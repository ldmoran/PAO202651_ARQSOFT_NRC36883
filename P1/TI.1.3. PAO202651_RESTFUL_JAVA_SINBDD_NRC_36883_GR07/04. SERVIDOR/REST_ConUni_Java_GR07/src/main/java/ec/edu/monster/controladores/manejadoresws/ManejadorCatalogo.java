package ec.edu.monster.controladores.manejadoresws;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import ec.edu.monster.seguridad.GestorTokens;
import ec.edu.monster.servicios.CatalogoUnidades;
import ec.edu.monster.servicios.Conversor;
import java.util.Optional;

/** Maneja las consultas de catálogo (CU-02 y CU-03) de la API REST. */
public class ManejadorCatalogo {

    /** CU-02: categorias disponibles. */
    public RespuestaServicio obtenerCategorias(String token) {
        RespuestaServicio errorToken = GestorTokens.verificar(token);
        if (errorToken != null) {
            return errorToken;
        }
        return RespuestaMapeador.exitoLista(CatalogoUnidades.categorias());
    }

    /** CU-03: unidades de una categoria. */
    public RespuestaServicio obtenerUnidades(String token, String categoria) {
        RespuestaServicio errorToken = GestorTokens.verificar(token);
        if (errorToken != null) {
            return errorToken;
        }
        Optional<Conversor> conversor = CatalogoUnidades.buscarCategoria(categoria);
        if (conversor.isEmpty()) {
            return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES, "Categoría no soportada");
        }
        return RespuestaMapeador.exitoLista(conversor.get().unidades());
    }
}
