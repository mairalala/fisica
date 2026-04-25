package com.pos.graficafisica;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class PanelGrafico extends JPanel {

    private String tit;
    private ArrayList<double[]> datos;
    private int idx;
    private Color col;

    // --- NUEVAS VARIABLES PARA LA ANIMACIÓN (ESTÁTICAS PARA ACCESO DESDE VENTANA) ---
    private static int puntoCorte = -1; 
    private static boolean modoResaltado = false;

    public PanelGrafico(String t, ArrayList<double[]> d, int i, Color c) {
        this.tit = t;
        this.datos = d;
        this.idx = i;
        this.col = c;
        setBackground(new Color(10, 12, 15));
    }

    // --- NUEVOS MÉTODOS DE CONTROL ---
    public static void setPuntoCorte(int valor) {
        puntoCorte = valor;
    }

    public static void setModoResaltado(boolean valor) {
        modoResaltado = valor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 50;

        // 1. Cuadrícula de fondo
        g2.setColor(new Color(30, 35, 45));
        for (int i = 0; i < w; i += 40) g2.drawLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) g2.drawLine(0, i, w, i);

        // 2. Ejes
        int ejeY = h / 2; 
        g2.setColor(new Color(100, 100, 120));
        g2.drawLine(padding, 20, padding, h - 20);
        g2.drawLine(padding, ejeY, w - 20, ejeY);

        // 3. Título
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(col);
        g2.drawString(tit.toUpperCase(), padding + 10, 25);

        if (datos == null || datos.size() < 2) {
            g2.setColor(Color.GRAY);
            g2.drawString("ESPERANDO DATOS...", w / 2 - 50, h / 2);
            return;
        }

        // 4. Lógica de dibujo
        Path2D.Double path = new Path2D.Double();
        boolean first = true;
        double escalaX = (w - padding - 40) / 10.0; 

        double escalaY;
        String tituloLower = tit.toLowerCase();
        if (tituloLower.contains("elongación") || tituloLower.contains("posición")) {
            escalaY = h * 0.4;
        } else if (tituloLower.contains("velocidad")) {
            escalaY = 60.0;
        } else {
            escalaY = 20.0;
        }

        // --- MODIFICACIÓN PARA ANIMACIÓN: Determinamos el límite de puntos ---
        int limite = (puntoCorte == -1) ? datos.size() : Math.min(puntoCorte, datos.size());

        for (int i = 0; i < limite; i++) {
            double[] d = datos.get(i);
            double xVal = padding + (d[0] * escalaX);
            double yVal = ejeY - (d[idx] * escalaY); 

            if (first) {
                path.moveTo(xVal, yVal);
                first = false;
            } else {
                path.lineTo(xVal, yVal);
            }
        }

        // --- MEJORA: EFECTO GLOW (NEÓN) ---
        g2.setStroke(new BasicStroke(4f));
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 40));
        g2.draw(path);
        
        // Línea principal
        // Si estamos en modo resaltado (reproducción), usamos Blanco para que destaque
        if (modoResaltado) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.5f));
        } else {
            g2.setColor(col);
            g2.setStroke(new BasicStroke(2.0f));
        }
        g2.draw(path);

        // 5. Relleno Degradado bajo la curva
        if (!path.getCurrentPoint().equals(null)) {
            Path2D.Double fillPath = (Path2D.Double) path.clone();
            fillPath.lineTo(padding + (datos.get(limite - 1)[0] * escalaX), ejeY);
            fillPath.lineTo(padding, ejeY);
            fillPath.closePath();
            g2.setPaint(new GradientPaint(0, ejeY - 50, new Color(col.getRed(), col.getGreen(), col.getBlue(), 30), 0, ejeY, new Color(0, 0, 0, 0)));
            g2.fill(fillPath);
        }

        // 6. Indicador de punto actual
        double[] last = datos.get(limite - 1);
        int lastX = (int) (padding + (last[0] * escalaX));
        int lastY = (int) (ejeY - (last[idx] * escalaY));

        if (lastX < w) {
            g2.setColor(Color.WHITE);
            g2.fillOval(lastX - 3, lastY - 3, 6, 6);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(modoResaltado ? Color.WHITE : col);
            g2.drawOval(lastX - 6, lastY - 6, 12, 12);
        }
    }
}