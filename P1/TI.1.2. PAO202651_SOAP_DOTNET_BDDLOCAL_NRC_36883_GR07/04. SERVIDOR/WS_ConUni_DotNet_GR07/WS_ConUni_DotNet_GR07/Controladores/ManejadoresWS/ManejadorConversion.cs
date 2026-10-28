using System.Globalization;
using ec.edu.monster.Datos;
using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;
using ec.edu.monster.Seguridad;
using ec.edu.monster.Servicios;

namespace ec.edu.monster.Controladores.ManejadoresWS
{
    /// <summary>
    /// Maneja el pedido de conversion (CU-04) que reciben WSLongitud, WSMasa y
    /// WSTemperatura. Aplica CU-05 (validar token), CU-06 (validar datos) y CU-07
    /// (respuesta estructurada), y los errores CU-08 a CU-11. Cada conversion correcta
    /// queda guardada en el historial (tabla conversion).
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
                var catalogo = CatalogoUnidades.Cargar();
                var elegida = catalogo.BuscarCategoria(categoria);
                if (elegida is null)
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
                var catOrigen = catalogo.CategoriaDeUnidad(origen);
                if (catOrigen is null)
                {
                    return RespuestaMapeador.Error(CodigoResultado.UnidadNoSoportada, "Unidad de origen no soportada");
                }
                var catDestino = catalogo.CategoriaDeUnidad(destino);
                if (catDestino is null)
                {
                    return RespuestaMapeador.Error(CodigoResultado.UnidadNoSoportada, "Unidad de destino no soportada");
                }
                if (catOrigen.Id != elegida.Id || catDestino.Id != elegida.Id)
                {
                    return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes,
                        "Las unidades no pertenecen a la categoría " + elegida.Nombre);
                }
                // Calculo, historial y CU-07: respuesta estructurada
                var uOrigen = CatalogoUnidades.UnidadDe(elegida, origen)!;
                var uDestino = CatalogoUnidades.UnidadDe(elegida, destino)!;
                var resultado = Redondear(Conversor.Convertir(valor, uOrigen, uDestino));
                new ConversionRepositorio(BaseDatos.Fuente).Registrar(token!.Trim(), uOrigen.Id, uDestino.Id,
                    valor, resultado, BaseDatos.Ahora());
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
