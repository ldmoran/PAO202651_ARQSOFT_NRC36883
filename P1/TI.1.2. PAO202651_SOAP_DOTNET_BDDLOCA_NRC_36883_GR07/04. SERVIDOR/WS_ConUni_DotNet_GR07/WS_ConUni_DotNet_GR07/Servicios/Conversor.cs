using System.Globalization;
using System.Text;
using ec.edu.monster.Modelos.Entidades;

namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Convierte entre dos unidades de una misma categoria usando los datos de la tabla unidad.
    /// Pasa el valor a la unidad base de la categoria y de ahi a la unidad de destino:
    /// base = (valor + desplazamiento) x factor, y destino = base / factor - desplazamiento.
    /// Longitud y masa tienen desplazamiento 0; la temperatura usa el celsius como base.
    /// </summary>
    public static class Conversor
    {
        public static double Convertir(double valor, Unidad origen, Unidad destino)
        {
            var baseUnidad = (valor + origen.Desplazamiento) * origen.Factor;
            return baseUnidad / destino.Factor - destino.Desplazamiento;
        }
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
