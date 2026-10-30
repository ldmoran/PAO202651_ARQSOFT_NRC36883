namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Temperatura: no es lineal. Se pasa primero a celsius y luego a la escala de destino.
    ///
    /// A celsius: C = (F - 32) x 5/9; C = K - 273,15; C = N x 100/33; C = Re x 5/4.
    /// Desde celsius: F = C x 9/5 + 32; K = C + 273,15; N = C x 33/100; Re = C x 4/5.
    /// </summary>
    public class ConversorTemperatura : IConversor
    {
        public string Categoria => "Temperatura";

        public IReadOnlyList<string> Unidades { get; } =
            new List<string> { "celsius", "fahrenheit", "kelvin", "newton", "reaumur" };

        public double Convertir(double valor, string origen, string destino) =>
            DesdeCelsius(ACelsius(valor, origen), destino);

        private static double ACelsius(double v, string escala) => escala switch
        {
            "fahrenheit" => (v - 32.0) * 5.0 / 9.0,
            "kelvin" => v - 273.15,
            "newton" => v * 100.0 / 33.0,
            "reaumur" => v * 5.0 / 4.0,
            _ => v // celsius
        };

        private static double DesdeCelsius(double c, string escala) => escala switch
        {
            "fahrenheit" => c * 9.0 / 5.0 + 32.0,
            "kelvin" => c + 273.15,
            "newton" => c * 33.0 / 100.0,
            "reaumur" => c * 4.0 / 5.0,
            _ => c // celsius
        };
    }
}
