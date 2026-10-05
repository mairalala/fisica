package com.pos.graficafisica.model;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SimulacionCatapulta {

    public double masaProyectil = 1.0;
    public double anguloLanzamiento = 45.0;
    public double gravedad = 9.81;
    public double constanteResorte = 150.0;
    public double deformacion = 0.5;
    public double resistenciaEstructura = 60.0;
    public boolean estructuraRota = false;

    public double x = 0;
    public double y = 0;
    public double vx = 0;
    public double vy = 0;
    public double tiempo = 0;
    public double dt = 0.05;

    private List<double[]> historialDatos = new ArrayList<>();

    public SimulacionCatapulta() {
        reset();
    }

    public void verificarIntegridad() {
        double fuerzaAplicada = constanteResorte * deformacion;
        if (fuerzaAplicada > resistenciaEstructura) {
            estructuraRota = true;
        }
    }

    public void actualizarEstadoEuler() {
        if (estructuraRota) return;
        double ax = 0;
        double ay = -gravedad;
        vx += ax * dt;
        vy += ay * dt;
        x += vx * dt;
        y += vy * dt;
        tiempo += dt;
        historialDatos.add(new double[]{tiempo, x, y, vx, vy});
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
        this.tiempo = 0;
        this.estructuraRota = false;
        verificarIntegridad();
        if (!estructuraRota) {
            double fuerzaLanzamiento = constanteResorte * deformacion;
            double vInicial = Math.sqrt((2 * fuerzaLanzamiento * deformacion) / masaProyectil);
            double rad = Math.toRadians(anguloLanzamiento);
            this.vx = vInicial * Math.cos(rad);
            this.vy = vInicial * Math.sin(rad);
        } else {
            this.vx = 0;
            this.vy = 0;
        }
        historialDatos.clear();
    }

    public void finalizarSimulacion() {
        double vMax = getVMax();
        double hMax = getHMax();
        double impulso = getImpulsoTotal();
        String estado = estructuraRota ? "FRACTURA CRÍTICA" : "OPERATIVA";

        System.out.println("-> [Simulación] Finalizada. Guardando en BD...");
        ConexionDB.guardarLanzamientoCatapulta(
                masaProyectil,
                anguloLanzamiento,
                deformacion,
                vMax,
                hMax,
                impulso,
                estado
        );
    }

    public void exportarDatosCSV(String nombreArchivo) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo))) {
            pw.println("Tiempo,X,Y,Vx,Vy");
            for (double[] d : historialDatos) {
                pw.printf("%.3f,%.3f,%.3f,%.3f,%.3f%n", d[0], d[1], d[2], d[3], d[4]);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public double getImpulsoTotal() {
        if (historialDatos.isEmpty()) return 0;
        double[] finalState = historialDatos.get(historialDatos.size() - 1);
        double vi = Math.sqrt(finalState[3] * finalState[3] + finalState[4] * finalState[4]);
        return masaProyectil * (vi * 0.8 - (-vi));
    }

    public List<double[]> getHistorialDatos() { return historialDatos; }
    public boolean isEstructuraRota() { return estructuraRota; }

    public double getVMax() {
        double maxV = 0;
        for (double[] d : historialDatos) {
            double v = Math.sqrt(d[3] * d[3] + d[4] * d[4]);
            if (v > maxV) maxV = v;
        }
        return maxV;
    }

    public double getHMax() {
        double maxH = 0;
        for (double[] d : historialDatos) { if (d[2] > maxH) maxH = d[2]; }
        return maxH;
    }
}