namespace ec.edu.monster.Modelos.Peticiones
{
    /// <summary>
    /// Cuerpo JSON de POST /api/conversiones/{longitud|masa|temperatura} (CU-04).
    /// Exclusivo de la variante REST: en SOAP estos mismos datos viajaban como
    /// parametros individuales de la operacion.
    /// </summary>
    public class ConversionRequest
    {
        public double Valor { get; set; }
        public string? UnidadOrigen { get; set; }
        public string? UnidadDestino { get; set; }
    }
}
