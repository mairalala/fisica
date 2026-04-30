package com.pos.graficafisica;

import java.util.ArrayList;
import java.util.List;

public class SimulacionParabola {

    public double m = 0.25, h = 2.5, L = 4.0, g = 9.8, k = 100.0, x0 = 1.2;
    public double muRampa = 0.15;
    public double muSuelo = 0.25;
    public double elasticidad = 0.75;
    public double bViscoso = 0.05, cAire = 0.02;
    public double rotAngle = 0;
    public double vAngular = 0;

    public double densidadAire = 1.225;
    private double radioEfectivo = 0.15;

    public double anguloRampa = 0, anguloLanzamiento = 0, anguloReboteAct = 0;
    public double t = 0, currX = 0, currY = 0, currVX = 0, currVY = 0;
    public double v1 = 0, theta = 0, vInicial = 0, aceAct = 0;
    public double hMax = 0, xMax = 0;
    public int numRebotes = 0;
    public List<double[]> historial = new ArrayList<>();

    public double getRadioEfectivo() {
        return radioEfectivo;
    } // Getter para el HUD

    public void configurarMaterial(String material) {
        switch (material) {
            case "Acero":
                this.elasticidad = 0.55;
                this.muSuelo = 0.15;
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
                this.muSuelo = 0.02;
                break;
            case "Goma":
            default:
                this.elasticidad = 0.85;
                this.muSuelo = 0.60;
                break;
        }
    }

    public void recalcular() {
        theta = Math.atan2(h, L);
        anguloRampa = Math.toDegrees(theta);
        anguloLanzamiento = anguloRampa;
        double hipotenusaRampa = Math.sqrt(L * L + h * h);

        // Cálculo dinámico del radio[cite: 2]
        this.radioEfectivo = Math.pow((3 * m) / (4 * Math.PI * densidadAire), 1.0 / 3.0);

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

    private double[] calcularAceleracion(double vx, double vy, double y) {
        double vTotal = Math.sqrt(vx * vx + vy * vy);
        double fResX = (bViscoso * vx) + (cAire * vTotal * vx);
        double fResY = (bViscoso * vy) + (cAire * vTotal * vy);

        double ax = -fResX / m;
        double ay = -g - (fResY / m);

        if (y <= 0.01) {
            double fRozSuelo = muSuelo * m * g;
            if (Math.abs(vx) > 0.01) {
                ax -= (vx > 0 ? 1 : -1) * fRozSuelo / m;
            }
        }
        return new double[]{ax, ay};
    }

    public void avanzarPaso(double dt, double limiteX) {
        if (t < 1.0) {
            t += dt * 2;
            vAngular = v1 / (radioEfectivo * 2);
            rotAngle += vAngular * dt;
            return;
        }

        if (currY > 0 || numRebotes < 25) {
            // RK4 Implementation[cite: 2]
            double[] a1 = calcularAceleracion(currVX, currVY, currY);
            double k1vx = a1[0] * dt;
            double k1vy = a1[1] * dt;
            double k1x = currVX * dt;
            double k1y = currVY * dt;

            double[] a2 = calcularAceleracion(currVX + k1vx / 2, currVY + k1vy / 2, currY + k1y / 2);
            double k2vx = a2[0] * dt;
            double k2vy = a2[1] * dt;
            double k2x = (currVX + k1vx / 2) * dt;
            double k2y = (currVY + k1vy / 2) * dt;

            double[] a3 = calcularAceleracion(currVX + k2vx / 2, currVY + k2vy / 2, currY + k2y / 2);
            double k3vx = a3[0] * dt;
            double k3vy = a3[1] * dt;
            double k3x = (currVX + k2vx / 2) * dt;
            double k3y = (currVY + k2vy / 2) * dt;

            double[] a4 = calcularAceleracion(currVX + k3vx, currVY + k3vy, currY + k3y);
            double k4vx = a4[0] * dt;
            double k4vy = a4[1] * dt;
            double k4x = (currVX + k3vx) * dt;
            double k4y = (currVY + k3vy) * dt;

            currX += (k1x + 2 * k2x + 2 * k3x + k4x) / 6.0;
            currY += (k1y + 2 * k2y + 2 * k3y + k4y) / 6.0;
            currVX += (k1vx + 2 * k2vx + 2 * k3vx + k4vx) / 6.0;
            currVY += (k1vy + 2 * k2vy + 2 * k3vy + k4vy) / 6.0;

            double[] accFinal = calcularAceleracion(currVX, currVY, currY);
            aceAct = Math.sqrt(accFinal[0] * accFinal[0] + accFinal[1] * accFinal[1]);

            if (currY <= 0.01) {
                vAngular = (Math.abs(currVX) > 0.01) ? currVX / radioEfectivo : vAngular * 0.9;
            } else {
                vAngular *= 0.995;
            }
            rotAngle += vAngular * dt;
            t += dt;

            if (currY <= 0) {
                currY = 0;
                currVY = -currVY * elasticidad;
                currVX *= (0.85 + (elasticidad * 0.1));
                if (Math.abs(currVX) > 0.01) {
                    anguloReboteAct = Math.toDegrees(Math.atan2(currVY, currVX));
                }
                numRebotes++;
                if (Math.abs(currVY) < 0.15) {
                    currVY = 0;
                }
            }
        }

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

    public double getMaxEnergiaInicial() {
        return 0.5 * m * Math.pow(v1, 2) + (m * g * h);
    }

    public double vTotal() {
        return Math.sqrt(currVX * currVX + currVY * currVY);
    }
}
