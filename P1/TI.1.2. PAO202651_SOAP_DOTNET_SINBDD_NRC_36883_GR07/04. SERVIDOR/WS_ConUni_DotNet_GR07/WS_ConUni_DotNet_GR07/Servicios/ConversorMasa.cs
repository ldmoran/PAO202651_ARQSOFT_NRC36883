namespace ec.edu.monster.Servicios
{
    /// <summary>
    /// Masa: conversion lineal con unidad base kilogramo.
    /// resultado = valor x factorOrigen / factorDestino
    /// </summary>
    public class ConversorMasa : IConversor
    {
        /// <summary>Factores en kilogramos (el quintal equivale a 100 libras).</summary>
        private static readonly (string Unidad, double Kilos)[] Factores =
        {
            ("gramos", 0.001),
            ("kilogramos", 1.0),
            ("onzas", 0.028349523125),
            ("libras", 0.45359237),
            ("quintales", 45.359237)
        };

        private static readonly Dictionary<string, double> Kilos =
            Factores.ToDictionary(f => f.Unidad, f => f.Kilos);

        public string Categoria => "Masa";

        public IReadOnlyList<string> Unidades { get; } = Factores.Select(f => f.Unidad).ToList();

        public double Convertir(double valor, string origen, string destino) =>
            valor * Kilos[origen] / Kilos[destino];
    }
}
