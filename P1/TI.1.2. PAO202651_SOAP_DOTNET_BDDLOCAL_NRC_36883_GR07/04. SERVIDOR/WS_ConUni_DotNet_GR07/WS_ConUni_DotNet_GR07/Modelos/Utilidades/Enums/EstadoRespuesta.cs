namespace ec.edu.monster.Modelos.Utilidades.Enums
{
    /// <summary>Estado general de una respuesta: la operacion salio bien o fallo.</summary>
    public enum EstadoRespuesta
    {
        Ok,
        Error
    }

    public static class EstadoRespuestaExtensiones
    {
        /// <summary>Texto que viaja en el mensaje SOAP: "OK" o "ERROR".</summary>
        public static string Texto(this EstadoRespuesta estado) =>
            estado == EstadoRespuesta.Ok ? "OK" : "ERROR";
    }
}
