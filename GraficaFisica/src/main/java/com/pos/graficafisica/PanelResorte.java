package com.pos.graficafisica;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

public class PanelResorte extends JPanel {
    private SimulacionResorte modelo;
    private double escalaPx = 100.0;
    private Timer timer;
    private double maxVelocidad = 0;
    private double maxAceleracion = 0;
    
    private final double L_NATURAL_M = 1.2; 

    public PanelResorte(SimulacionResorte modelo) {
        this.modelo = modelo;
        setBackground(new Color(23, 32, 42));
        
        timer = new Timer(16, e -> {
            modelo.actualizarEstadoRK4(); 
            
            if (Math.abs(modelo.velocidad) > maxVelocidad) maxVelocidad = Math.abs(modelo.velocidad);
            if (Math.abs(modelo.aceleracion) > maxAceleracion) maxAceleracion = Math.abs(modelo.aceleracion);
            
            // --- NUEVA LÓGICA DE AUTO-DETECCIÓN DE ESTABILIDAD ---
            // Si la velocidad y aceleración son casi cero, y ha pasado un tiempo mínimo de simulación
            boolean estaEstable = Math.abs(modelo.velocidad) < 0.001 && Math.abs(modelo.aceleracion) < 0.001;
            
            if (estaEstable && modelo.tiempo > 1.0) { 
                detener(); // Detiene el timer del resorte
                System.out.println("Simulación estabilizada a los " + modelo.tiempo + " segundos.");
            }
            
            repaint();
        });
    }

    public void iniciarAnimacion() {
        maxVelocidad = 0;
        maxAceleracion = 0;
        timer.start();
    }

    public void detener() {
        timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int techoY = 60;
        int longitudBasePx = (int)(L_NATURAL_M * escalaPx);
        int elongacionPx = (int) (modelo.elongacion * escalaPx);
        int finalY = techoY + longitudBasePx + elongacionPx;
        int xRegla = getWidth() - 100;

        dibujarReglaIndustrial(g2, xRegla, techoY);

        // Marca Roja: Longitud Natural (Equilibrio)
        int yEstadoNatural = techoY + longitudBasePx;
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(231, 76, 60)); 
        g2.drawLine(xRegla - 40, yEstadoNatural, xRegla + 10, yEstadoNatural);
        g2.drawString("L. NATURAL", xRegla - 100, yEstadoNatural + 4);

        // Marca Amarilla: Posición Actual
        g2.setColor(new Color(241, 196, 15));
        g2.drawLine(xRegla - 60, finalY, xRegla + 10, finalY);
        g2.drawString(String.format("%.1f cm", modelo.elongacion * 100), xRegla - 115, finalY + 5);

        // Dibujo del Resorte
        float grosorK = (float) Math.min(8.0, Math.max(2.5, modelo.constanteK / 12.0));
        g2.setStroke(new BasicStroke(grosorK));
        GradientPaint gp = new GradientPaint(centerX - 20, 0, new Color(46, 204, 113), centerX + 20, 0, new Color(20, 90, 50), true);
        g2.setPaint(gp);
        dibujarCuerpoResorte(g2, centerX, techoY, longitudBasePx, elongacionPx);

        // Dibujo de la Masa
        int diametroPx = (int)(modelo.radioEsfera * 1000); 
        g2.setColor(new Color(231, 76, 60));
        g2.fillOval(centerX - (diametroPx/2), finalY, diametroPx, diametroPx);
        
        g2.setColor(Color.WHITE);
        g2.drawString(modelo.masa + " kg", centerX - 15, finalY + (diametroPx/2) + 15);

        dibujarHUD(g2);
    }

    private void dibujarHUD(Graphics2D g2) {
        int x = 20, y = 20;
        g2.setColor(new Color(21, 27, 33, 230));
        g2.fillRoundRect(x, y, 320, 260, 15, 15);
        g2.setColor(new Color(93, 173, 226));
        g2.drawRoundRect(x, y, 320, 260, 15, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString("TELEMETRÍA RK4", x + 15, y + 25);
        
        g2.setColor(new Color(200, 200, 200));
        double lFinal = L_NATURAL_M + modelo.elongacion;
        g2.drawString(String.format("L. Natural (L0): %.2f m", L_NATURAL_M), x + 15, y + 50);
        g2.drawString(String.format("L. Final   (Lf): %.2f m", lFinal), x + 15, y + 70);
        g2.drawLine(x + 15, y + 80, x + 300, y + 80);

        g2.setColor(Color.WHITE);
        g2.drawString(String.format("Velocidad:   %.3f m/s", modelo.velocidad), x + 15, y + 100);
        g2.drawString(String.format("V. Máxima:   %.3f m/s", maxVelocidad), x + 15, y + 120);
        
        g2.drawString(String.format("Aceleración: %.3f m/s²", modelo.aceleracion), x + 15, y + 145);
        g2.drawString(String.format("A. Máxima:   %.3f m/s²", maxAceleracion), x + 15, y + 165);
        
        boolean estaEstable = Math.abs(modelo.velocidad) < 0.001 && Math.abs(modelo.aceleracion) < 0.001;
        g2.setColor(estaEstable ? new Color(46, 204, 113) : new Color(243, 156, 18));
        
        // --- INDICADOR VISUAL DE QUE EL TIEMPO SE DETUVO ---
        String estadoTexto = estaEstable ? "SISTEMA ESTABLE (FINALIZADO)" : "EN OSCILACIÓN";
        g2.drawString("ESTADO: " + estadoTexto, x + 15, y + 195);

        g2.setColor(Color.CYAN);
        g2.drawString("Flujo: " + modelo.obtenerTipoFlujo(), x + 15, y + 220);
        
        g2.setColor(new Color(241, 196, 15));
        g2.drawString(String.format("Tiempo: %.2f s", modelo.tiempo), x + 15, y + 245);
    }

    private void dibujarCuerpoResorte(Graphics2D g2, int cX, int tY, int lB, int ePx) {
        int espiras = 30;
        double xAnt = cX, yAnt = tY;
        for (int i = 1; i <= espiras; i++) {
            double ySig = tY + (i * (lB + ePx) / (double) espiras);
            double xSig = cX + (i % 2 == 0 ? 25 : -25);
            if (i == espiras) xSig = cX;
            g2.draw(new Line2D.Double(xAnt, yAnt, xSig, ySig));
            xAnt = xSig; yAnt = ySig;
        }
    }

    private void dibujarReglaIndustrial(Graphics2D g2, int x, int inicioY) {
        g2.setColor(new Color(127, 140, 141));
        g2.drawLine(x, inicioY, x, getHeight() - 50);
        for (int i = 0; i <= 600; i += 50) {
            int yPos = inicioY + i;
            g2.drawLine(x, yPos, x - 20, yPos);
            g2.drawString(i/10 + "cm", x + 10, yPos + 5);
        }
    }
}