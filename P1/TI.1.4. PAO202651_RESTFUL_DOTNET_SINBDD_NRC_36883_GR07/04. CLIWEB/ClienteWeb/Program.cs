using ClienteWeb.Components;
using ec.edu.monster.Cliente.Servicios;

// Cliente web de "Conversion de Unidades Monster" (Blazor Server). El navegador nunca habla con
// la API REST ni conoce el token: este servidor web llama a la API y guarda el token en la
// sesion (circuito) de cada usuario.
//
// URL de la API REST: variable CONUNI_URL o http://localhost:5080/
var builder = WebApplication.CreateBuilder(args);

if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("ASPNETCORE_URLS"))
    && !args.Any(a => a.StartsWith("--urls", StringComparison.OrdinalIgnoreCase)))
{
    builder.WebHost.UseUrls("http://localhost:5090");
}

builder.WebHost.UseStaticWebAssets(); // sirve _content/... de la libreria compartida en cualquier entorno

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();
builder.Services.AgregarClienteMonster();

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
