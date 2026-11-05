using System.Data.Common;

namespace ec.edu.monster.eureka.Datos
{
    /// <summary>Entrega conexiones abiertas a la base de datos. Las pruebas pueden usar otra base.</summary>
    public interface IFuenteDatos
    {
        DbConnection Abrir();
    }

    /// <summary>
    /// Conexion a SQL Server (base "eurekabank"). La cadena no esta en el codigo: se toma de la variable de entorno
    /// EUREKA_DB_CONEXION o de ConnectionStrings:EurekaBank en appsettings.json (autenticacion de Windows).
    /// </summary>
    public class ConexionSqlServer : IFuenteDatos
    {
        public const string Llave = "EurekaBank";
        private readonly string cadena;

        public ConexionSqlServer(string cadenaPorDefecto)
        {
            var delEntorno = Environment.GetEnvironmentVariable("EUREKA_DB_CONEXION");
            cadena = string.IsNullOrWhiteSpace(delEntorno) ? cadenaPorDefecto : delEntorno;
        }

        public DbConnection Abrir()
        {
            var c = new Microsoft.Data.SqlClient.SqlConnection(cadena);
            c.Open();
            return c;
        }
    }

    /// <summary>Punto unico de acceso a la base de datos.</summary>
    public static class BaseDatos
    {
        private static IFuenteDatos? fuente;
        private static string cadenaPorDefecto = "Server=localhost;Database=eurekabank;Integrated Security=True;TrustServerCertificate=True";

        public static IFuenteDatos Fuente => fuente ??= new ConexionSqlServer(cadenaPorDefecto);

        public static void UsarCadena(string? cadena)
        {
            if (!string.IsNullOrWhiteSpace(cadena)) cadenaPorDefecto = cadena;
        }

        public static void Configurar(IFuenteDatos nueva) => fuente = nueva;
    }
}
