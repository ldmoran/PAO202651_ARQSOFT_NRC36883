using ec.edu.monster.Modelos.Utilidades.Enums;

namespace ec.edu.monster.Modelos.Utilidades.Mapeadores
{
    /// <summary>
    /// Convierte los resultados de la logica (tokens, listas, valores convertidos
    /// y errores) en la respuesta estructurada <see cref="RespuestaServicio"/>
    /// (CU-07). Es el unico lugar donde se arma la respuesta, para que todas las
    /// operaciones respondan con el mismo formato.
    /// </summary>
    public static class RespuestaMapeador
    {
        /// <summary>Respuesta correcta con un mensaje.</summary>
        public static RespuestaServicio Exito(string mensaje) => new()
        {
            Estado = EstadoRespuesta.Ok.Texto(),
            Codigo = CodigoResultado.Ok200.Texto(),
            Mensaje = mensaje
        };

        /// <summary>Login correcto: lleva el token de la sesion.</summary>
        public static RespuestaServicio ExitoLogin(string token)
        {
            var r = Exito("Autenticación exitosa");
            r.Token = token;
            return r;
        }

        /// <summary>Consulta del catalogo: lleva la lista de categorias o de unidades.</summary>
        public static RespuestaServicio ExitoLista(IEnumerable<string> datos)
        {
            var r = Exito("Consulta realizada correctamente");
            r.Datos = datos.ToList();
            return r;
        }

        /// <summary>Conversion correcta: lleva el resultado y las unidades usadas.</summary>
        public static RespuestaServicio ExitoConversion(double resultado, string unidadOrigen, string unidadDestino)
        {
            var r = Exito("Conversión realizada correctamente");
            r.Resultado = resultado;
            r.UnidadOrigen = unidadOrigen;
            r.UnidadDestino = unidadDestino;
            return r;
        }

        /// <summary>Respuesta de error con su codigo y mensaje; el resultado queda nulo.</summary>
        public static RespuestaServicio Error(CodigoResultado codigo, string mensaje) => new()
        {
            Estado = EstadoRespuesta.Error.Texto(),
            Codigo = codigo.Texto(),
            Mensaje = mensaje
        };
    }
}
