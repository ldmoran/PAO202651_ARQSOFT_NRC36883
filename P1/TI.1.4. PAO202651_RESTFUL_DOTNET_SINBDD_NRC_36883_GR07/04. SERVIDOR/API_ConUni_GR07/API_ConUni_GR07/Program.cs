using Microsoft.OpenApi.Models;

// Servidor REST "Conversion de Unidades Monster" (GR07) - .NET 8 + ASP.NET Core Web API.
// Sin base de datos: credenciales, catalogo y tokens viven en memoria.
var builder = WebApplication.CreateBuilder(args);

if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("ASPNETCORE_URLS"))
    && !args.Any(a => a.StartsWith("--urls", StringComparison.OrdinalIgnoreCase)))
{
    builder.WebHost.UseUrls("http://localhost:5080");
}

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "API_ConUni_GR07",
        Version = "v1",
        Description = "Conversión de Unidades Monster (GR07) - API REST en .NET, sin base de datos."
    });
});

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "API_ConUni_GR07 v1"));

app.MapControllers();

app.MapGet("/", () => Results.Content(
    """
    <html><head><meta charset="utf-8"><title>API_ConUni_GR07</title></head><body>
    <h2>Servidor REST - Conversión de Unidades Monster (GR07, .NET)</h2>
    <ul>
      <li><a href="/swagger">Documentación interactiva (Swagger / OpenAPI)</a></li>
      <li>POST /api/auth/login</li>
      <li>POST /api/auth/logout</li>
      <li>GET /api/catalogo</li>
      <li>GET /api/catalogo/{categoria}</li>
      <li>POST /api/conversiones/longitud</li>
      <li>POST /api/conversiones/masa</li>
      <li>POST /api/conversiones/temperatura</li>
    </ul></body></html>
    """, "text/html"));

app.Run();

/// <summary>Permite que las pruebas de integracion arranquen el servidor completo.</summary>
public partial class Program { }
