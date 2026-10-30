using System.Globalization;
using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;
using ec.edu.monster.Seguridad;
using ec.edu.monster.Servicios;

namespace ec.edu.monster.Controladores.Manejadores
{
    /// <summary>
    /// Maneja el pedido de conversion (CU-04) que recibe ConversionesController para
    /// las rutas longitud, masa y temperatura. Aplica CU-05 (validar token), CU-06
    /// (validar datos) y CU-07 (respuesta estructurada), y los errores CU-08 a CU-11.
    /// </summary>
    public class ManejadorConversion
    {
        /// <summary>CU-04: realizar conversion dentro de una categoria.</summary>
        public RespuestaServicio Convertir(string categoria, double valor,
            string? unidadOrigen, string? unidadDestino, string? token)
        {
            try
            {
                // CU-05: validar token (CU-08 / CU-09 si falla)
                var errorToken = GestorTokens.Verificar(token);
                if (errorToken is not null)
                {
                    return errorToken;
                }
                // CU-06: validar datos (CU-10 / CU-11 si falla)
                var conversor = CatalogoUnidades.BuscarCategoria(categoria);
                if (conversor is null)
                {
                    return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes, "Categoría no soportada");
                }
                if (double.IsNaN(valor) || double.IsInfinity(valor))
                {
                    return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes, "El valor debe ser numérico");
                }
                if (string.IsNullOrWhiteSpace(unidadOrigen) || string.IsNullOrWhiteSpace(unidadDestino))
                {
                    return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes,
                        "Las unidades de origen y destino son obligatorias");
                }
                var origen = Texto.Normalizar(unidadOrigen)!;
                var destino = Texto.Normalizar(unidadDestino)!;
                var catOrigen = CatalogoUnidades.CategoriaDeUnidad(origen);
                if (catOrigen is null)
                {
                    return RespuestaMapeador.Error(CodigoResultado.UnidadNoSoportada, "Unidad de origen no soportada");
                }
                var catDestino = CatalogoUnidades.CategoriaDeUnidad(destino);
                if (catDestino is null)
                {
                    return RespuestaMapeador.Error(CodigoResultado.UnidadNoSoportada, "Unidad de destino no soportada");
                }
                if (!ReferenceEquals(catOrigen, conversor) || !ReferenceEquals(catDestino, conversor))
                {
                    return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes,
                        "Las unidades no pertenecen a la categoría " + conversor.Categoria);
                }
                // Calculo y CU-07: respuesta estructurada
                var resultado = Redondear(conversor.Convertir(valor, origen, destino));
                return RespuestaMapeador.ExitoConversion(resultado, origen, destino);
            }
            catch (Exception)
            {
                return RespuestaMapeador.Error(CodigoResultado.ErrorServidor, "Error interno del servicio");
            }
        }

        /// <summary>Evita ruido de coma flotante (0.30000000000000004): 12 cifras significativas.</summary>
        public static double Redondear(double v)
        {
            if (v == 0.0)
            {
                return 0.0;
            }
            return double.Parse(v.ToString("G12", CultureInfo.InvariantCulture), CultureInfo.InvariantCulture);
        }
    }
}
