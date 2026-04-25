package com.pos.graficafisica;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class PantallaCarga extends JWindow {
    private double bolaY = 100;
    private double velY = 0;
    private final double gravedad = 0.6;
    private final double rebote = -0.85;
    private final int suelo = 250;
    private final int radio = 25;
    
    // Variables para la barra de carga
    private float progreso = 0; // De 0.0 a 1.0

    public PantallaCarga() {
        setSize(450, 450);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        // Timer de animación (60 FPS)
        Timer timer = new Timer(16, e -> {
            // Animación de la bola
            velY += gravedad;
            bolaY += velY;
            if (bolaY + (radio * 2) > suelo) {
                bolaY = suelo - (radio * 2);
                velY *= rebote;
            }

            // Incrementar progreso suavemente
            if (progreso < 1.0f) {
                progreso += 0.005f; // Ajusta este valor para que cargue más rápido o lento
            }

            repaint();
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Limpiar para transparencia
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setComposite(AlphaComposite.SrcOver);

        // 1. Dibujar Sombra
        double distanciaAlSuelo = suelo - (bolaY + (radio * 2));
        float opacidadSombra = (float) Math.max(0, 0.3 - (distanciaAlSuelo / 200));
        int anchoSombra = (int) (radio * 2 - (distanciaAlSuelo / 5));
        g2.setColor(new Color(0, 0, 0, opacidadSombra));
        g2.fillOval(getWidth()/2 - anchoSombra/2, suelo - 5, anchoSombra, 8);

        // 2. Dibujar Esfera
        GradientPaint gpBola = new GradientPaint(
            getWidth()/2 - 10, (int)bolaY, new Color(52, 152, 219),
            getWidth()/2 + radio, (int)bolaY + (radio * 2), new Color(44, 62, 80)
        );
        g2.setPaint(gpBola);
        g2.fill(new Ellipse2D.Double(getWidth()/2 - radio, bolaY, radio * 2, radio * 2));

        // 3. Dibujar Texto
        g2.setColor(new Color(44, 62, 80));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String txt = "PREPARANDO ENTORNO FÍSICO...";
        int txtWidth = g2.getFontMetrics().stringWidth(txt);
        g2.drawString(txt, (getWidth() - txtWidth) / 2, suelo + 40);

        // 4. Dibujar Barra de Carga
        int barraAncho = 300;
        int barraAlto = 8;
        int barraX = (getWidth() - barraAncho) / 2;
        int barraY = suelo + 60;

        // Fondo de la barra (gris claro)
        g2.setColor(new Color(200, 200, 200, 100));
        g2.fill(new RoundRectangle2D.Double(barraX, barraY, barraAncho, barraAlto, 10, 10));

        // Progreso de la barra (azul brillante)
        if (progreso > 0) {
            GradientPaint gpBarra = new GradientPaint(
                barraX, barraY, new Color(52, 152, 219),
                barraX + (barraAncho * progreso), barraY, new Color(46, 204, 113)
            );
            g2.setPaint(gpBarra);
            g2.fill(new RoundRectangle2D.Double(barraX, barraY, barraAncho * progreso, barraAlto, 10, 10));
        }
        
        // Mostrar porcentaje
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.setColor(new Color(127, 140, 141));
        String porcTxt = (int)(progreso * 100) + "%";
        g2.drawString(porcTxt, barraX + barraAncho + 10, barraY + 8);
    }
}