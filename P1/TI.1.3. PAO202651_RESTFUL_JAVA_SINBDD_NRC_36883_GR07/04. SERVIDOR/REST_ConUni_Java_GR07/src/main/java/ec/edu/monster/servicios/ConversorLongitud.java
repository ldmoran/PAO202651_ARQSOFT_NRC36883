package ec.edu.monster.servicios;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Longitud: conversion lineal con unidad base metro.
 * resultado = valor x factorOrigen / factorDestino
 */
public class ConversorLongitud implements Conversor {

    /** Factores en metros. */
    private static final Map<String, Double> METROS = new LinkedHashMap<>();

    static {
        METROS.put("centimetros", 0.01);
        METROS.put("metros", 1.0);
        METROS.put("pies", 0.3048);
        METROS.put("yardas", 0.9144);
        METROS.put("millas", 1609.344);
    }

    @Override
    public String categoria() {
        return "Longitud";
    }

    @Override
    public List<String> unidades() {
        return List.copyOf(METROS.keySet());
    }

    @Override
    public double convertir(double valor, String origen, String destino) {
        return valor * METROS.get(origen) / METROS.get(destino);
    }
}
