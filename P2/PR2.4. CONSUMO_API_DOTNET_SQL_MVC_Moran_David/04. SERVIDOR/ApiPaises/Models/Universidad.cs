namespace ApiPaises.Models;

/// <summary>Una universidad traida de la API publica de universidades.</summary>
public class Universidad
{
    public int Id { get; set; }
    public string Nombre { get; set; } = "";
    public string Dominio { get; set; } = "";
    public string SitioWeb { get; set; } = "";
    public string Pais { get; set; } = "";
    public string CodigoPais { get; set; } = "";
}
