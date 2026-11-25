using System.Globalization;
using ApiPaises.Data;
using ApiPaises.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

// Numeros y fechas en formato de Ecuador (1.402.112.000 y coma decimal) en todas las pantallas.
var cultura = new CultureInfo("es-EC");
CultureInfo.DefaultThreadCurrentCulture = cultura;
CultureInfo.DefaultThreadCurrentUICulture = cultura;

var builder = WebApplication.CreateBuilder(args);

// Servidor: puerto 8091 (el de Java usa 8090). Se puede cambiar con ASPNETCORE_URLS.
if (Environment.GetEnvironmentVariable("ASPNETCORE_URLS") == null)
    builder.WebHost.UseUrls("http://0.0.0.0:8091");

builder.Services.AddControllersWithViews(o =>
{
    o.SuppressImplicitRequiredAttributeForNonNullableReferenceTypes = true;
    o.Filters.Add(new AutoValidateAntiforgeryTokenAttribute());
});

// Base de datos: variable de entorno PAISES_DB_CONEXION, o la cadena de appsettings.json (SQL Server local con usuario de Windows).
// Para pasarla a AWS RDS solo se cambia esa variable de entorno.
var conexion = Environment.GetEnvironmentVariable("PAISES_DB_CONEXION") ?? builder.Configuration.GetConnectionString("Paises");
builder.Services.AddDbContext<PaisesContext>(o => o.UseSqlServer(conexion));

builder.Services.AddHttpClient<ConsumoApi>();
builder.Services.AddHttpClient<ConsumoUniversidades>();
builder.Services.AddScoped<ServicioPaises>();
builder.Services.AddScoped<ServicioUniversidades>();

var app = builder.Build();

// Crea la base y las tablas con las migraciones de Entity Framework si todavia no existen.
using (var alcance = app.Services.CreateScope())
{
    alcance.ServiceProvider.GetRequiredService<PaisesContext>().Database.Migrate();
}

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Paises/Error");
}
app.UseStaticFiles();
app.UseRouting();
app.MapControllerRoute("default", "{controller=Paises}/{action=Index}/{id?}");

Console.WriteLine("Atlas de Países (.NET) listo. En el navegador: http://localhost:8091");
foreach (var interfaz in System.Net.NetworkInformation.NetworkInterface.GetAllNetworkInterfaces()
             .Where(i => i.OperationalStatus == System.Net.NetworkInformation.OperationalStatus.Up
                         && i.NetworkInterfaceType != System.Net.NetworkInformation.NetworkInterfaceType.Loopback))
    foreach (var d in interfaz.GetIPProperties().UnicastAddresses.Where(d => d.Address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork))
    {
        var ip = d.Address.ToString();
        if (ip.StartsWith("192.168.") || ip.StartsWith("10.") || ip.StartsWith("172."))
            Console.WriteLine($"Desde un celular real (misma red wifi): http://{ip}:8091");
    }
Console.WriteLine("Desde el emulador de Android: http://10.0.2.2:8091");
app.Run();
