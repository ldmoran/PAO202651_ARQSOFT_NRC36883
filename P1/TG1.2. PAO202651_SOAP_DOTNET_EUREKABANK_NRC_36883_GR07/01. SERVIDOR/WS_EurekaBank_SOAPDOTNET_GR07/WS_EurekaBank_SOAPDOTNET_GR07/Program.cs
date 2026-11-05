using CoreWCF;
using CoreWCF.Configuration;
using CoreWCF.Description;
using ec.edu.monster.eureka.Datos;
using ec.edu.monster.eureka.WS;

// Servidor SOAP "EurekaBank" (GR07) - .NET 8 + CoreWCF + SQL Server (base eurekabank).
var builder = WebApplication.CreateBuilder(args);
BaseDatos.UsarCadena(builder.Configuration.GetConnectionString(ConexionSqlServer.Llave));

if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("ASPNETCORE_URLS"))
    && !args.Any(a => a.StartsWith("--urls", StringComparison.OrdinalIgnoreCase)))
{
    builder.WebHost.UseUrls("http://localhost:5081");
}

builder.Services.AddServiceModelServices();
builder.Services.AddServiceModelMetadata();
builder.Services.AddSingleton<IServiceBehavior, UseRequestHeadersForMetadataAddressBehavior>();

var app = builder.Build();

app.UseServiceModel(sb =>
{
    sb.AddService<WSEurekaBank>();
    sb.AddServiceEndpoint<WSEurekaBank, IWSEurekaBank>(new BasicHttpBinding(), "/WSEurekaBank");
});

app.Services.GetRequiredService<ServiceMetadataBehavior>().HttpGetEnabled = true;

app.MapGet("/", () => Results.Content(
    """
    <html><head><meta charset="utf-8"><title>WS_EurekaBank_SOAPDOTNET_GR07</title></head><body style="font-family:Segoe UI,Arial;margin:40px">
    <h2>EurekaBank - Servidor SOAP (.NET + SQL Server)</h2>
    <p><a href="/WSEurekaBank?wsdl">WSEurekaBank?wsdl</a></p></body></html>
    """, "text/html"));

app.Run();

public partial class Program { }
