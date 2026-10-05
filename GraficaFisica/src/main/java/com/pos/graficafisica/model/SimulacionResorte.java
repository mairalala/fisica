package com.pos.graficafisica.model;

import java.util.ArrayList;
import java.util.List;

public class SimulacionResorte {

    // Parámetros físicos según tus apuntes
    public double masa = 0.5;
    public double constanteK = 50.0;
    public double gravedad = 9.81;
    public double radioEsfera = 0.05; // R en tus apuntes ( Stokes)
    public double viscosidadFluido = 0.15; // η (eta) en tus apuntes

    // Variables de estado
    public double elongacion = 0; // y
    public double velocidad = 0;  // v
    public double aceleracion = 0; // a
    public double tiempo = 0;
    private double dt = 0.01; // Paso de tiempo para Runge-Kutta

    // --- NUEVO: Lista para almacenar el historial de la ejecución actual ---
    private List<double[]> historialDatos = new ArrayList<>();

    // Ecuación diferencial: a = (Fe + Fv - m*g) / m
    private double calcularAceleracion(double y, double v) {
        double b = 6 * Math.PI * viscosidadFluido * radioEsfera;
        double fuerzaElastica = -constanteK * y;
        double fuerzaViscosa = -b * v;
        double fuerzaPeso = masa * gravedad;
        return (fuerzaElastica + fuerzaViscosa + fuerzaPeso) / masa;
    }

    // Implementación del Método de Runge-Kutta de 4to Orden (RK4)
    public void actualizarEstadoRK4() {
        double k1v = calcularAceleracion(elongacion, velocidad);
        double k1y = velocidad;

        double k2v = calcularAceleracion(elongacion + 0.5 * dt * k1y, velocidad + 0.5 * dt * k1v);
        double k2y = velocidad + 0.5 * dt * k1v;

        double k3v = calcularAceleracion(elongacion + 0.5 * dt * k2y, velocidad + 0.5 * dt * k2v);
        double k3y = velocidad + 0.5 * dt * k2v;

        double k4v = calcularAceleracion(elongacion + dt * k3y, velocidad + dt * k3v);
        double k4y = velocidad + dt * k3v;

        velocidad += (dt / 6.0) * (k1v + 2 * k2v + 2 * k3v + k4v);
        elongacion += (dt / 6.0) * (k1y + 2 * k2y + 2 * k3y + k4y);

        aceleracion = calcularAceleracion(elongacion, velocidad);
        tiempo += dt;

        // --- NUEVO: Guardar el paso actual para la tabla de resultados ---
        historialDatos.add(new double[]{tiempo, elongacion, velocidad, aceleracion});
    }

    public String obtenerTipoFlujo() {
        double densidadAire = 1.225; // kg/m³
        double reynolds = (densidadAire * Math.abs(velocidad) * (2 * radioEsfera)) / viscosidadFluido;

        if (reynolds < 2000) return "Laminar (Re: " + String.format("%.1f", reynolds) + ")";
        if (reynolds < 4000) return "Transición (Re: " + String.format("%.1f", reynolds) + ")";
        return "Turbulento (Re: " + String.format("%.1f", reynolds) + ")";
    }

    // --- NUEVO: Métodos para la integración con la Ventana de Resultados ---
    public List<double[]> getHistorialDatos() {
        return historialDatos;
    }

    public void reiniciarHistorial() {
        historialDatos.clear();
    }

    public void reset() {
        elongacion = 0;
        velocidad = 0;
        aceleracion = 0;
        tiempo = 0;
        reiniciarHistorial();
    }
}