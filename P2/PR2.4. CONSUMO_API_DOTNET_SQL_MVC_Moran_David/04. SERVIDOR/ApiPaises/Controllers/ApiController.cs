using ApiPaises.Models;
using ApiPaises.Services;
using Microsoft.AspNetCore.Mvc;

namespace ApiPaises.Controllers;

/// <summary>
/// API REST en JSON para el cliente movil. Usa las mismas rutas que el servidor de Java, asi la app Android sirve con los dos.
/// Los errores salen como {"error": "mensaje"}.
/// </summary>
[ApiController]
[IgnoreAntiforgeryToken]
[Route("api")]
public class ApiController : ControllerBase
{
    public record SolicitudGuardar(string Nombre, string Codigo);
    public record SolicitudUniversidades(string? Pais, string? Universidad);

    private readonly ServicioPaises _paises;
    private readonly ServicioUniversidades _universidades;

    public ApiController(ServicioPaises paises, ServicioUniversidades universidades)
    {
        _paises = paises;
        _universidades = universidades;
    }

    private async Task<IActionResult> Ejecutar(Func<Task<IActionResult>> accion)
    {
        try
        {
            return await accion();
        }
        catch (ErrorDeNegocio e)
        {
            return StatusCode(e.Codigo, new { error = e.Message });
        }
        catch (Exception e) when (e is HttpRequestException or TaskCanceledException)
        {
            return StatusCode(502, new { error = "No se pudo consultar la API pública. Revisa la conexión a internet del servidor." });
        }
    }

    [HttpGet("salud")]
    public IActionResult Salud() => Ok(new { estado = "ok" });

    // ------------------------------------------------------------------ paises
    [HttpGet("paises")]
    public Task<IActionResult> Listar(string? nombre, string? capital, string? region) =>
        Ejecutar(async () => Ok(await _paises.ListarAsync(nombre, capital, region)));

    [HttpGet("paises/{id:int}")]
    public Task<IActionResult> Obtener(int id) => Ejecutar(async () => Ok(await _paises.ObtenerAsync(id)));

    [HttpPost("paises")]
    public Task<IActionResult> Crear(Pais pais) => Ejecutar(async () => StatusCode(201, await _paises.CrearAsync(pais)));

    [HttpPut("paises/{id:int}")]
    public Task<IActionResult> Actualizar(int id, Pais pais) => Ejecutar(async () => Ok(await _paises.ActualizarAsync(id, pais)));

    [HttpDelete("paises/{id:int}")]
    public Task<IActionResult> Eliminar(int id) => Ejecutar(async () =>
    {
        await _paises.EliminarAsync(id);
        return NoContent();
    });

    [HttpPost("importar")]
    public Task<IActionResult> Importar() => Ejecutar(async () => Ok(await _paises.ImportarAsync()));

    // ------------------------------------------------------------------ API publica de paises
    [HttpGet("externo")]
    public Task<IActionResult> Externo(string? nombre) => Ejecutar(async () => Ok(await _paises.ConsultarEnApiAsync(nombre)));

    [HttpPost("externo/guardar")]
    public Task<IActionResult> GuardarExterno(SolicitudGuardar s) =>
        Ejecutar(async () => Ok(await _paises.GuardarDesdeApiAsync(s.Nombre, s.Codigo)));

    // ------------------------------------------------------------------ reportes
    [HttpGet("reportes")]
    public Task<IActionResult> Reportes() => Ejecutar(async () => Ok(await _paises.ReportesAsync()));

    // ------------------------------------------------------------------ universidades
    [HttpGet("universidades")]
    public Task<IActionResult> ListarUniversidades(string? nombre, string? pais) =>
        Ejecutar(async () => Ok(await _universidades.ListarAsync(nombre, pais)));

    [HttpGet("universidades/paises")]
    public Task<IActionResult> PaisesConUniversidades() => Ejecutar(async () => Ok(await _universidades.PaisesConUniversidadesAsync()));

    [HttpGet("universidades/externo")]
    public Task<IActionResult> UniversidadesExterno(string? pais, string? universidad) =>
        Ejecutar(async () => Ok(await _universidades.ConsultarEnApiAsync(pais, universidad)));

    [HttpPost("universidades/importar")]
    public Task<IActionResult> ImportarUniversidades(SolicitudUniversidades s) => Ejecutar(async () =>
    {
        var (recibidas, nuevas) = await _universidades.ImportarAsync(s.Pais, s.Universidad);
        return Ok(new { recibidas, nuevas });
    });

    [HttpDelete("universidades/{id:int}")]
    public Task<IActionResult> EliminarUniversidad(int id) => Ejecutar(async () =>
    {
        await _universidades.EliminarAsync(id);
        return NoContent();
    });
}
