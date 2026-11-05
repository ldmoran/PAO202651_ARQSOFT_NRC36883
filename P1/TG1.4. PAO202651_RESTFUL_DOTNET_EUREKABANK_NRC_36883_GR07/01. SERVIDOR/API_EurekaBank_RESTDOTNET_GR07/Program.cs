using ec.edu.monster.eureka.Datos;
using Microsoft.OpenApi.Models;

// Servidor REST "EurekaBank" (GR07) - .NET 8 + ASP.NET Core Web API + SQL Server (base eurekabank).
var builder = WebApplication.CreateBuilder(args);
BaseDatos.UsarCadena(builder.Configuration.GetConnectionString(ConexionSqlServer.Llave));

if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("ASPNETCORE_URLS"))
    && !args.Any(a => a.StartsWith("--urls", StringComparison.OrdinalIgnoreCase)))
{
    builder.WebHost.UseUrls("http://localhost:5081");
}

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "API_EurekaBank_RESTDOTNET_GR07",
        Version = "v1",
        Description = "EurekaBank (GR07) - API REST en .NET + SQL Server."
    });
});

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "API_EurekaBank_RESTDOTNET_GR07 v1"));

app.MapControllers();

app.MapGet("/", () => Results.Content(
    """
    <html><head><meta charset="utf-8"><title>API_EurekaBank_RESTDOTNET_GR07</title></head><body style="font-family:Segoe UI,Arial;margin:40px">
    <h2>EurekaBank - Servidor REST (.NET + SQL Server)</h2>
    <ul>
      <li><a href="/swagger">Documentación interactiva (Swagger / OpenAPI)</a></li>
      <li>POST /api/sesiones</li>
      <li>GET /api/cuentas</li>
      <li>GET /api/movimientos?cuenta=00100001</li>
      <li>GET /api/movimientos/tipos</li>
      <li>POST /api/movimientos</li>
    </ul></body></html>
    """, "text/html"));

app.Run();

public partial class Program { }
