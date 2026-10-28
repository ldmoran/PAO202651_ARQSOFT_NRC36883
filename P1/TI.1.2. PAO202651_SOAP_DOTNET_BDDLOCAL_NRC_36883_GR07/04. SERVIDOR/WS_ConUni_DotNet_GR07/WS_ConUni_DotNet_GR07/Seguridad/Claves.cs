using System.Security.Cryptography;
using System.Text;

namespace ec.edu.monster.Seguridad
{
    /// <summary>Guarda y compara claves como SHA-256(sal + clave) en hexadecimal; la clave en claro nunca se almacena.</summary>
    public static class Claves
    {
        /// <summary>Sal nueva de 16 bytes (32 caracteres hexadecimales).</summary>
        public static string GenerarSal() => Convert.ToHexString(RandomNumberGenerator.GetBytes(16)).ToLowerInvariant();

        public static string Hash(string sal, string clave) =>
            Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(sal + clave))).ToLowerInvariant();

        /// <summary>Compara en tiempo constante el hash guardado con el de la clave escrita.</summary>
        public static bool Coincide(string clave, string sal, string hashGuardado) =>
            CryptographicOperations.FixedTimeEquals(
                Encoding.UTF8.GetBytes(Hash(sal, clave)), Encoding.UTF8.GetBytes(hashGuardado.Trim()));
    }
}
