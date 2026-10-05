package com.pos.graficafisica.view;

import com.pos.graficafisica.model.ConexionDB;
import com.pos.graficafisica.model.SimulacionParabola;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class PanelParabola extends JPanel {
    private SimulacionParabola modelo;
    private PanelGrafico gVel, gAlt, gDist, gAce;
    private DefaultTableModel tablaModelo; 
    
    private double escala = 60.0;
    private int ox = 100, oy = 650;
    private double camX = 0, camY = 0;
    private Point mousePt;
    private Timer timer;
    private boolean run = false;
    private String materialActual = "Goma", formaActual = "Esfera";
    private double limiteX = 150.0;
    private double xMaxParabola = 0, yMaxParabola = 0, xImpactoParabola = 0;
    private boolean mostrarCruz = false;
    private double errorPrecision = 0;
    
    private boolean hudExpandido = true;
    private final int HUD_X = 15, HUD_Y = 15, HUD_WIDTH = 310;

    public PanelParabola(SimulacionParabola modelo, PanelGrafico gVel, PanelGrafico gAlt, PanelGrafico gDist, PanelGrafico gAce) {
        this.modelo = modelo;
        this.gVel = gVel;
        this.gAlt = gAlt;
        this.gDist = gDist;
        this.gAce = gAce;
        this.tablaModelo = new DefaultTableModel(new Object[]{"Tiempo (s)", "Pos X (m)", "Pos Y (m)", "Vel (m/s)"}, 0);

        setBackground(new Color(30, 30, 35));

        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) escala *= 1.1;
            else escala /= 1.1;
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                if (e.getX() >= HUD_X && e.getX() <= HUD_X + HUD_WIDTH && 
                    e.getY() >= HUD_Y && e.getY() <= HUD_Y + 45) {
                    hudExpandido = !hudExpandido;
                }
                mousePt = e.getPoint(); 
                repaint();
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                camX += (e.getX() - mousePt.x);
                camY += (e.getY() - mousePt.y);
                mousePt = e.getPoint();
                repaint();
            }
        });

        timer = new Timer(16, e -> {
            if (run) {
                modelo.avanzarPaso(0.016, limiteX); 
                
                tablaModelo.addRow(new Object[]{
                    String.format("%.2f", modelo.t),
                    String.format("%.2f", modelo.currX),
                    String.format("%.2f", modelo.currY),
                    String.format("%.2f", modelo.vTotal())
                });

                if (modelo.numRebotes == 0 && modelo.t > 1.0) {
                    if (modelo.currY > yMaxParabola) {
                        yMaxParabola = modelo.currY;
                        xMaxParabola = modelo.currX;
                    }
                } else if (modelo.numRebotes == 1 && xImpactoParabola == 0) {
                    xImpactoParabola = modelo.currX;
                    calcularError(modelo.currX);
                }

                boolean seDetuvo = (Math.abs(modelo.currVX) < 0.05 && modelo.currY <= 0.01 && modelo.t > 1.5);
                if (seDetuvo || modelo.currX >= limiteX) {
                    run = false;
                    mostrarCruz = true;
                    // GUARDAR EN BASE DE DATOS AUTOMÁTICAMENTE
                    ConexionDB.insertarLanzamientoParabola(modelo.h, modelo.L, modelo.m, modelo.vInicial, modelo.currX);
                }
                actualizarTodo(); 
            }
        });
        timer.start();
    }

    public DefaultTableModel getTablaModelo() { return tablaModelo; }

    public void play() { this.run = true; this.mostrarCruz = false; repaint(); }
    public void pause() { this.run = false; repaint(); }

    public void reset() {
        run = false; 
        xMaxParabola = 0; yMaxParabola = 0; xImpactoParabola = 0;
        mostrarCruz = false; errorPrecision = 0;
        tablaModelo.setRowCount(0); 
        modelo.reset(); 
        actualizarTodo();
    }

    private void actualizarTodo() {
        repaint();
        if (gVel != null) gVel.repaint();
        if (gAlt != null) gAlt.repaint();
        if (gDist != null) gDist.repaint();
        if (gAce != null) gAce.repaint();
    }
    
    private void calcularError(double xImpactoReal) {
        double vx0 = modelo.vInicial * Math.cos(modelo.theta);
        double vy0 = modelo.vInicial * Math.sin(modelo.theta);
        double a = -0.5 * modelo.g;
        double disc = vy0 * vy0 - 4 * a * modelo.h;
        if (disc >= 0) {
            double tIdeal = (-vy0 - Math.sqrt(disc)) / (2 * a);
            double xIdeal = modelo.L + (vx0 * tIdeal);
            errorPrecision = Math.abs((xIdeal - xImpactoReal) / xIdeal) * 100;
        }
    }

    public void setFormaYMaterial(String forma, String material) {
        this.formaActual = forma;
        this.materialActual = material;
        modelo.configurarMaterial(material); 
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gph) {
        super.paintComponent(gph);
        Graphics2D g2 = (Graphics2D) gph;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        AffineTransform base = g2.getTransform();
        g2.translate(camX, camY);

        // Suelo y rampa
        g2.setPaint(new GradientPaint(ox, oy, new Color(44, 62, 80), ox, oy + 50, new Color(20, 30, 48)));
        g2.fillRect(ox - 1000, oy, 10000, 500);
        g2.setColor(new Color(52, 152, 219, 100));
        g2.drawLine(ox - 1000, oy, 10000, oy);

        int xR = (int) (modelo.L * escala), yR = (int) (modelo.h * escala);
        g2.setPaint(new GradientPaint(ox, oy - yR, new Color(149, 165, 166), ox + xR, oy, new Color(127, 140, 141)));
        g2.fillPolygon(new int[]{ox, ox + xR, ox + xR}, new int[]{oy, oy, oy - yR}, 3);
        g2.setColor(Color.WHITE);
        g2.drawPolygon(new int[]{ox, ox + xR, ox + xR}, new int[]{oy, oy, oy - yR}, 3);

        dibujarTrayectoriaIdeal(g2);
        dibujarTrayectoriaReal(g2);
        dibujarCotas(g2); 
        if (mostrarCruz) dibujarCruzMaxima(g2);

        double px, py;
        if (modelo.t < 1.0) { 
            px = ox + (modelo.t * xR); py = oy - (modelo.t * yR);
            dibujarResorte(g2, ox, oy, (int) px, (int) py);
        } else {
            px = ox + modelo.currX * escala; py = oy - modelo.currY * escala;
        }

        AffineTransform proyectilTrans = g2.getTransform();
        g2.translate(px, py);
        g2.rotate(modelo.rotAngle); 
        dibujarFormaConMaterial(g2, 0, 0);
        g2.setTransform(proyectilTrans);

        g2.setTransform(base);
        dibujarHUD(g2);
    }

   private void dibujarHUD(Graphics2D g2) {
        // Aumentamos el alto para dar espacio a los nuevos datos
        int altoHud = hudExpandido ? 480 : 45; 
        g2.setPaint(new GradientPaint(HUD_X, HUD_Y, new Color(44, 62, 80, 240), HUD_X, HUD_Y + altoHud, new Color(20, 30, 48, 240)));
        g2.fillRoundRect(HUD_X, HUD_Y, HUD_WIDTH, altoHud, 15, 15);
        g2.setColor(new Color(52, 152, 219));
        g2.drawRoundRect(HUD_X, HUD_Y, HUD_WIDTH, altoHud, 15, 15);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString(hudExpandido ? "▼ MONITOR DE FÍSICA" : "▲ MOSTRAR MONITOR", HUD_X + 20, HUD_Y + 30);
        
        if (!hudExpandido) return;
        
        int y = HUD_Y + 65;
        g2.setFont(new Font("Consolas", Font.PLAIN, 13));
        
        // Datos de posición y tiempo
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("➤ TIEMPO     : %6.2f s", modelo.t), HUD_X + 20, y); y += 25;
        g2.drawString(String.format("➤ DISTANCIA X: %6.2f m", modelo.currX), HUD_X + 20, y); y += 25;
        g2.drawString(String.format("➤ ALTURA Y   : %6.2f m", modelo.currY), HUD_X + 20, y); y += 25;
        
        // DATOS DE ROTACIÓN (Nuevos)
        g2.setColor(new Color(155, 89, 182)); // Color Púrpura para rotación
        g2.drawString(String.format("➤ ROTACIÓN   : %6.2f rad", modelo.rotAngle), HUD_X + 20, y); y += 25;
        g2.drawString(String.format("➤ VEL. ANG   : %6.2f rad/s", modelo.vAngular), HUD_X + 20, y); y += 25;

        // DATOS DEL OBJETO Y ENTORNO
        g2.setColor(new Color(241, 196, 15));
        g2.drawString(String.format("➤ REBOTES    : %6d", modelo.numRebotes), HUD_X + 20, y); y += 25;
        g2.drawString(String.format("➤ RADIO EFEC : %6.2f m", modelo.getRadioEfectivo()), HUD_X + 20, y); y += 25;
        
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("➤ X IMPACTO  : %6.2f m", xImpactoParabola), HUD_X + 20, y); y += 25;
        g2.drawString(String.format("➤ ERROR      : %6.2f %%", errorPrecision), HUD_X + 20, y); y += 35;
        
        // Barras de Energía
        double eCin = 0.5 * modelo.m * Math.pow(modelo.vTotal(), 2);
        double ePot = modelo.m * modelo.g * modelo.currY;
        double eMax = modelo.getMaxEnergiaInicial();
        
        dibujarBarraEnergia(g2, "ENERGÍA CINÉTICA (J)", eCin, eMax, new Color(52, 152, 219), HUD_X + 30, y); y += 50;
        dibujarBarraEnergia(g2, "ENERGÍA POTENCIAL (J)", ePot, eMax, new Color(46, 204, 113), HUD_X + 30, y);
    }

    private void dibujarBarraEnergia(Graphics2D g2, String label, double valor, double max, Color color, int x, int y) {
        int ancho = 250;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString(label, x, y - 8);
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(x, y, ancho, 14, 5, 5);
        int valAncho = (int) (ancho * Math.min(valor / (max > 0 ? max : 1), 1.0));
        g2.setPaint(new GradientPaint(x, y, color, x + valAncho, y, color.darker()));
        g2.fillRoundRect(x, y, valAncho, 14, 5, 5);
    }

    private void dibujarTrayectoriaIdeal(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        g2.setColor(new Color(46, 204, 113, 120));
        Path2D.Double path = new Path2D.Double();
        double vx0 = modelo.vInicial * Math.cos(modelo.theta);
        double vy0 = modelo.vInicial * Math.sin(modelo.theta);
        path.moveTo(ox + modelo.L * escala, oy - modelo.h * escala);
        for (double ti = 0; ti < 5; ti += 0.05) {
            double ix = modelo.L + (vx0 * ti);
            double iy = modelo.h + (vy0 * ti) - (0.5 * modelo.g * ti * ti);
            if (iy < 0) break;
            path.lineTo(ox + ix * escala, oy - iy * escala);
        }
        g2.draw(path);
    }

    private void dibujarResorte(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(211, 84, 0));
        g2.setStroke(new BasicStroke(3));
        int loops = 10;
        double dx = (x2 - x1) / (double) loops;
        double dy = (y2 - y1) / (double) loops;
        double ox_v = -(y2 - y1) / 10.0;
        double oy_v = (x2 - x1) / 10.0;
        Path2D.Double resorte = new Path2D.Double();
        resorte.moveTo(x1, y1);
        for (int i = 0; i < loops; i++) {
            double midX = x1 + dx * (i + 0.5);
            double midY = y1 + dy * (i + 0.5);
            int sign = (i % 2 == 0) ? 1 : -1;
            resorte.lineTo(midX + sign * ox_v, midY + sign * oy_v);
        }
        resorte.lineTo(x2, y2);
        g2.draw(resorte);
    }

    private void dibujarCotas(Graphics2D g2) {
        if (xMaxParabola > 0) {
            g2.setColor(new Color(231, 76, 60));
            int cx = ox + (int)(xMaxParabola * escala);
            int cy = oy - (int)(yMaxParabola * escala);
            g2.fillOval(cx-4, cy-4, 8, 8);
            g2.drawString(String.format("H-Max: %.2f m", yMaxParabola), cx + 10, cy);
        }
        if (xImpactoParabola > 0) {
            g2.setColor(new Color(46, 204, 113));
            int ix = ox + (int)(xImpactoParabola * escala);
            g2.drawLine(ix, oy - 10, ix, oy + 10);
            g2.drawString(String.format("X-Impacto: %.2f m", xImpactoParabola), ix - 30, oy + 25);
        }
    }

    private void dibujarTrayectoriaReal(Graphics2D g2) {
        if (modelo.historial.size() < 2) return;
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(231, 76, 60));
        Path2D.Double path = new Path2D.Double();
        boolean first = true;
        for (double[] d : modelo.historial) {
            if (first) { path.moveTo(ox + d[1]*escala, oy - d[2]*escala); first = false; }
            else path.lineTo(ox + d[1]*escala, oy - d[2]*escala);
        }
        g2.draw(path);
    }

    private void dibujarCruzMaxima(Graphics2D g2) {
        g2.setColor(new Color(52, 152, 219));
        int cx = ox + (int)(modelo.currX * escala);
        int cy = oy - (int)(modelo.currY * escala);
        g2.drawLine(cx - 15, cy, cx + 15, cy);
        g2.drawLine(cx, cy - 15, cx, cy + 15);
    }

    private void dibujarFormaConMaterial(Graphics2D g2, int x, int y) {
        Color base;
        switch(materialActual) {
            case "Acero": base = new Color(149, 165, 166); break;
            case "Madera": base = new Color(139, 69, 19); break;
            case "Hielo": base = new Color(173, 216, 230); break;
            default: base = new Color(231, 76, 60); break;
        }
        g2.setPaint(new RadialGradientPaint(new Point(x-5, y-5), 15, new float[]{0f, 1f}, new Color[]{Color.WHITE, base}));
        int size = 22;
        if (formaActual.equals("Esfera")) {
            g2.fillOval(x - size/2, y - size/2, size, size);
            g2.setColor(Color.BLACK);
            g2.drawOval(x - size/2, y - size/2, size, size);
            g2.drawLine(x, y, x + size/2, y); 
        } else {
            g2.fillRect(x - size/2, y - size/2, size, size);
        }
    }
}