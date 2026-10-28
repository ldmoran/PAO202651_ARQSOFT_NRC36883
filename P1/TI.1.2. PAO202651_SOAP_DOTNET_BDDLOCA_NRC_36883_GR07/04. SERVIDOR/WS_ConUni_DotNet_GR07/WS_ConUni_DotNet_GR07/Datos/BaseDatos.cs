namespace ec.edu.monster.Datos
{
    /// <summary>
    /// Punto unico de acceso a la base de datos y al reloj del servidor. En produccion usa SQL Server y
    /// la hora del sistema; las pruebas lo cambian con <see cref="Configurar"/> para usar una base
    /// temporal y una hora controlada.
    /// </summary>
    public static class BaseDatos
    {
        private static IFuenteDatos? fuente;
        private static string cadenaPorDefecto =
            "Server=localhost;Database=conversion_unidades;Integrated Security=True;TrustServerCertificate=True";

        /// <summary>Conexion vigente. Si nadie la configuro, se crea con la cadena por defecto.</summary>
        public static IFuenteDatos Fuente => fuente ??= new ConexionSqlServer(cadenaPorDefecto);

        public static TimeProvider Reloj { get; private set; } = TimeProvider.System;

        /// <summary>Cadena de conexion que lee Program.cs de appsettings.json. No pisa lo que ya hayan configurado las pruebas.</summary>
        public static void UsarCadena(string? cadena)
        {
            if (!string.IsNullOrWhiteSpace(cadena))
            {
                cadenaPorDefecto = cadena;
            }
        }

        public static void Configurar(IFuenteDatos nuevaFuente, TimeProvider nuevoReloj)
        {
            fuente = nuevaFuente;
            Reloj = nuevoReloj;
        }

        /// <summary>Hora actual del servidor (hora local, sin zona), tal como se guarda en la base.</summary>
        public static DateTime Ahora() => Reloj.GetLocalNow().DateTime;

        /// <summary>Vuelve a SQL Server y a la hora del sistema.</summary>
        public static void Restablecer()
        {
            fuente = null;
            Reloj = TimeProvider.System;
        }
    }
}
