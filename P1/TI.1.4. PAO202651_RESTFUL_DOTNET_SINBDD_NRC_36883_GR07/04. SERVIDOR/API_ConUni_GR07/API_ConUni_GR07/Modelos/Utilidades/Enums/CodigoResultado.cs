namespace ec.edu.monster.Modelos.Utilidades.Enums
{
    /// <summary>Codigos de resultado que devuelve el servicio (ver diccionario de datos).</summary>
    public enum CodigoResultado
    {
        Ok200,
        TokenInvalido,
        SesionExpirada,
        CredencialesIncorrectas,
        UnidadNoSoportada,
        DatosInconsistentes,
        ErrorServidor
    }

    public static class CodigoResultadoExtensiones
    {
        /// <summary>Texto que viaja en el cuerpo JSON de la respuesta, por ejemplo "AUTH-001".</summary>
        public static string Texto(this CodigoResultado codigo) => codigo switch
        {
            CodigoResultado.Ok200 => "OK-200",
            CodigoResultado.TokenInvalido => "AUTH-001",
            CodigoResultado.SesionExpirada => "AUTH-002",
            CodigoResultado.CredencialesIncorrectas => "AUTH-003",
            CodigoResultado.UnidadNoSoportada => "UNIT-001",
            CodigoResultado.DatosInconsistentes => "DATA-001",
            _ => "SRV-500"
        };
    }
}
