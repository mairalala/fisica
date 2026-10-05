package com.pos.graficafisica.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraficaCatapulta extends JTabbedPane {
    private PanelGrafico gPosicionX, gPosicionY, gVelocidadX, gVelocidadY;

    public GraficaCatapulta(List<double[]> historial) {
        // Convertimos o casteamos de forma segura a ArrayList para que coincida con PanelGrafico
        ArrayList<double[]> historialArrayList = (historial instanceof ArrayList)
                ? (ArrayList<double[]>) historial
                : new ArrayList<>(historial);

        gPosicionX = new PanelGrafico("Posición X (m)", historialArrayList, 1, Color.CYAN);
        gPosicionY = new PanelGrafico("Altura Y (m)", historialArrayList, 2, Color.GREEN);
        gVelocidadX = new PanelGrafico("Velocidad X (m/s)", historialArrayList, 3, Color.ORANGE);
        gVelocidadY = new PanelGrafico("Velocidad Y (m/s)", historialArrayList, 4, Color.RED);

        addTab("📏 POSICIÓN X", gPosicionX);
        addTab("📈 ALTURA (Y)", gPosicionY);
        addTab("⏩ VEL. HORIZONTAL", gVelocidadX);
        addTab("⬆️ VEL. VERTICAL", gVelocidadY);

        setPreferredSize(new Dimension(0, 300));
        setBackground(new Color(44, 62, 80));
    }

    public void refrescar() {
        gPosicionX.repaint();
        gPosicionY.repaint();
        gVelocidadX.repaint();
        gVelocidadY.repaint();
    }
}