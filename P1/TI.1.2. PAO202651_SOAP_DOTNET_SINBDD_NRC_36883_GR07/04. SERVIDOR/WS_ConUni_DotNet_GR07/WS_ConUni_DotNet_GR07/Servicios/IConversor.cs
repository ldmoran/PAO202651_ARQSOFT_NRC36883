using System.Globalization;
using System.Text;

namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Contrato de un conversor de unidades. Cada categoria (longitud, masa,
    /// temperatura, y en el futuro otras categorias) implementa esta interfaz y se registra
    /// en <see cref="CatalogoUnidades"/>; el resto del sistema no cambia.
    /// </summary>
    public interface IConversor
    {
        /// <summary>Nombre de la categoria, por ejemplo "Longitud".</summary>
        string Categoria { get; }

        /// <summary>Unidades soportadas, en minusculas y sin tildes.</summary>
        IReadOnlyList<string> Unidades { get; }

        /// <summary>
        /// Convierte un valor. Las unidades ya fueron validadas contra
        /// <see cref="Unidades"/> y vienen normalizadas.
        /// </summary>
        double Convertir(double valor, string origen, string destino);
    }

    public static class Texto
    {
        /// <summary>Pasa a minusculas, sin tildes y sin espacios sobrantes.</summary>
        public static string? Normalizar(string? texto)
        {
            if (texto is null)
            {
                return null;
            }
            var descompuesto = texto.Trim().Normalize(NormalizationForm.FormD);
            var sb = new StringBuilder(descompuesto.Length);
            foreach (var c in descompuesto)
            {
                if (CharUnicodeInfo.GetUnicodeCategory(c) != UnicodeCategory.NonSpacingMark)
                {
                    sb.Append(c);
                }
            }
            return sb.ToString().ToLowerInvariant();
        }
    }
}
