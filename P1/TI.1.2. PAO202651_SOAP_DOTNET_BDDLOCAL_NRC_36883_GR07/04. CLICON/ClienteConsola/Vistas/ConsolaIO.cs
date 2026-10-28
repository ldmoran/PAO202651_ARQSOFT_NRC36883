using System.Globalization;
using ec.edu.monster.ClienteConsola.Modelos;

namespace ec.edu.monster.ClienteConsola.Vistas
{
    /// <summary>Entrada y salida de texto compartida por las vistas de consola (marcos, listas, lectura validada).</summary>
    public class ConsolaIO
    {
        private const int Ancho = 54;

        private readonly TextReader entrada;
        private readonly TextWriter salida;

        public ConsolaIO(TextReader entrada, TextWriter salida)
        {
            this.entrada = entrada;
            this.salida = salida;
        }

        public void Linea(string texto = "") => salida.WriteLine(texto);

        /// <summary>Marco con titulo(s) centrados.</summary>
        public void Marco(params string[] lineas)
        {
            var borde = "+" + new string('-', Ancho) + "+";
            salida.WriteLine(borde);
            foreach (var l in lineas)
            {
                var libre = Math.Max(0, Ancho - l.Length);
                var izq = libre / 2;
                salida.WriteLine("|" + new string(' ', izq) + l + new string(' ', libre - izq) + "|");
            }
            salida.WriteLine(borde);
        }

        /// <summary>Subtitulo de seccion.</summary>
        public void Seccion(string titulo)
        {
            salida.WriteLine();
            salida.WriteLine("--- " + titulo + " " + new string('-', Math.Max(3, Ancho - titulo.Length - 3)));
        }

        public void Ok(string? texto) => salida.WriteLine("  [OK] " + texto);

        public void Error(RespuestaServicio r)
        {
            salida.WriteLine($"  [ERROR {r.Codigo}] {r.Mensaje}");
            if (r.RequiereNuevoLogin)
            {
                salida.WriteLine("  Su sesión terminó. Inicie sesión nuevamente.");
            }
        }

        public string? Leer(string texto)
        {
            salida.Write(texto);
            salida.Flush();
            return entrada.ReadLine();
        }

        public double? LeerNumero(string texto)
        {
            while (true)
            {
                var t = Leer(texto);
                if (t == null)
                {
                    return null;
                }
                if (double.TryParse(t.Trim().Replace(',', '.'), NumberStyles.Float, CultureInfo.InvariantCulture, out var v)
                    && !double.IsNaN(v) && !double.IsInfinity(v))
                {
                    return v;
                }
                salida.WriteLine("  Ingrese un número válido (ejemplo: 12.5).");
            }
        }

        /// <summary>Muestra una lista numerada y devuelve el elemento elegido (por numero o por nombre).</summary>
        public string? Elegir(string titulo, IReadOnlyList<string> opciones)
        {
            Seccion(titulo);
            for (var i = 0; i < opciones.Count; i++)
            {
                salida.WriteLine($"  {i + 1}. {opciones[i]}");
            }
            while (true)
            {
                var t = Leer("Elija (número o nombre, 0 para cancelar): ");
                if (t == null || t.Trim() == "0")
                {
                    return null;
                }
                t = t.Trim();
                if (int.TryParse(t, out var n))
                {
                    if (n >= 1 && n <= opciones.Count)
                    {
                        return opciones[n - 1];
                    }
                }
                else
                {
                    var o = opciones.FirstOrDefault(x => x.Equals(t, StringComparison.OrdinalIgnoreCase));
                    if (o != null)
                    {
                        return o;
                    }
                }
                salida.WriteLine("  Opción no válida.");
            }
        }
    }
}
