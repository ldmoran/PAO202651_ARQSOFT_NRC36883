namespace ApiPaises.Models;

public record FilaRegion(string Region, int Paises, long PoblacionTotal, long Promedio);

public record Reporte(List<FilaRegion> PorRegion, Pais? MasPoblado, int Total);
