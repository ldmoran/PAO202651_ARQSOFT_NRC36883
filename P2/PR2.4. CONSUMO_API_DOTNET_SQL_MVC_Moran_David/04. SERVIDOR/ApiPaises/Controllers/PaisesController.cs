using ApiPaises.Models;
using ApiPaises.Services;
using Microsoft.AspNetCore.Mvc;

namespace ApiPaises.Controllers;

/// <summary>Controlador MVC de la web: recibe lo que hace el usuario, llama al servicio y elige la vista.</summary>
public class PaisesController : Controller
{
    public static readonly string[] Regiones = { "América", "Europa", "Asia", "África", "Oceanía", "Antártida" };

    private readonly ServicioPaises _servicio;

    public PaisesController(ServicioPaises servicio)
    {
        _servicio = servicio;
    }

    // ------------------------------------------------------------------ lista, busqueda y filtro por region
    public async Task<IActionResult> Index(string? nombre, string? capital, string? region)
    {
        ViewData["Seccion"] = "paises";
        ViewBag.Nombre = nombre;
        ViewBag.Capital = capital;
        ViewBag.Region = region ?? "";
        return View(await _servicio.ListarAsync(nombre, capital, region));
    }

    // ------------------------------------------------------------------ crear, editar y eliminar
    public IActionResult Crear()
    {
        ViewData["Seccion"] = "paises";
        return View("Formulario", new Pais { Region = Regiones[0] });
    }

    [HttpPost]
    public async Task<IActionResult> Crear(Pais pais)
    {
        ViewData["Seccion"] = "paises";
        try
        {
            await _servicio.CrearAsync(pais);
            TempData["Aviso"] = "País creado";
            return RedirectToAction(nameof(Index));
        }
        catch (ErrorDeNegocio e)
        {
            ViewBag.Error = e.Message;
            return View("Formulario", pais);
        }
    }

    public async Task<IActionResult> Editar(int id)
    {
        ViewData["Seccion"] = "paises";
        try
        {
            return View("Formulario", await _servicio.ObtenerAsync(id));
        }
        catch (ErrorDeNegocio e)
        {
            TempData["Error"] = e.Message;
            return RedirectToAction(nameof(Index));
        }
    }

    [HttpPost]
    public async Task<IActionResult> Editar(int id, Pais pais)
    {
        ViewData["Seccion"] = "paises";
        try
        {
            await _servicio.ActualizarAsync(id, pais);
            TempData["Aviso"] = "País guardado";
            return RedirectToAction(nameof(Index));
        }
        catch (ErrorDeNegocio e)
        {
            pais.Id = id;
            ViewBag.Error = e.Message;
            return View("Formulario", pais);
        }
    }

    [HttpPost]
    public async Task<IActionResult> Eliminar(int id)
    {
        try
        {
            await _servicio.EliminarAsync(id);
            TempData["Aviso"] = "País eliminado";
        }
        catch (ErrorDeNegocio e)
        {
            TempData["Error"] = e.Message;
        }
        return RedirectToAction(nameof(Index));
    }

    // ------------------------------------------------------------------ importar desde la API
    [HttpPost]
    public async Task<IActionResult> Importar()
    {
        try
        {
            var r = await _servicio.ImportarAsync();
            TempData["Aviso"] = $"{r.Recibidos} recibidos: {r.Nuevos} nuevos y {r.Actualizados} actualizados";
        }
        catch (HttpRequestException)
        {
            TempData["Error"] = "No se pudo descargar la lista de la API pública. Revisa tu conexión a internet.";
        }
        catch (TaskCanceledException)
        {
            TempData["Error"] = "La API pública tardó demasiado en responder. Intenta de nuevo.";
        }
        return RedirectToAction(nameof(Index));
    }

    // ------------------------------------------------------------------ consultar la API por nombre
    public async Task<IActionResult> Consultar(string? nombre)
    {
        ViewData["Seccion"] = "api";
        ViewBag.Nombre = nombre;
        if (string.IsNullOrWhiteSpace(nombre)) return View(null);
        try
        {
            return View(await _servicio.ConsultarEnApiAsync(nombre));
        }
        catch (Exception e) when (e is HttpRequestException or TaskCanceledException)
        {
            ViewBag.Error = "No se pudo consultar la API pública. Revisa tu conexión a internet.";
            return View(new List<Pais>());
        }
    }

    [HttpPost]
    public async Task<IActionResult> GuardarDesdeApi(string nombre, string codigo)
    {
        try
        {
            var p = await _servicio.GuardarDesdeApiAsync(nombre, codigo);
            TempData["Aviso"] = $"{p.Nombre} se guardó en la base de datos";
        }
        catch (Exception e) when (e is ErrorDeNegocio or HttpRequestException or TaskCanceledException)
        {
            TempData["Error"] = e is ErrorDeNegocio ? e.Message : "No se pudo consultar la API pública.";
        }
        return RedirectToAction(nameof(Consultar), new { nombre });
    }

    // ------------------------------------------------------------------ reportes
    public async Task<IActionResult> Reportes()
    {
        ViewData["Seccion"] = "reportes";
        return View(await _servicio.ReportesAsync());
    }

    public IActionResult Error() => View();
}
