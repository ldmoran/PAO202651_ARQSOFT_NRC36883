using System.Text.Json;
using ApiPaises.Models;

namespace ApiPaises.Services;

/// <summary>Consume la API publica de universidades (universities.hipolabs.com).</summary>
public class ConsumoUniversidades
{
    private readonly HttpClient _http;

    public ConsumoUniversidades(HttpClient http, IConfiguration config)
    {
        _http = http;
        _http.BaseAddress = new Uri((config["UNIVERSIDADES_API_URL"] ?? "http://universities.hipolabs.com").TrimEnd('/') + "/");
        _http.Timeout = TimeSpan.FromSeconds(40);
    }

    /// <summary>Busca por pais (en ingles, como lo pide la API), por parte del nombre de la universidad, o por las dos cosas a la vez.</summary>
    public async Task<List<Universidad>> BuscarAsync(string? paisEnIngles, string? nombre)
    {
        var partes = new List<string>();
        if (!string.IsNullOrWhiteSpace(paisEnIngles)) partes.Add("country=" + Uri.EscapeDataString(paisEnIngles.Trim()));
        if (!string.IsNullOrWhiteSpace(nombre)) partes.Add("name=" + Uri.EscapeDataString(nombre.Trim()));
        var lista = new List<Universidad>();
        if (partes.Count == 0) return lista;

        using var r = await _http.GetAsync("search?" + string.Join("&", partes));
        r.EnsureSuccessStatusCode();
        using var doc = JsonDocument.Parse(await r.Content.ReadAsStringAsync());
        if (doc.RootElement.ValueKind != JsonValueKind.Array) return lista;
        foreach (var o in doc.RootElement.EnumerateArray())
        {
            var n = Texto(o, "name");
            if (n.Length == 0) continue;
            lista.Add(new Universidad
            {
                Nombre = n,
                Pais = Texto(o, "country"),
                CodigoPais = Texto(o, "alpha_two_code"),
                Dominio = Primero(o, "domains"),
                SitioWeb = Primero(o, "web_pages")
            });
        }
        return lista;
    }

    private static string Texto(JsonElement o, string c) =>
        o.TryGetProperty(c, out var e) && e.ValueKind == JsonValueKind.String ? (e.GetString() ?? "").Trim() : "";

    private static string Primero(JsonElement o, string c) =>
        o.TryGetProperty(c, out var e) && e.ValueKind == JsonValueKind.Array && e.GetArrayLength() > 0 && e[0].ValueKind == JsonValueKind.String
            ? (e[0].GetString() ?? "").Trim() : "";
}
