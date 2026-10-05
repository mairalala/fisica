package com.pos.graficafisica.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class PanelGrafico extends JPanel {

    private String tit;
    private ArrayList<double[]> datos;
    private int idx;
    private Color col;

    private static int puntoCorte = -1; 
    private static boolean modoResaltado = false;
    
    // --- VARIABLE DE ZOOM ---
    private double zoomFactor = 1.0; 

    public PanelGrafico(String t, ArrayList<double[]> d, int i, Color c) {
        this.tit = t;
        this.datos = d;
        this.idx = i;
        this.col = c;
        setBackground(new Color(10, 12, 15));
        setFocusable(true);

        // 1. ZOOM CON MOUSE
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    zoomFactor *= 1.1;
                } else {
                    zoomFactor /= 1.1;
                }
                repaint();
            }
        });

        // 2. ZOOM CON TECLADO (Ctrl + / Ctrl -)
        configurarKeyBindings();
    }

    // --- NUEVOS MÉTODOS (GETTER Y SETTER) PARA SOLUCIONAR EL ERROR ---
    public double getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoom) {
        this.zoomFactor = zoom;
    }

    private void configurarKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), "zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), "zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_DOWN_MASK), "zoomOut");

        am.put("zoomIn", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { zoomFactor *= 1.1; repaint(); }
        });
        am.put("zoomOut", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { zoomFactor /= 1.1; repaint(); }
        });
    }

    public static void setPuntoCorte(int valor) { puntoCorte = valor; }
    public static void setModoResaltado(boolean valor) { modoResaltado = valor; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 50;

        double maxAbsY = 0.1; 
        double maxTime = 10.0; 

        if (datos != null && !datos.isEmpty()) {
            int limiteBusqueda = (puntoCorte == -1) ? datos.size() : Math.min(puntoCorte, datos.size());
            for (int i = 0; i < limiteBusqueda; i++) {
                double[] d = datos.get(i);
                if (idx == 4) { 
                    maxAbsY = Math.max(maxAbsY, Math.abs(d[2]));
                    maxTime = Math.max(maxTime, Math.abs(d[1])); 
                } else {
                    maxAbsY = Math.max(maxAbsY, Math.abs(d[idx]));
                    maxTime = Math.max(maxTime, d[0]);
                }
            }
        }

        g2.setColor(new Color(30, 35, 45));
        for (int i = 0; i < w; i += 40) g2.drawLine(i, 0, i, h);
        for (int i = 0; i < h; i += 40) g2.drawLine(0, i, w, i);

        int ejeY = h / 2; 
        int ejeX_Centro = w / 2;
        g2.setColor(new Color(100, 100, 120));

        if (idx == 4) {
            g2.drawLine(ejeX_Centro, 20, ejeX_Centro, h - 20);
            g2.drawLine(padding, ejeY, w - 20, ejeY);
        } else {
            g2.drawLine(padding, 20, padding, h - 20);
            g2.drawLine(padding, ejeY, w - 20, ejeY);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(col);
        g2.drawString(tit.toUpperCase() + String.format(" | ZOOM: %.1fx", zoomFactor), padding + 10, 25);

        if (datos == null || datos.size() < 2) return;

        double escalaX, escalaY;
        if (idx == 4) {
            escalaX = ((w / 2.0 - padding - 20) / (maxTime * 1.1)) * zoomFactor;
            escalaY = ((h / 2.0 - 40) / (maxAbsY * 1.1)) * zoomFactor;
        } else {
            escalaX = ((w - padding - 40) / (maxTime * 1.1)) * zoomFactor;
            escalaY = ((h / 2.0 - 40) / (maxAbsY * 1.1)) * zoomFactor;
        }

        Path2D.Double path = new Path2D.Double();
        boolean first = true;
        int limite = (puntoCorte == -1) ? datos.size() : Math.min(puntoCorte, datos.size());

        for (int i = 0; i < limite; i++) {
            double[] d = datos.get(i);
            double xPos, yPos;
            if (idx == 4) {
                xPos = ejeX_Centro + (d[1] * escalaX);
                yPos = ejeY - (d[2] * escalaY);
            } else {
                xPos = padding + (d[0] * escalaX);
                yPos = ejeY - (d[idx] * escalaY);
            }
            if (first) { path.moveTo(xPos, yPos); first = false; }
            else { path.lineTo(xPos, yPos); }
        }

        g2.setStroke(new BasicStroke(4f));
        g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 40));
        g2.draw(path);
        
        g2.setColor(modoResaltado ? Color.WHITE : col);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(path);

        double[] last = datos.get(limite - 1);
        int lx = (idx == 4) ? (int)(ejeX_Centro + (last[1]*escalaX)) : (int)(padding + (last[0]*escalaX));
        int ly = (int)(ejeY - (last[idx == 4 ? 2 : idx]*escalaY));
        if (lx > 0 && lx < w && ly > 0 && ly < h) {
            g2.setColor(Color.WHITE);
            g2.fillOval(lx - 3, ly - 3, 6, 6);
        }
    }
}