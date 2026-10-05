package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionCatapulta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelCatapulta extends JPanel {

    private SimulacionCatapulta modelo;
    private GraficaCatapulta panelGraficas;
    private Timer timer;
    private boolean enEjecucion = false;

    public PanelCatapulta(SimulacionCatapulta modelo, GraficaCatapulta panelGraficas) {
        this.modelo = modelo;
        this.panelGraficas = panelGraficas;
        setBackground(new Color(15, 20, 30));

        // Bucle de animación (Timer de Swing)
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enEjecucion) {
                    modelo.actualizarEstadoEuler();
                    panelGraficas.refrescar();
                    repaint();

                    // Condición de término: Si el proyectil cae al suelo (y <= 0) o se rompe la estructura
                    if ((modelo.y <= 0 && modelo.tiempo > 0.1) || modelo.isEstructuraRota()) {
                        enEjecucion = false;
                        timer.stop();
                        System.out.println("-> [Animación] Proyectil tocó el suelo. Registrando simulación...");
                        modelo.finalizarSimulacion(); // Guardado automático en SQLite
                    }
                }
            }
        });
    }

    public void iniciarLanzamiento() {
        if (enEjecucion) return;
        modelo.reset();
        panelGraficas.refrescar();
        enEjecucion = true;
        timer.start();
    }

    public void reset() {
        if (timer != null) timer.stop();
        enEjecucion = false;
        modelo.reset();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // 1. Dibujar suelo
        g2d.setColor(new Color(40, 50, 60));
        g2d.fillRect(0, alto - 50, ancho, 50);
        g2d.setColor(new Color(70, 80, 95));
        g2d.drawLine(0, alto - 50, ancho, alto - 50);

        // 2. Dibujar estructura básica de la catapulta
        g2d.setColor(new Color(120, 60, 30));
        g2d.fillRect(80, alto - 130, 25, 80);
        g2d.fillRect(65, alto - 130, 55, 15);

        // 3. Dibujar proyectil en su posición actual (escalada para la vista)
        int px = 110 + (int) (modelo.x * 15);
        int py = (alto - 50) - (int) (modelo.y * 15);

        if (modelo.isEstructuraRota()) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2d.drawString("¡ESTRUCTURA FRACTURADA POR EXCESO DE TENSIÓN!", 50, 80);
        } else {
            // Dibujar trayectoria o estela simple si está en movimiento
            g2d.setColor(new Color(231, 76, 60));
            g2d.fillOval(px - 9, py - 9, 18, 18);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(px - 9, py - 9, 18, 18);
        }

        // ==========================================
        // 4. RESTAURACIÓN DEL HUD (Panel de Información)
        // ==========================================
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(20, 20, 320, 150, 15, 15);
        g2d.setColor(new Color(41, 128, 185));
        g2d.drawRoundRect(20, 20, 320, 150, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.drawString("📊 ESTADÍSTICAS EN TIEMPO REAL", 35, 45);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        double velocidadActual = Math.sqrt(modelo.vx * modelo.vx + modelo.vy * modelo.vy);

        g2d.drawString(String.format("⏱️ Tiempo: %.2f s", modelo.tiempo), 35, 70);
        g2d.drawString(String.format("📍 Posición X: %.2f m", modelo.x), 35, 95);
        g2d.drawString(String.format("📈 Altura Y: %.2f m", Math.max(0, modelo.y)), 35, 120);
        g2d.drawString(String.format("🚀 Velocidad: %.2f m/s", velocidadActual), 35, 145);
    }
}