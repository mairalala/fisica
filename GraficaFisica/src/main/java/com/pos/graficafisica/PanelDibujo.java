package com.pos.graficafisica;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class PanelDibujo extends JPanel {
    private SimulacionFisica modelo;
    private double escala = 60.0;
    private int ox = 100, oy = 650;
    private double camX = 0, camY = 0;
    private Point mousePt;
    private Timer timer;
    private boolean run = false;
    private String materialActual = "Goma", formaActual = "Esfera";
    private double limiteX = 150.0;
    private double xMaxParabola = 0, yMaxParabola = 0;
    private boolean mostrarCruz = false;
    private double errorPrecision = 0;
    private boolean datosGuardados = false; 
    
    // Variables para el HUD colapsable
    private boolean hudExpandido = true;
    private final int HUD_X = 15, HUD_Y = 15, HUD_WIDTH = 310;

    public PanelDibujo(SimulacionFisica modelo) {
        this.modelo = modelo;
        setBackground(new Color(248, 249, 249));

        // Control de Zoom
        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) escala *= 1.1;
            else escala /= 1.1;
            repaint();
        });

        // Control de Ratón (Click para HUD y Arrastre para Cámara)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                // Verificar si el clic es en la cabecera del HUD para colapsar
                if (e.getX() >= HUD_X && e.getX() <= HUD_X + HUD_WIDTH && 
                    e.getY() >= HUD_Y && e.getY() <= HUD_Y + 45) {
                    hudExpandido = !hudExpandido;
                    repaint();
                }
                mousePt = e.getPoint(); 
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
                
                if (modelo.numRebotes == 0) {
                    if (modelo.currY > yMaxParabola) {
                        yMaxParabola = modelo.currY;
                        xMaxParabola = modelo.currX;
                    }
                } else if (modelo.numRebotes == 1 && errorPrecision == 0) {
                    calcularError(modelo.currX);
                }

                boolean seDetuvo = (Math.abs(modelo.currVX) < 0.05 && modelo.currY <= 0.01 && modelo.t > 1.5);
                boolean salioDeLimite = (modelo.currX >= limiteX);

                if (seDetuvo || salioDeLimite) {
                    if (run) {
                        run = false;
                        mostrarCruz = true;
                        if (!datosGuardados) {
                            ConexionDB.guardarSimulacion(materialActual, modelo.vInicial, errorPrecision, modelo.numRebotes);
                            datosGuardados = true;
                        }
                    }
                }
                repaint(); 
            }
        });
        timer.start();
    }
    
    private void calcularError(double xImpactoReal) {
        double vx0 = modelo.vInicial * Math.cos(modelo.theta);
        double vy0 = modelo.vInicial * Math.sin(modelo.theta);
        double a = -0.5 * modelo.g;
        double b = vy0;
        double c = modelo.h;
        double disc = b * b - 4 * a * c;
        if (disc >= 0) {
            double tIdeal = (-b - Math.sqrt(disc)) / (2 * a);
            double xIdeal = modelo.L + (vx0 * tIdeal);
            errorPrecision = Math.abs((xIdeal - xImpactoReal) / xIdeal) * 100;
        }
    }

    public void toggle() {
        run = !run;
        if (run) { mostrarCruz = false; datosGuardados = false; }
        repaint(); 
    }

    public void reset() {
        run = false; camX = 0; camY = 0;
        xMaxParabola = 0; yMaxParabola = 0;
        mostrarCruz = false; errorPrecision = 0;
        datosGuardados = false; modelo.reset();
        repaint();
    }

    public void setFormaYMaterial(String forma, String material) {
        this.formaActual = forma;
        this.materialActual = material;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gph) {
        super.paintComponent(gph);
        Graphics2D g2 = (Graphics2D) gph;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.translate(camX, camY);

        // Dibujar Rampa y Suelo
        g2.setColor(new Color(189, 195, 199));
        int xR = (int) (modelo.L * escala), yR = (int) (modelo.h * escala);
        g2.fillPolygon(new int[]{ox, ox + xR, ox + xR}, new int[]{oy, oy, oy - yR}, 3);

        g2.setColor(new Color(44, 62, 80));
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(ox, oy, ox + (int) (limiteX * escala), oy);

        dibujarParabolaIdeal(g2);
        dibujarTrayectoriaReal(g2);
        dibujarCotas(g2); 
        if (mostrarCruz) dibujarCruzMaxima(g2);

        // Lógica de posición del objeto
        double px, py;
        double hip = Math.sqrt(modelo.L * modelo.L + modelo.h * modelo.h);
        double porc = 1.0 - (modelo.x0 / hip);

        if (!run && modelo.t == 0) {
            px = ox + (porc * xR); py = oy - (porc * yR);
            dibujarResorte(g2, ox, oy, (int) px, (int) py);
        } else if (modelo.t < 1.0) {
            double tAnim = porc + (modelo.t * (1.0 - porc));
            px = ox + tAnim * xR; py = oy - tAnim * yR;
            dibujarResorte(g2, ox, oy, (int) px, (int) py);
        } else {
            dibujarResorte(g2, ox, oy, ox + xR, oy - yR);
            px = ox + modelo.currX * escala; py = oy - modelo.currY * escala;
        }

        AffineTransform old = g2.getTransform();
        g2.translate(px, py);
        g2.rotate(modelo.rotAngle);
        dibujarFormaConMaterial(g2, 0, 0);
        g2.setTransform(old);

        // HUD (estático, sin cámara)
        g2.translate(-camX, -camY);
        dibujarHUD(g2);
    }

    private void dibujarHUD(Graphics2D g2) {
        int altoHud = hudExpandido ? 525 : 45;
        
        // Fondo y Borde
        g2.setColor(new Color(20, 30, 48, 225));
        g2.fillRoundRect(HUD_X, HUD_Y, HUD_WIDTH, altoHud, 20, 20);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(HUD_X, HUD_Y, HUD_WIDTH, altoHud, 20, 20);

        // Título interactivo
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(hudExpandido ? new Color(52, 152, 219) : Color.WHITE);
        g2.drawString(hudExpandido ? "📊 PANEL DE CONTROL (Clic para encoger)" : "📊 MOSTRAR DETALLES", HUD_X + 15, HUD_Y + 28);
        
        if (!hudExpandido) return;

        // --- Contenido Detallado ---
        double vActual = modelo.vTotal();
        int s = 22, y = HUD_Y + 65;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("V. INICIAL : %6.2f m/s", modelo.vInicial), 30, y); y += s;
        g2.drawString(String.format("V. ACTUAL  : %6.2f m/s", vActual), 30, y); y += s;
        g2.drawString(String.format("ACELERACIÓN: %6.2f m/s²", modelo.aceAct), 30, y); y += s;
        g2.setColor(new Color(46, 204, 113));
        g2.drawString(String.format("V. ANGULAR : %6.2f rad/s", modelo.vAngular), 30, y);
        
        y += 40;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(241, 196, 15));
        g2.drawString("📐 MÉTRICAS DINÁMICAS", 30, y); y += 25;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(String.format("Ang. Rampa       : %6.2f°", modelo.anguloRampa), 30, y); y += s;
        g2.drawString(String.format("H-Máx (Arco 1)   : %6.2f m", yMaxParabola), 30, y); y += s;
        g2.drawString(String.format("X-Impacto        : %6.2f m", xMaxParabola), 30, y); y += s;
        g2.drawString(String.format("Total Rebotes    : %d", modelo.numRebotes), 30, y);
        
        // --- SECCIÓN DE ENERGÍA ---
        y += 40;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("⚡ MONITOR DE ENERGÍA", 30, y);
        y += 20;

        double eCinetica = 0.5 * modelo.m * Math.pow(vActual, 2);
        double ePotencial = modelo.m * modelo.g * modelo.currY;
        double eTotalActual = eCinetica + ePotencial;
        double eMax = 0.5 * modelo.m * Math.pow(modelo.v1, 2) + (modelo.m * modelo.g * modelo.h);

        dibujarBarraEnergia(g2, "CINÉTICA", eCinetica, eMax, new Color(52, 152, 219), 30, y);
        y += 35;
        dibujarBarraEnergia(g2, "POTENCIAL", ePotencial, eMax, new Color(46, 204, 113), 30, y);
        y += 35;
        dibujarBarraEnergia(g2, "MECÁNICA", eTotalActual, eMax, new Color(230, 126, 34), 30, y);

        y += 50;
        g2.setColor(new Color(231, 76, 60, 40));
        g2.fillRoundRect(25, y - 15, 280, 25, 10, 10);
        g2.setColor(new Color(255, 80, 80));
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString(String.format("PRECISIÓN ERR    : %6.2f %%", errorPrecision), 35, y);
    }

    private void dibujarBarraEnergia(Graphics2D g2, String label, double valor, double max, Color color, int x, int y) {
        int anchoMax = 250; int altoBarra = 10;
        double porcentaje = (max > 0) ? Math.min(valor / max, 1.0) : 0;
        porcentaje = Math.max(porcentaje, 0);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString(String.format("%s: %.2f J", label, valor), x, y);
        g2.setColor(new Color(40, 40, 40));
        g2.fillRoundRect(x, y + 5, anchoMax, altoBarra, 5, 5);
        g2.setColor(color);
        g2.fillRoundRect(x, y + 5, (int)(anchoMax * porcentaje), altoBarra, 5, 5);
        g2.setColor(new Color(255, 255, 255, 50));
        g2.drawRoundRect(x, y + 5, anchoMax, altoBarra, 5, 5);
    }

    private void dibujarCotas(Graphics2D g2) {
        if (xMaxParabola == 0) return;
        int sx = ox + (int) (xMaxParabola * escala), sy = oy - (int) (yMaxParabola * escala);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        g2.setColor(new Color(231, 76, 60));
        g2.drawLine(sx, oy, sx, sy);
        g2.drawString(String.format("hMax: %.2f m", yMaxParabola), sx + 5, sy + 20);
        g2.drawLine(ox + (int) (modelo.L * escala), oy - 20, sx, oy - 20);
        g2.drawString(String.format("xMax: %.2f m", xMaxParabola), sx - 80, oy - 25);
    }

    private void dibujarParabolaIdeal(Graphics2D g2) {
        if (modelo.vInicial <= 0) return;
        g2.setColor(new Color(52, 152, 219, 130));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
        Path2D.Double pathIdeal = new Path2D.Double();
        double vx0 = modelo.vInicial * Math.cos(modelo.theta);
        double vy0 = modelo.vInicial * Math.sin(modelo.theta);
        boolean iniciado = false;
        for (double ti = 0; ti < 5.0; ti += 0.05) {
            double xi = modelo.L + (vx0 * ti);
            double yi = modelo.h + (vy0 * ti) - (0.5 * modelo.g * ti * ti);
            if (yi < -0.1 || xi > limiteX) break;
            if (!iniciado) { pathIdeal.moveTo(ox + xi * escala, oy - yi * escala); iniciado = true; }
            else { pathIdeal.lineTo(ox + xi * escala, oy - yi * escala); }
        }
        g2.draw(pathIdeal);
    }

    private void dibujarTrayectoriaReal(Graphics2D g2) {
        if (modelo.historial.size() < 2) return;
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(231, 76, 60, 200));
        Path2D.Double realPath = new Path2D.Double();
        boolean started = false;
        for (double[] d : modelo.historial) {
            if (d[0] >= 1.0) {
                double x = ox + d[1] * escala; double y = oy - d[2] * escala;
                if (!started) { realPath.moveTo(x, y); started = true; }
                else { realPath.lineTo(x, y); }
            }
        }
        g2.draw(realPath);
    }

    private void dibujarResorte(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(127, 140, 141)); g2.setStroke(new BasicStroke(2));
        int espiras = 12;
        for (int i = 0; i < espiras; i++) {
            int xa = x1 + (x2 - x1) * i / espiras; int ya = y1 + (y2 - y1) * i / espiras;
            int xb = x1 + (x2 - x1) * (i + 1) / espiras; int yb = y1 + (y2 - y1) * (i + 1) / espiras;
            int offset = (i % 2 == 0 ? 8 : -8);
            g2.drawLine(xa + offset, ya, xb - offset, yb);
        }
    }

    private void dibujarCruzMaxima(Graphics2D g2) {
        int centerX = ox + (int) (xMaxParabola * escala), centerY = oy - (int) (yMaxParabola * escala);
        g2.setStroke(new BasicStroke(2)); g2.setColor(Color.RED);
        g2.drawLine(centerX - 10, centerY, centerX + 10, centerY);
        g2.drawLine(centerX, centerY - 10, centerX, centerY + 10);
    }

    private void dibujarFormaConMaterial(Graphics2D g2, int x, int y) {
        g2.setColor(switch (materialActual) {
            case "Acero" -> new Color(149, 165, 166);
            case "Madera" -> new Color(139, 69, 19);
            case "Plástico" -> new Color(52, 152, 219);
            case "Hielo" -> new Color(209, 242, 235, 180);
            default -> new Color(231, 76, 60);
        });
        int size = (int) (26 * (escala / 60.0));
        if (formaActual.equals("Esfera")) {
            g2.fillOval(x - size / 2, y - size / 2, size, size);
            g2.setColor(new Color(255, 255, 255, 150)); g2.drawLine(x, y, x + size / 2, y);
        } else if (formaActual.equals("Cubo")) {
            g2.fillRect(x - size / 2, y - size / 2, size, size);
            g2.setColor(Color.BLACK); g2.drawRect(x - size / 2, y - size / 2, size, size);
        } else {
            g2.fillPolygon(new int[]{x, x + size / 2, x, x - size / 2}, new int[]{y - size / 2, y, y + size / 2, y}, 4);
        }
    }
}