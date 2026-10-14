package ec.edu.monster.controladores.manejadoresws;

import ec.edu.monster.modelos.utilidades.RespuestaServicio;
import ec.edu.monster.modelos.utilidades.enums.CodigoResultado;
import ec.edu.monster.modelos.utilidades.mapeadores.RespuestaMapeador;
import ec.edu.monster.seguridad.GestorTokens;
import ec.edu.monster.servicios.CatalogoUnidades;
import ec.edu.monster.servicios.Conversor;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

/**
 * Maneja el pedido de conversion (CU-04) que reciben WSLongitud, WSMasa y
 * WSTemperatura. Aplica CU-05 (validar token), CU-06 (validar datos) y CU-07
 * (respuesta estructurada), y los errores CU-08 a CU-11.
 */
public class ManejadorConversion {

    /** CU-04: realizar conversion dentro de una categoria. */
    public RespuestaServicio convertir(String categoria, double valor,
            String unidadOrigen, String unidadDestino, String token) {
        try {
            // CU-05: validar token (CU-08 / CU-09 si falla)
            RespuestaServicio errorToken = GestorTokens.verificar(token);
            if (errorToken != null) {
                return errorToken;
            }
            // CU-06: validar datos (CU-10 / CU-11 si falla)
            Optional<Conversor> cat = CatalogoUnidades.buscarCategoria(categoria);
            if (cat.isEmpty()) {
                return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES, "Categoría no soportada");
            }
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES, "El valor debe ser numérico");
            }
            if (unidadOrigen == null || unidadOrigen.isBlank() || unidadDestino == null || unidadDestino.isBlank()) {
                return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES,
                        "Las unidades de origen y destino son obligatorias");
            }
            String origen = Conversor.normalizar(unidadOrigen);
            String destino = Conversor.normalizar(unidadDestino);
            Optional<Conversor> catOrigen = CatalogoUnidades.categoriaDeUnidad(origen);
            if (catOrigen.isEmpty()) {
                return RespuestaMapeador.error(CodigoResultado.UNIDAD_NO_SOPORTADA, "Unidad de origen no soportada");
            }
            Optional<Conversor> catDestino = CatalogoUnidades.categoriaDeUnidad(destino);
            if (catDestino.isEmpty()) {
                return RespuestaMapeador.error(CodigoResultado.UNIDAD_NO_SOPORTADA, "Unidad de destino no soportada");
            }
            Conversor conversor = cat.get();
            if (catOrigen.get() != conversor || catDestino.get() != conversor) {
                return RespuestaMapeador.error(CodigoResultado.DATOS_INCONSISTENTES,
                        "Las unidades no pertenecen a la categoría " + conversor.categoria());
            }
            // Calculo y CU-07: respuesta estructurada
            double resultado = redondear(conversor.convertir(valor, origen, destino));
            return RespuestaMapeador.exitoConversion(resultado, origen, destino);
        } catch (RuntimeException e) {
            return RespuestaMapeador.error(CodigoResultado.ERROR_SERVIDOR, "Error interno del servicio");
        }
    }

    /** Evita ruido de coma flotante (0.30000000000000004): 12 cifras significativas. */
    static double redondear(double v) {
        if (v == 0.0) {
            return 0.0;
        }
        return new BigDecimal(v).round(new MathContext(12)).doubleValue();
    }
}
