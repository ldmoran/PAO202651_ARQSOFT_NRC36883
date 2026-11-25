using ApiPaises.Models;
using ApiPaises.Services;
using Microsoft.AspNetCore.Mvc;

namespace ApiPaises.Controllers;

/// <summary>Web de universidades: consulta la API publica por pais y/o universidad, guarda lo que sirva y busca en la base.</summary>
public class UniversidadesController : Controller
{
    private readonly ServicioUniversidades _servicio;

    public UniversidadesController(ServicioUniversidades servicio)
    {
        _servicio = servicio;
    }

    public async Task<IActionResult> Index(string? nombre, string? pais)
    {
        ViewData["Seccion"] = "universidades";
        ViewBag.Nombre = nombre;
        ViewBag.Pais = pais ?? "";
        ViewBag.Paises = await _servicio.PaisesConUniversidadesAsync();
        return View(await _servicio.ListarAsync(nombre, pais));
    }

    public async Task<IActionResult> Consultar(string? pais, string? universidad)
    {
        ViewData["Seccion"] = "universidades";
        ViewBag.Pais = pais ?? "";
        ViewBag.Universidad = universidad ?? "";
        ViewBag.Sugerencias = await _servicio.NombresDePaisesAsync();
        if (string.IsNullOrWhiteSpace(pais) && string.IsNullOrWhiteSpace(universidad)) return View(null);
        try
        {
            return View(await _servicio.ConsultarEnApiAsync(pais, universidad));
        }
        catch (Exception e) when (e is HttpRequestException or TaskCanceledException)
        {
            ViewBag.Error = "No se pudo consultar la API de universidades. Revisa tu conexión a internet.";
            return View(new List<Universidad>());
        }
    }

    [HttpPost]
    public async Task<IActionResult> Guardar(string? pais, string? universidad)
    {
        try
        {
            var (recibidas, nuevas) = await _servicio.ImportarAsync(pais, universidad);
            TempData["Aviso"] = nuevas == 0
                ? $"Las {recibidas} universidades ya estaban guardadas"
                : $"{nuevas} universidades guardadas (de {recibidas} encontradas)";
            return RedirectToAction(nameof(Index), new { nombre = universidad, pais = string.IsNullOrWhiteSpace(pais) ? null : ServicioUniversidades.PaisParaLaApi(pais) });
        }
        catch (Exception e) when (e is ErrorDeNegocio or HttpRequestException or TaskCanceledException)
        {
            TempData["Error"] = e is ErrorDeNegocio ? e.Message : "No se pudo consultar la API de universidades.";
            return RedirectToAction(nameof(Consultar), new { pais, universidad });
        }
    }

    [HttpPost]
    public async Task<IActionResult> Eliminar(int id, string? pais)
    {
        try
        {
            await _servicio.EliminarAsync(id);
            TempData["Aviso"] = "Universidad eliminada";
        }
        catch (ErrorDeNegocio e)
        {
            TempData["Error"] = e.Message;
        }
        return RedirectToAction(nameof(Index), new { pais });
    }
}
