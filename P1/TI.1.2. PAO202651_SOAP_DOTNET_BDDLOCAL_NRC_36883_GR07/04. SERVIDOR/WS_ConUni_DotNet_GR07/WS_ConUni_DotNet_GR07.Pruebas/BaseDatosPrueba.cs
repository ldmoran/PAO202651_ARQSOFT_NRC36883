using System.Data.Common;
using System.Text.RegularExpressions;
using ec.edu.monster.Datos;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.Pruebas
{
    /// <summary>
    /// Base de datos temporal en el SQL Server local, creada con los MISMOS scripts de la carpeta
    /// "03. BDD" (con otro nombre) y borrada al terminar las pruebas. Usa autenticacion de Windows.
    /// </summary>
    public static class BaseDatosPrueba
    {
        private const string Servidor = "Server=localhost;Integrated Security=True;TrustServerCertificate=True;Connect Timeout=5";
        private static readonly List<string> Creadas = new();
        private static readonly Lazy<bool> DisponibleLazy = new(Probar);
        private static readonly Lazy<IFuenteDatos> CompartidaLazy = new(() => Crear());

        static BaseDatosPrueba() => AppDomain.CurrentDomain.ProcessExit += (_, _) => BorrarTodas();

        /// <summary>true si hay un SQL Server local al que conectarse; si no, las pruebas se omiten.</summary>
        public static bool Disponible => DisponibleLazy.Value;

        /// <summary>Base compartida por las pruebas que solo agregan sesiones; deja el servidor apuntando a ella.</summary>
        public static IFuenteDatos Compartida(TimeProvider? reloj = null)
        {
            BaseDatos.Configurar(CompartidaLazy.Value, reloj ?? TimeProvider.System);
            return CompartidaLazy.Value;
        }

        /// <summary>Base nueva y limpia, solo para quien la pide.</summary>
        public static IFuenteDatos Nueva(TimeProvider? reloj = null)
        {
            var fuente = Crear();
            BaseDatos.Configurar(fuente, reloj ?? TimeProvider.System);
            return fuente;
        }

        private static bool Probar()
        {
            try
            {
                using var cn = new SqlConnection(Servidor);
                cn.Open();
                BorrarAntiguas(cn);
                return true;
            }
            catch (Exception e)
            {
                MotivoNoDisponible = e.GetType().Name + ": " + e.Message;
                return false;
            }
        }

        /// <summary>Borra las bases temporales que dejo una corrida anterior si no alcanzo a limpiarlas al terminar.</summary>
        private static void BorrarAntiguas(SqlConnection cn)
        {
            try
            {
                using var cmd = cn.CreateCommand();
                cmd.CommandText =
                    "DECLARE @sql NVARCHAR(MAX) = N''; " +
                    "SELECT @sql += N'ALTER DATABASE [' + name + N'] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [' + name + N'];' " +
                    "FROM sys.databases WHERE name LIKE 'conuni_pruebas[_]%'; EXEC sp_executesql @sql;";
                cmd.ExecuteNonQuery();
            }
            catch (Exception)
            {
                // si no se puede limpiar, las pruebas siguen igual
            }
        }

        /// <summary>Por que no se pudo conectar (vacio si hay SQL Server).</summary>
        public static string MotivoNoDisponible { get; private set; } = "";

        private static IFuenteDatos Crear()
        {
            var nombre = "conuni_pruebas_" + Guid.NewGuid().ToString("N")[..10];
            lock (Creadas)
            {
                Creadas.Add(nombre);
            }
            Ejecutar(Servidor, $"CREATE DATABASE [{nombre}]");
            foreach (var archivo in new[] { "02_CreaBD_ConversionUnidades.sql", "03_CargaDatos_ConversionUnidades.sql" })
            {
                var script = File.ReadAllText(Path.Combine(CarpetaBdd(), archivo))
                    .Replace("IF DB_ID('conversion_unidades') IS NULL", "IF 1 = 0")
                    .Replace("conversion_unidades", nombre);
                foreach (var lote in Regex.Split(script, @"^\s*GO\s*$", RegexOptions.Multiline | RegexOptions.IgnoreCase))
                {
                    if (!string.IsNullOrWhiteSpace(lote))
                    {
                        Ejecutar(Servidor + $";Database={nombre}", lote);
                    }
                }
            }
            return new FuentePrueba(Servidor + $";Database={nombre}");
        }

        private static string CarpetaBdd()
        {
            var dir = new DirectoryInfo(AppContext.BaseDirectory);
            while (dir is not null)
            {
                var candidata = Path.Combine(dir.FullName, "03. BDD");
                if (Directory.Exists(candidata))
                {
                    return candidata;
                }
                dir = dir.Parent;
            }
            throw new DirectoryNotFoundException("No se encuentra la carpeta '03. BDD' con los scripts de la base.");
        }

        private static void Ejecutar(string conexion, string sql)
        {
            using var cn = new SqlConnection(conexion);
            cn.Open();
            using var cmd = cn.CreateCommand();
            cmd.CommandText = sql;
            cmd.ExecuteNonQuery();
        }

        /// <summary>Borra las bases temporales de esta corrida (la llama la coleccion de pruebas al terminar).</summary>
        public static void BorrarTodas()
        {
            foreach (var nombre in Creadas.ToArray())
            {
                try
                {
                    SqlConnection.ClearAllPools();
                    Ejecutar(Servidor, $"ALTER DATABASE [{nombre}] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; DROP DATABASE [{nombre}]");
                }
                catch (Exception)
                {
                    // si no se pudo borrar, queda una base de pruebas que se puede eliminar a mano
                }
            }
        }

        private sealed class FuentePrueba : IFuenteDatos
        {
            private readonly string cadena;

            public FuentePrueba(string cadena) => this.cadena = cadena;

            public DbConnection Abrir()
            {
                var cn = new SqlConnection(cadena);
                cn.Open();
                return cn;
            }
        }
    }

    /// <summary>Al terminar todas las pruebas de la coleccion, borra las bases temporales.</summary>
    public sealed class LimpiezaBaseDatos : IDisposable
    {
        public void Dispose() => BaseDatosPrueba.BorrarTodas();
    }

    /// <summary>Prueba que necesita el SQL Server local; se omite (no falla) si no esta disponible.</summary>
    public sealed class SqlServerFactAttribute : FactAttribute
    {
        public SqlServerFactAttribute()
        {
            if (!BaseDatosPrueba.Disponible)
            {
                Skip = "No hay un SQL Server en localhost (" + BaseDatosPrueba.MotivoNoDisponible
                    + "): instala SQL Server Express y ejecuta los scripts de '03. BDD'.";
            }
        }
    }

    /// <summary>Reloj que se mueve a mano: permite probar la expiracion de tokens sin esperar.</summary>
    public sealed class RelojManual : TimeProvider
    {
        private DateTimeOffset ahora;

        public RelojManual(DateTimeOffset inicio) => ahora = inicio;

        public void Avanzar(TimeSpan tiempo) => ahora += tiempo;

        public override DateTimeOffset GetUtcNow() => ahora;

        public override TimeZoneInfo LocalTimeZone => TimeZoneInfo.Utc;
    }
}
