using System.Globalization;
using System.Text;
using ApiPaises.Data;
using ApiPaises.Models;
using Microsoft.EntityFrameworkCore;

namespace ApiPaises.Services;

/// <summary>Consulta universidades en la API publica, las guarda y permite buscarlas en la base.</summary>
public class ServicioUniversidades
{
    // La API solo entiende los paises en ingles. Estos son los que la gente suele escribir en espanol.
    private static readonly Dictionary<string, string> PaisesEnEspanol = new()
    {
        ["alemania"] = "Germany", ["arabia saudita"] = "Saudi Arabia", ["argelia"] = "Algeria", ["belgica"] = "Belgium",
        ["brasil"] = "Brazil", ["canada"] = "Canada", ["catar"] = "Qatar", ["corea del sur"] = "South Korea", ["dinamarca"] = "Denmark",
        ["egipto"] = "Egypt", ["emiratos arabes unidos"] = "United Arab Emirates", ["escocia"] = "United Kingdom", ["eslovaquia"] = "Slovakia",
        ["espana"] = "Spain", ["estados unidos"] = "United States", ["eeuu"] = "United States", ["usa"] = "United States", ["filipinas"] = "Philippines",
        ["finlandia"] = "Finland", ["francia"] = "France", ["grecia"] = "Greece", ["holanda"] = "Netherlands", ["paises bajos"] = "Netherlands",
        ["hungria"] = "Hungary", ["india"] = "India", ["inglaterra"] = "United Kingdom", ["reino unido"] = "United Kingdom", ["irlanda"] = "Ireland",
        ["italia"] = "Italy", ["japon"] = "Japan", ["marruecos"] = "Morocco", ["mexico"] = "Mexico", ["noruega"] = "Norway",
        ["nueva zelanda"] = "New Zealand", ["peru"] = "Peru", ["polonia"] = "Poland", ["rep dominicana"] = "Dominican Republic",
        ["republica dominicana"] = "Dominican Republic", ["rumania"] = "Romania", ["rusia"] = "Russian Federation", ["suecia"] = "Sweden",
        ["suiza"] = "Switzerland", ["sudafrica"] = "South Africa", ["tailandia"] = "Thailand", ["turquia"] = "Turkey", ["ucrania"] = "Ukraine"
    };

    private readonly PaisesContext _db;
    private readonly ConsumoUniversidades _api;

    public ServicioUniversidades(PaisesContext db, ConsumoUniversidades api)
    {
        _db = db;
        _api = api;
    }

    /// <summary>Convierte "España" o "ecuador" en el nombre que entiende la API ("Spain", "Ecuador"). Lo demas pasa igual.</summary>
    public static string PaisParaLaApi(string? texto)
    {
        var t = (texto ?? "").Trim();
        if (t.Length == 0) return "";
        return PaisesEnEspanol.TryGetValue(SinTildes(t), out var ingles) ? ingles : t;
    }

    private static string SinTildes(string s)
    {
        var d = s.Normalize(NormalizationForm.FormD);
        var sb = new StringBuilder();
        foreach (var c in d) if (CharUnicodeInfo.GetUnicodeCategory(c) != UnicodeCategory.NonSpacingMark) sb.Append(c);
        return sb.ToString().ToLowerInvariant().Replace(".", "").Trim();
    }

    /// <summary>Busca en la API por pais, por universidad o por las dos cosas. Hace falta al menos una.</summary>
    public async Task<List<Universidad>> ConsultarEnApiAsync(string? pais, string? universidad)
    {
        if (string.IsNullOrWhiteSpace(pais) && string.IsNullOrWhiteSpace(universidad))
            throw new ErrorDeNegocio("Escribe un país, el nombre de una universidad, o las dos cosas");
        var paisApi = PaisParaLaApi(pais);
        if (paisApi.Length > 0 && !string.IsNullOrWhiteSpace(universidad))
        {
            // La API distingue tildes ("Politecnica" no encuentra "Politécnica"), asi que con pais + universidad se filtra aqui, sin tildes.
            var buscado = SinTildes(universidad);
            return (await _api.BuscarAsync(paisApi, null)).Where(u => SinTildes(u.Nombre).Contains(buscado)).ToList();
        }
        return await _api.BuscarAsync(paisApi, universidad);
    }

    /// <summary>Guarda lo encontrado sin duplicar (mismo nombre y pais). Devuelve cuantas son nuevas.</summary>
    public async Task<(int Recibidas, int Nuevas)> ImportarAsync(string? pais, string? universidad)
    {
        var recibidas = await ConsultarEnApiAsync(pais, universidad);
        if (recibidas.Count == 0) throw new ErrorDeNegocio("La API no tiene universidades para esa búsqueda", 404);
        var paisesRecibidos = recibidas.Select(u => u.Pais).Distinct().ToList();
        var ya = (await _db.Universidades.Where(u => paisesRecibidos.Contains(u.Pais)).Select(u => u.Pais + "|" + u.Nombre).ToListAsync())
            .ToHashSet(StringComparer.OrdinalIgnoreCase);
        int nuevas = 0;
        foreach (var u in recibidas)
        {
            if (!ya.Add(u.Pais + "|" + u.Nombre)) continue;
            _db.Universidades.Add(u);
            nuevas++;
        }
        await _db.SaveChangesAsync();
        return (recibidas.Count, nuevas);
    }

    public async Task<List<Universidad>> ListarAsync(string? nombre, string? pais)
    {
        IQueryable<Universidad> q = _db.Universidades.AsNoTracking();
        if (!string.IsNullOrWhiteSpace(nombre)) q = q.Where(u => u.Nombre.Contains(nombre.Trim()));
        if (!string.IsNullOrWhiteSpace(pais)) q = q.Where(u => u.Pais == pais.Trim());
        return await q.OrderBy(u => u.Pais).ThenBy(u => u.Nombre).ToListAsync();
    }

    public Task<List<string>> PaisesConUniversidadesAsync() =>
        _db.Universidades.AsNoTracking().Select(u => u.Pais).Distinct().OrderBy(p => p).ToListAsync();

    /// <summary>Los nombres de la tabla de paises (para sugerirlos mientras se escribe).</summary>
    public Task<List<string>> NombresDePaisesAsync() =>
        _db.Paises.AsNoTracking().OrderBy(p => p.Nombre).Select(p => p.Nombre).ToListAsync();

    public async Task EliminarAsync(int id)
    {
        var u = await _db.Universidades.FirstOrDefaultAsync(x => x.Id == id) ?? throw new ErrorDeNegocio("No existe esa universidad", 404);
        _db.Universidades.Remove(u);
        await _db.SaveChangesAsync();
    }

    public Task<int> ContarAsync() => _db.Universidades.CountAsync();
}
