using CoreWCF;
using CoreWCF.Configuration;
using CoreWCF.Description;
using ec.edu.monster.Controladores.WS;

// Servidor SOAP "Conversion de Unidades Monster" (GR07) - .NET 8 + CoreWCF.
// Sin base de datos: credenciales, catalogo y tokens viven en memoria.
var builder = WebApplication.CreateBuilder(args);

if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("ASPNETCORE_URLS"))
    && !args.Any(a => a.StartsWith("--urls", StringComparison.OrdinalIgnoreCase)))
{
    builder.WebHost.UseUrls("http://localhost:5080");
}

builder.Services.AddServiceModelServices();
builder.Services.AddServiceModelMetadata();
builder.Services.AddSingleton<IServiceBehavior, UseRequestHeadersForMetadataAddressBehavior>();

var app = builder.Build();

app.UseServiceModel(sb =>
{
    sb.AddService<WSLogin>();
    sb.AddServiceEndpoint<WSLogin, IWSLogin>(new BasicHttpBinding(), "/WSLogin");
    sb.AddService<WSCatalogo>();
    sb.AddServiceEndpoint<WSCatalogo, IWSCatalogo>(new BasicHttpBinding(), "/WSCatalogo");
    sb.AddService<WSLongitud>();
    sb.AddServiceEndpoint<WSLongitud, IWSLongitud>(new BasicHttpBinding(), "/WSLongitud");
    sb.AddService<WSMasa>();
    sb.AddServiceEndpoint<WSMasa, IWSMasa>(new BasicHttpBinding(), "/WSMasa");
    sb.AddService<WSTemperatura>();
    sb.AddServiceEndpoint<WSTemperatura, IWSTemperatura>(new BasicHttpBinding(), "/WSTemperatura");
});

var metadata = app.Services.GetRequiredService<ServiceMetadataBehavior>();
metadata.HttpGetEnabled = true;

app.MapGet("/", () => Results.Content(
    """
    <html><head><meta charset="utf-8"><title>WS_ConUni_DotNet_GR07</title></head><body>
    <h2>Servidor SOAP - Conversión de Unidades Monster (GR07, .NET)</h2>
    <ul>
      <li><a href="/WSLogin?wsdl">WSLogin</a></li>
      <li><a href="/WSCatalogo?wsdl">WSCatalogo</a></li>
      <li><a href="/WSLongitud?wsdl">WSLongitud</a></li>
      <li><a href="/WSMasa?wsdl">WSMasa</a></li>
      <li><a href="/WSTemperatura?wsdl">WSTemperatura</a></li>
    </ul></body></html>
    """, "text/html"));

app.Run();

/// <summary>Permite que las pruebas de integracion arranquen el servidor completo.</summary>
public partial class Program { }
