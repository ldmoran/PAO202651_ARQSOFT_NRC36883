namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Longitud: conversion lineal con unidad base metro.
    /// resultado = valor x factorOrigen / factorDestino
    /// </summary>
    public class ConversorLongitud : IConversor
    {
        /// <summary>Factores en metros (en el orden en que se muestran).</summary>
        private static readonly (string Unidad, double Metros)[] Factores =
        {
            ("centimetros", 0.01),
            ("metros", 1.0),
            ("pies", 0.3048),
            ("yardas", 0.9144),
            ("millas", 1609.344)
        };

        private static readonly Dictionary<string, double> Metros =
            Factores.ToDictionary(f => f.Unidad, f => f.Metros);

        public string Categoria => "Longitud";

        public IReadOnlyList<string> Unidades { get; } = Factores.Select(f => f.Unidad).ToList();

        public double Convertir(double valor, string origen, string destino) =>
            valor * Metros[origen] / Metros[destino];
    }
}
