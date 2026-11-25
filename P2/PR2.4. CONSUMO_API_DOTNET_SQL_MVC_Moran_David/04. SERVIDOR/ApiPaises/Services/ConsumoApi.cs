using System.Text.Json;
using ApiPaises.Models;

namespace ApiPaises.Services;

/// <summary>Consume la API publica de paises y convierte el JSON en objetos Pais (region y subregion en espanol).</summary>
public class ConsumoApi
{
    private readonly HttpClient _http;

    public ConsumoApi(HttpClient http, IConfiguration config)
    {
        _http = http;
        _http.BaseAddress = new Uri((config["PAISES_API_URL"] ?? "https://www.apicountries.com").TrimEnd('/') + "/");
        _http.Timeout = TimeSpan.FromSeconds(40);
    }

    public Task<List<Pais>> ObtenerTodosAsync() => LeerAsync("countries");

    public Task<List<Pais>> ObtenerPorNombreAsync(string nombre) => LeerAsync("name/" + Uri.EscapeDataString(nombre.Trim()));

    private async Task<List<Pais>> LeerAsync(string ruta)
    {
        using var r = await _http.GetAsync(ruta);
        if (r.StatusCode == System.Net.HttpStatusCode.NotFound) return new();
        r.EnsureSuccessStatusCode();
        return Convertir(await r.Content.ReadAsStringAsync());
    }

    public static List<Pais> Convertir(string json)
    {
        var lista = new List<Pais>();
        using var doc = JsonDocument.Parse(json);
        if (doc.RootElement.ValueKind != JsonValueKind.Array) return lista;
        foreach (var o in doc.RootElement.EnumerateArray())
        {
            var nombre = Texto(o, "name");
            var codigo = Texto(o, "alpha3Code");
            if (nombre.Length == 0 || codigo.Length != 3) continue;
            var alfa2 = Texto(o, "alpha2Code").ToLowerInvariant();
            var p = new Pais
            {
                Codigo = codigo,
                Nombre = nombre,
                Capital = Texto(o, "capital"),
                Region = RegionEnEspanol(Texto(o, "region")),
                Subregion = SubregionEnEspanol(Texto(o, "subregion")),
                Poblacion = (long)Numero(o, "population"),
                Area = (decimal)Numero(o, "area"),
                Moneda = Moneda(o),
                Bandera = alfa2.Length == 2 ? $"https://flagcdn.com/w320/{alfa2}.png" : ""
            };
            p.Normalizar();
            lista.Add(p);
        }
        return lista;
    }

    private static string Moneda(JsonElement o)
    {
        if (o.TryGetProperty("currencies", out var m) && m.ValueKind == JsonValueKind.Array && m.GetArrayLength() > 0)
        {
            var c = m[0];
            var nombre = Texto(c, "name");
            var cod = Texto(c, "code");
            return cod.Length == 0 ? nombre : nombre.Length == 0 ? cod : $"{nombre} ({cod})";
        }
        return "";
    }

    private static string Texto(JsonElement o, string clave) =>
        o.TryGetProperty(clave, out var e) && e.ValueKind == JsonValueKind.String ? (e.GetString() ?? "").Trim() : "";

    private static double Numero(JsonElement o, string clave) =>
        o.TryGetProperty(clave, out var e) && e.ValueKind == JsonValueKind.Number && e.TryGetDouble(out var d) ? d : 0;

    public static string RegionEnEspanol(string r) => (r ?? "").Trim().ToLowerInvariant() switch
    {
        "americas" or "america" => "América",
        "africa" => "África",
        "europe" => "Europa",
        "asia" => "Asia",
        "oceania" => "Oceanía",
        "antarctic" or "antarctic ocean" or "polar" => "Antártida",
        "" => "Sin región",
        _ => r.Trim()
    };

    public static string SubregionEnEspanol(string s) => (s ?? "").Trim() switch
    {
        "Northern Africa" => "África del Norte",
        "Western Africa" => "África Occidental",
        "Eastern Africa" => "África Oriental",
        "Middle Africa" => "África Central",
        "Southern Africa" => "África Austral",
        "Northern America" => "América del Norte",
        "Central America" => "América Central",
        "South America" => "América del Sur",
        "Caribbean" => "Caribe",
        "Central Asia" => "Asia Central",
        "Eastern Asia" => "Asia Oriental",
        "South-Eastern Asia" => "Sudeste Asiático",
        "Southern Asia" => "Asia Meridional",
        "Western Asia" => "Asia Occidental",
        "Eastern Europe" => "Europa Oriental",
        "Northern Europe" => "Europa del Norte",
        "Southern Europe" => "Europa del Sur",
        "Western Europe" => "Europa Occidental",
        "Central Europe" => "Europa Central",
        "Australia and New Zealand" => "Australia y Nueva Zelanda",
        "Polynesia" => "Polinesia",
        var otro => otro
    };
}
