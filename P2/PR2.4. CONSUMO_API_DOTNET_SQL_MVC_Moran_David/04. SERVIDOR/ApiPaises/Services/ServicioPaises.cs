using ApiPaises.Data;
using ApiPaises.Models;
using Microsoft.EntityFrameworkCore;

namespace ApiPaises.Services;

public record ResultadoImportacion(int Recibidos, int Nuevos, int Actualizados, int Total);

/// <summary>Reglas del taller: importar sin duplicar, validar, buscar y armar los reportes. Los controladores solo llaman aqui.</summary>
public class ServicioPaises
{
    private readonly PaisesContext _db;
    private readonly ConsumoApi _api;

    public ServicioPaises(PaisesContext db, ConsumoApi api)
    {
        _db = db;
        _api = api;
    }

    // ------------------------------------------------------------------ importar y consultar la API
    public async Task<ResultadoImportacion> ImportarAsync()
    {
        var recibidos = await _api.ObtenerTodosAsync();
        var existentes = await _db.Paises.ToDictionaryAsync(p => p.Codigo);
        int nuevos = 0, actualizados = 0;
        foreach (var p in recibidos)
        {
            if (existentes.TryGetValue(p.Codigo, out var actual))
            {
                Copiar(p, actual);
                actualizados++;
            }
            else
            {
                _db.Paises.Add(p);
                existentes[p.Codigo] = p;
                nuevos++;
            }
        }
        await _db.SaveChangesAsync();
        return new ResultadoImportacion(recibidos.Count, nuevos, actualizados, await _db.Paises.CountAsync());
    }

    public async Task<List<Pais>> ConsultarEnApiAsync(string? nombre)
    {
        if (string.IsNullOrWhiteSpace(nombre)) throw new ErrorDeNegocio("Escribe el nombre de un país");
        return await _api.ObtenerPorNombreAsync(nombre);
    }

    public async Task<Pais> GuardarDesdeApiAsync(string nombre, string codigo)
    {
        var encontrado = (await ConsultarEnApiAsync(nombre)).FirstOrDefault(p => p.Codigo.Equals(codigo, StringComparison.OrdinalIgnoreCase))
            ?? throw new ErrorDeNegocio("No se encontró ese país en la API", 404);
        var actual = await _db.Paises.FirstOrDefaultAsync(p => p.Codigo == encontrado.Codigo);
        if (actual == null) _db.Paises.Add(actual = encontrado);
        else Copiar(encontrado, actual);
        await _db.SaveChangesAsync();
        return actual;
    }

    // ------------------------------------------------------------------ consultas y CRUD
    public async Task<List<Pais>> ListarAsync(string? nombre, string? capital, string? region)
    {
        IQueryable<Pais> q = _db.Paises.AsNoTracking();
        if (!string.IsNullOrWhiteSpace(nombre)) q = q.Where(p => p.Nombre.Contains(nombre.Trim()));
        if (!string.IsNullOrWhiteSpace(capital)) q = q.Where(p => p.Capital.Contains(capital.Trim()));
        if (!string.IsNullOrWhiteSpace(region)) q = q.Where(p => p.Region == region.Trim());
        return await q.OrderBy(p => p.Nombre).ToListAsync();
    }

    public async Task<Pais> ObtenerAsync(int id) =>
        await _db.Paises.AsNoTracking().FirstOrDefaultAsync(p => p.Id == id) ?? throw new ErrorDeNegocio("No existe ese país", 404);

    public async Task<Pais> CrearAsync(Pais p)
    {
        p.Id = 0;
        Validar(p);
        if (await _db.Paises.AnyAsync(x => x.Codigo == p.Codigo)) throw new ErrorDeNegocio("Ya existe un país con ese código");
        _db.Paises.Add(p);
        await _db.SaveChangesAsync();
        return p;
    }

    public async Task<Pais> ActualizarAsync(int id, Pais p)
    {
        Validar(p);
        var actual = await _db.Paises.FirstOrDefaultAsync(x => x.Id == id) ?? throw new ErrorDeNegocio("No existe ese país", 404);
        if (await _db.Paises.AnyAsync(x => x.Codigo == p.Codigo && x.Id != id)) throw new ErrorDeNegocio("Ya existe otro país con ese código");
        Copiar(p, actual);
        await _db.SaveChangesAsync();
        return actual;
    }

    public async Task EliminarAsync(int id)
    {
        var actual = await _db.Paises.FirstOrDefaultAsync(x => x.Id == id) ?? throw new ErrorDeNegocio("No existe ese país", 404);
        _db.Paises.Remove(actual);
        await _db.SaveChangesAsync();
    }

    public Task<int> ContarAsync() => _db.Paises.CountAsync();

    // ------------------------------------------------------------------ reportes
    public async Task<Reporte> ReportesAsync()
    {
        var filas = (await _db.Paises.AsNoTracking()
                .GroupBy(p => p.Region)
                .Select(g => new { Region = g.Key, Paises = g.Count(), Total = g.Sum(p => p.Poblacion) })
                .ToListAsync())
            .OrderByDescending(f => f.Paises).ThenBy(f => f.Region)
            .Select(f => new FilaRegion(f.Region, f.Paises, f.Total, f.Paises == 0 ? 0 : f.Total / f.Paises))
            .ToList();
        var mas = await _db.Paises.AsNoTracking().OrderByDescending(p => p.Poblacion).FirstOrDefaultAsync();
        return new Reporte(filas, mas, await _db.Paises.CountAsync());
    }

    // ------------------------------------------------------------------ interno
    private static void Validar(Pais p)
    {
        p.Normalizar();
        if (p.Nombre.Length == 0) throw new ErrorDeNegocio("El nombre es obligatorio");
        if (p.Codigo.Length != 3 || !p.Codigo.All(c => c is >= 'A' and <= 'Z')) throw new ErrorDeNegocio("El código debe tener 3 letras, por ejemplo ECU");
        if (p.Region.Length == 0) throw new ErrorDeNegocio("La región es obligatoria");
        if (p.Poblacion < 0) throw new ErrorDeNegocio("La población no puede ser negativa");
        if (p.Area < 0) throw new ErrorDeNegocio("El área no puede ser negativa");
    }

    private static void Copiar(Pais de, Pais a)
    {
        a.Codigo = de.Codigo; a.Nombre = de.Nombre; a.Capital = de.Capital; a.Region = de.Region; a.Subregion = de.Subregion;
        a.Poblacion = de.Poblacion; a.Area = de.Area; a.Moneda = de.Moneda; a.Bandera = de.Bandera;
    }
}
