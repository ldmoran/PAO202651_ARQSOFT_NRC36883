package ec.edu.monster.servicios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Masa: conversion lineal con unidad base kilogramo.
 * resultado = valor x factorOrigen / factorDestino
 */
public class ConversorMasa implements Conversor {

    /** Factores en kilogramos (el quintal equivale a 100 libras). */
    private static final Map<String, Double> KILOS = new LinkedHashMap<>();

    static {
        KILOS.put("gramos", 0.001);
        KILOS.put("kilogramos", 1.0);
        KILOS.put("onzas", 0.028349523125);
        KILOS.put("libras", 0.45359237);
        KILOS.put("quintales", 45.359237);
    }

    @Override
    public String categoria() {
        return "Masa";
    }

    @Override
    public List<String> unidades() {
        return List.copyOf(KILOS.keySet());
    }

    @Override
    public double convertir(double valor, String origen, String destino) {
        return valor * KILOS.get(origen) / KILOS.get(destino);
    }
}
