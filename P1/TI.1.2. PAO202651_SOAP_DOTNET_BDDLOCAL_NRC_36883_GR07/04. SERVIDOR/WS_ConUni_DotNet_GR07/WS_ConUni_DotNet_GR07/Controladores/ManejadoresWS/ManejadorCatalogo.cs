using ec.edu.monster.Modelos.Utilidades;
using ec.edu.monster.Modelos.Utilidades.Enums;
using ec.edu.monster.Modelos.Utilidades.Mapeadores;
using ec.edu.monster.Seguridad;
using ec.edu.monster.Servicios;

namespace ec.edu.monster.Controladores.ManejadoresWS
{
    /// <summary>Maneja los pedidos del catalogo (CU-02 y CU-03) que recibe el servicio WSCatalogo.</summary>
    public class ManejadorCatalogo
    {
        /// <summary>CU-02: categorias disponibles.</summary>
        public RespuestaServicio ObtenerCategorias(string? token)
        {
            try
            {
                var errorToken = GestorTokens.Verificar(token);
                if (errorToken is not null)
                {
                    return errorToken;
                }
                return RespuestaMapeador.ExitoLista(CatalogoUnidades.Cargar().Categorias());
            }
            catch (Exception)
            {
                return RespuestaMapeador.Error(CodigoResultado.ErrorServidor, "Error interno del servicio");
            }
        }

        /// <summary>CU-03: unidades de una categoria.</summary>
        public RespuestaServicio ObtenerUnidades(string? token, string? categoria)
        {
            try
            {
                var errorToken = GestorTokens.Verificar(token);
                if (errorToken is not null)
                {
                    return errorToken;
                }
                var encontrada = CatalogoUnidades.Cargar().BuscarCategoria(categoria);
                if (encontrada is null)
                {
                    return RespuestaMapeador.Error(CodigoResultado.DatosInconsistentes, "Categoría no soportada");
                }
                return RespuestaMapeador.ExitoLista(encontrada.NombresUnidades());
            }
            catch (Exception)
            {
                return RespuestaMapeador.Error(CodigoResultado.ErrorServidor, "Error interno del servicio");
            }
        }
    }
}
