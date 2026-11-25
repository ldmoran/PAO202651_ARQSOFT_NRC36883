using System.Globalization;
using System.Text;
using ec.edu.monster.Cliente.Modelos;

// Cliente de consola de EurekaBank (.NET). Menu numerado: iniciar sesion, ver cuentas, ver movimientos y operar.
// URL del servicio: argumento 1, variable EUREKA_URL o http://localhost:5081/WSEurekaBank
Console.OutputEncoding = Encoding.UTF8;
Console.InputEncoding = Encoding.UTF8;

var url = args.Length > 0 ? args[0] : SoapClient.UrlConfigurada();
var cliente = new SoapClient(url);
string? usuario = null;

Marco("EUREKABANK", "Cliente de consola (.NET)", "Servicio: " + cliente.Url);
var seguir = true;
while (seguir)
{
    Console.WriteLine();
    Console.WriteLine("---- Menú | Sesión: " + (usuario == null ? "no iniciada" : usuario) + " ----");
    if (usuario == null)
    {
        Console.WriteLine("  1. Iniciar sesión");
    }
    else
    {
        Console.WriteLine("  2. Ver cuentas");
        Console.WriteLine("  3. Ver movimientos");
        Console.WriteLine("  4. Depósito");
        Console.WriteLine("  5. Retiro");
        Console.WriteLine("  6. Transferencia");
        Console.WriteLine("  7. Cerrar sesión");
    }
    Console.WriteLine("  0. Salir");
    var op = Leer("Opción: ");
    if (op == null) break;
    if (op != "0" && op != "1" && usuario == null)
    {
        Console.WriteLine("  Debe iniciar sesión primero.");
        continue;
    }
    switch (op)
    {
        case "1":
            var u = Leer("Usuario: ") ?? "";
            var c = Leer("Clave: ") ?? "";
            var r = await cliente.IniciarSesion(u, c);
            if (r.Ok && r.Datos) { usuario = u.Trim(); Console.WriteLine("  Bienvenido, " + usuario + "."); }
            else Console.WriteLine("  " + (r.Ok ? "Usuario o clave incorrectos." : r.Error));
            break;
        case "2":
            var cu = await cliente.Cuentas();
            if (!cu.Ok) { Console.WriteLine("  " + cu.Error); break; }
            Console.WriteLine($"  {"Cuenta",-10}{"Cliente",-30}{"Moneda",-9}{"Saldo",12}");
            foreach (var x in cu.Datos!) Console.WriteLine($"  {x.Codigo,-10}{x.NombreCliente,-30}{x.Moneda,-9}{x.Saldo.ToString("N2", CultureInfo.InvariantCulture),12}");
            break;
        case "3":
            var cta = Leer("Cuenta (vacío = todas): ") ?? "";
            var mv = await cliente.Movimientos(cta);
            if (!mv.Ok) { Console.WriteLine("  " + mv.Error); break; }
            Console.WriteLine($"  {"Cuenta",-10}{"N.º",4} {"Fecha",-11}{"Tipo",-24}{"Importe",11}{"Saldo",12}");
            foreach (var m in mv.Datos!)
                Console.WriteLine($"  {m.CodigoCuenta,-10}{m.Numero,4} {m.Fecha,-11}{m.Tipo,-24}{m.Importe.ToString("N2", CultureInfo.InvariantCulture),11}{m.SaldoActual.ToString("N2", CultureInfo.InvariantCulture),12}");
            break;
        case "4": await Operar("DEPOSITO", false); break;
        case "5": await Operar("RETIRO", false); break;
        case "6": await Operar("TRANSFERENCIA", true); break;
        case "7": usuario = null; Console.WriteLine("  Sesión cerrada."); break;
        case "0": seguir = false; break;
        default: Console.WriteLine("  Opción no válida."); break;
    }
}
Console.WriteLine("Hasta luego.");

async Task Operar(string tipo, bool conDestino)
{
    var origen = Leer(conDestino ? "Cuenta origen: " : "Cuenta: ") ?? "";
    var destino = conDestino ? Leer("Cuenta destino: ") : null;
    var txt = (Leer("Importe: ") ?? "").Replace(',', '.');
    if (!decimal.TryParse(txt, NumberStyles.Any, CultureInfo.InvariantCulture, out var importe) || importe <= 0)
    {
        Console.WriteLine("  El importe debe ser un número mayor a 0.");
        return;
    }
    var r = await cliente.Registrar(tipo, origen.Trim(), destino?.Trim(), importe);
    Console.WriteLine("  " + (r.Ok ? "OK: " + r.Datos : "ERROR: " + r.Error));
}

static string? Leer(string pregunta)
{
    Console.Write(pregunta);
    return Console.ReadLine()?.Trim();
}

static void Marco(params string[] lineas)
{
    var borde = "+" + new string('-', 54) + "+";
    Console.WriteLine(borde);
    foreach (var l in lineas)
    {
        var libre = Math.Max(0, 54 - l.Length);
        Console.WriteLine("|" + new string(' ', libre / 2) + l + new string(' ', libre - libre / 2) + "|");
    }
    Console.WriteLine(borde);
}
