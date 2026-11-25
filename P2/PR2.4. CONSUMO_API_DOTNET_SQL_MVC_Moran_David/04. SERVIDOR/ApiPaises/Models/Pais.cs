namespace ApiPaises.Models;

/// <summary>Un pais guardado en la base de datos.</summary>
public class Pais
{
    public int Id { get; set; }
    public string Codigo { get; set; } = "";
    public string Nombre { get; set; } = "";
    public string Capital { get; set; } = "";
    public string Region { get; set; } = "";
    public string Subregion { get; set; } = "";
    public long Poblacion { get; set; }
    public decimal Area { get; set; }
    public string Moneda { get; set; } = "";
    public string Bandera { get; set; } = "";

    /// <summary>Deja los textos limpios (sin nulos ni espacios) y el codigo en mayusculas.</summary>
    public void Normalizar()
    {
        Codigo = (Codigo ?? "").Trim().ToUpperInvariant();
        Nombre = (Nombre ?? "").Trim();
        Capital = (Capital ?? "").Trim();
        Region = (Region ?? "").Trim();
        Subregion = (Subregion ?? "").Trim();
        Moneda = (Moneda ?? "").Trim();
        Bandera = (Bandera ?? "").Trim();
    }
}
