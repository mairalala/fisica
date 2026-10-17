package com.pos.graficafisica.model;

import java.util.ArrayList;
import java.util.List;

public class SimulacionNewton {

    public enum Ley {
        PRIMERA,
        SEGUNDA,
        TERCERA
    }

    public double masa = 2.0;
    public double masaB = 2.0;
    public double fuerza = 10.0;
    public double tiempo;
    public double posicionA;
    public double posicionB;
    public double velocidadA;
    public double velocidadB;
    public double aceleracionA;
    public double aceleracionB;
    public Ley ley = Ley.PRIMERA;
    public boolean fuerzaAplicada;

    // Historial de iteraciones para tablas y control analítico (patrón unificado)
    public List<double[]> historial = new ArrayList<>();

    public void iniciar(Ley ley, double masa, double masaB, double fuerza) {
        this.ley = ley;
        this.masa = masa;
        this.masaB = masaB;
        this.fuerza = fuerza;
        tiempo = 0;
        posicionA = ley == Ley.TERCERA ? -0.525 : 0;
        posicionB = ley == Ley.TERCERA ? 0.525 : 0;
        velocidadA = ley == Ley.PRIMERA ? 1.5 : 0;
        velocidadB = 0;
        aceleracionA = 0;
        aceleracionB = 0;
        fuerzaAplicada = false;
        historial.clear();
    }

    public void actualizar(double dt) {
        aceleracionA = 0;
        aceleracionB = 0;

        switch (ley) {
            case PRIMERA -> {
                if (fuerzaAplicada) {
                    aceleracionA = fuerza / masa;
                }
            }
            case SEGUNDA -> aceleracionA = fuerza / masa;
            case TERCERA -> {
                if (fuerzaAplicada) {
                    aceleracionA = fuerza / masa;
                    aceleracionB = -fuerza / masaB;
                }
            }
        }

        // Corrección de operadores de incremento con dt
        velocidadA += aceleracionA * dt;
        posicionA += velocidadA * dt;

        if (ley == Ley.TERCERA) {
            velocidadB += aceleracionB * dt;
            posicionB += velocidadB * dt;
        }

        tiempo += dt;

        // Registrar métricas en el historial de la simulación
        historial.add(new double[]{tiempo, posicionA, velocidadA, aceleracionA, posicionB, velocidadB, aceleracionB});
    }

    // Método para guardar el estado actual en la base de datos SQLite
    public void guardarEnBaseDatos() {
        ConexionDB.guardarSimulacionNewton(
                ley.toString(),
                masa,
                masaB,
                fuerza,
                aceleracionA,
                aceleracionB,
                tiempo
        );
    }
}