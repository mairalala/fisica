package com.pos.graficafisica;

import java.util.ArrayList;
import java.util.List;

public class SimulacionFisica {

    public double m = 0.25, h = 2.5, L = 4.0, g = 9.8, k = 100.0, x0 = 1.2;
    public double muRampa = 0.15;
    public double muSuelo = 0.25;
    public double elasticidad = 0.75;
    public double bViscoso = 0.05, cAire = 0.02;
    public double rotAngle = 0;
    public double vAngular = 0;
    private double radioEfectivo = 0.15;

    public double anguloRampa = 0, anguloLanzamiento = 0, anguloReboteAct = 0;
    public double t = 0, currX = 0, currY = 0, currVX = 0, currVY = 0;
    public double v1 = 0, theta = 0, vInicial = 0, aceAct = 0;
    public double hMax = 0, xMax = 0;
    public int numRebotes = 0;
    public List<double[]> historial = new ArrayList<>();

    // 🔥 Nuevo: Ajustar propiedades físicas según material
    public void configurarMaterial(String material) {
        switch (material) {
            case "Acero":
                this.elasticidad = 0.55; // Rebota poco pero es pesado
                this.muSuelo = 0.15;     // Desliza más
                break;
            case "Madera":
                this.elasticidad = 0.40;
                this.muSuelo = 0.45;
                break;
            case "Plástico":
                this.elasticidad = 0.65;
                this.muSuelo = 0.30;
                break;
            case "Hielo":
                this.elasticidad = 0.30;
                this.muSuelo = 0.02;     // Casi nada de fricción
                break;
            case "Goma":
            default:
                this.elasticidad = 0.85; // Mucho rebote
                this.muSuelo = 0.60;     // Mucha fricción
                break;
        }
    }

    public void recalcular() {
        theta = Math.atan2(h, L);
        anguloRampa = Math.toDegrees(theta);
        anguloLanzamiento = anguloRampa;
        double hipotenusaRampa = Math.sqrt(L * L + h * h);

        // Conservación de energía con pérdida por fricción en rampa
        double energiaElastica = 0.5 * k * Math.pow(x0, 2);
        double energiaPotencial = m * g * h;
        double trabajoFriccionRampa = muRampa * m * g * Math.cos(theta) * hipotenusaRampa;

        double energiaCineticaFinal = energiaElastica + energiaPotencial - trabajoFriccionRampa;
        v1 = energiaCineticaFinal > 0 ? Math.sqrt((2 * energiaCineticaFinal) / m) : 0;

        vInicial = v1;
        reset();
    }

    public void reset() {
        t = 0;
        numRebotes = 0;
        hMax = 0;
        xMax = 0;
        aceAct = 0;
        anguloReboteAct = 0;
        rotAngle = 0;
        vAngular = 0;
        currX = L;
        currY = h;
        currVX = v1 * Math.cos(theta);
        currVY = v1 * Math.sin(theta);
        historial.clear();
    }

    public void avanzarPaso(double dt, double limiteX) {
        // Fase de impulso por resorte (0 a 1 segundo de animación interna)
        if (t < 1.0) {
            t += dt * 2;
            vAngular = v1 / (radioEfectivo * 2);
            rotAngle += vAngular * dt;
            return;
        }
        t += dt;

        // Limite de seguridad de rebotes para evitar bucles infinitos
        if (currY > 0 || numRebotes < 25) {
            double vTotal = vTotal();

            // Fuerzas de resistencia (Aire y Viscosidad)
            double fResX = (bViscoso * currVX) + (cAire * vTotal * currVX);
            double fResY = (bViscoso * currVY) + (cAire * vTotal * currVY);

            double ax = -fResX / m;
            double ay = -g - (fResY / m);

            // Comportamiento en el suelo
            if (currY <= 0.01) {
                double fRozSuelo = muSuelo * m * g;
                if (Math.abs(currVX) > 0.01) {
                    // La fricción se opone al movimiento horizontal
                    ax -= (currVX > 0 ? 1 : -1) * fRozSuelo / m;
                    vAngular = currVX / radioEfectivo;
                } else {
                    vAngular *= 0.9; // Frenado por rotación
                    currVX = 0;
                }
            } else {
                vAngular *= 0.995; // Resistencia mínima al girar en el aire
            }

            // Integración de Euler
            currVX += ax * dt;
            currVY += ay * dt;
            currX += currVX * dt;
            currY += currVY * dt;
            rotAngle += vAngular * dt;
            aceAct = Math.sqrt(ax * ax + ay * ay);

            // Detección de Colisión con el suelo
            if (currY <= 0) {
                currY = 0;
                // Aplicar coeficiente de restitución (elasticidad)
                currVY = -currVY * elasticidad;

                // Pérdida de velocidad horizontal por el impacto
                currVX *= (0.85 + (elasticidad * 0.1));

                if (Math.abs(currVX) > 0.01) {
                    anguloReboteAct = Math.toDegrees(Math.atan2(currVY, currVX));
                }

                numRebotes++;

                // Umbral para detener rebotes muy pequeños (evitar efecto "jitter")
                if (Math.abs(currVY) < 0.15) {
                    currVY = 0;
                }
            }
        }

        // Condición de parada total
        if (Math.abs(currVX) < 0.02 && Math.abs(currVY) < 0.02 && currY <= 0) {
            currVX = 0;
            currVY = 0;
            vAngular = 0;
        }

        if (currY > hMax) {
            hMax = currY;
        }
        if (currX > xMax) {
            xMax = currX;
        }

        historial.add(new double[]{t, currX, currY, vTotal(), aceAct});
    }

    public double getEnergiaMecanicaTotal() {
        double cinetica = 0.5 * m * Math.pow(vTotal(), 2);
        double potencial = m * g * currY;
        return cinetica + potencial;
    }

    public double getMaxEnergiaInicial() {
        // Energía total con la que empieza el vuelo (al final de la rampa)
        return 0.5 * m * Math.pow(v1, 2) + (m * g * h);
    }

    public double vTotal() {
        return Math.sqrt(currVX * currVX + currVY * currVY);
    }
}
