package com.pos.graficafisica;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GraficaParabolas extends JTabbedPane {
    private PanelGrafico gVelocidad, gAltura, gDistancia, gAceleracion;

    public GraficaParabolas(ArrayList<double[]> historial) {
        gVelocidad = new PanelGrafico("Velocidad (v/t)", historial, 3, Color.CYAN);
        gAltura = new PanelGrafico("Altura (y/t)", historial, 2, Color.GREEN);
        gDistancia = new PanelGrafico("Alcance (x/t)", historial, 1, Color.MAGENTA);
        gAceleracion = new PanelGrafico("Aceleración (a/t)", historial, 4, Color.ORANGE);

        addTab("📊 ALTURA (Y)", gAltura);
        addTab("⚡ VELOCIDAD", gVelocidad);
        addTab("📏 ALCANCE (X)", gDistancia);
        addTab("🚀 ACELERACIÓN", gAceleracion);
        
        setPreferredSize(new Dimension(0, 350));
    }

    public void refrescar() {
        gVelocidad.repaint();
        gAltura.repaint();
        gDistancia.repaint();
        gAceleracion.repaint();
    }

    public PanelGrafico getgVelocidad() { return gVelocidad; }
    public PanelGrafico getgAltura() { return gAltura; }
    public PanelGrafico getgDistancia() { return gDistancia; }
    public PanelGrafico getgAceleracion() { return gAceleracion; }
}