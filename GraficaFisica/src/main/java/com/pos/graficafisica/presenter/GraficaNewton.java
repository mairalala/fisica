package com.pos.graficafisica.presenter;

import com.pos.graficafisica.view.PanelGrafico;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GraficaNewton extends JTabbedPane {
    private PanelGrafico gVelocidadA, gAceleracionA, gPosicionA, gVelocidadB;

    public GraficaNewton(List<double[]> historial) {
        // Mapeo de índices según el historial de SimulacionNewton:
        // [0: tiempo, 1: posicionA, 2: velocidadA, 3: aceleracionA, 4: posicionB, 5: velocidadB, 6: aceleracionB]
        gVelocidadA = new PanelGrafico("Velocidad Objeto A (v/t)", historial, 2, Color.CYAN);
        gAceleracionA = new PanelGrafico("Aceleración Objeto A (a/t)", historial, 3, Color.ORANGE);
        gPosicionA = new PanelGrafico("Posición Objeto A (x/t)", historial, 1, Color.GREEN);
        gVelocidadB = new PanelGrafico("Velocidad Objeto B (v/t)", historial, 5, Color.MAGENTA);

        addTab("⚡ VELOCIDAD (A)", gVelocidadA);
        addTab("🚀 ACELERACIÓN (A)", gAceleracionA);
        addTab("📏 POSICIÓN (A)", gPosicionA);
        addTab("🔄 VELOCIDAD (B)", gVelocidadB);

        setPreferredSize(new Dimension(0, 350));
    }

    public void refrescar() {
        gVelocidadA.repaint();
        gAceleracionA.repaint();
        gPosicionA.repaint();
        gVelocidadB.repaint();
    }

    public PanelGrafico getgVelocidadA() { return gVelocidadA; }
    public PanelGrafico getgAceleracionA() { return gAceleracionA; }
    public PanelGrafico getgPosicionA() { return gPosicionA; }
    public PanelGrafico getgVelocidadB() { return gVelocidadB; }
}