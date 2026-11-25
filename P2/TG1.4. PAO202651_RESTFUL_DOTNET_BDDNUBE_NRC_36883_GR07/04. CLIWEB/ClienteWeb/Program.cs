using ClienteWeb.Components;
using ec.edu.monster.Cliente.Servicios;

// Cliente web de EurekaBank (Blazor Server). El navegador nunca habla REST directamente: este servidor
// web llama al servicio REST y mantiene la sesion en el circuito de cada usuario.
//
// URL del servicio REST: variable EUREKA_URL o http://localhost:5081
var builder = WebApplication.CreateBuilder(args);

if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("ASPNETCORE_URLS"))
    && !args.Any(a => a.StartsWith("--urls", StringComparison.OrdinalIgnoreCase)))
{
    builder.WebHost.UseUrls("http://localhost:5090");
}

builder.WebHost.UseStaticWebAssets(); // sirve _content/... de la libreria compartida en cualquier entorno

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();
builder.Services.AgregarClienteEureka();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
}
app.UseStatusCodePagesWithReExecute("/not-found", createScopeForStatusCodePages: true);

app.UseAntiforgery();

app.MapStaticAssets();
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode()
    .AddAdditionalAssemblies(typeof(SesionCliente).Assembly);

app.Run();
