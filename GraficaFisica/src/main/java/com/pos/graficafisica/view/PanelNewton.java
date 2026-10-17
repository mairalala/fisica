package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionNewton;

import javax.swing.*;
import java.awt.*;

public class PanelNewton extends JPanel {
    private static final Color FONDO = new Color(23, 32, 42);
    private static final Color ACENTO = new Color(93, 173, 226);
    private static final Color FUERZA = new Color(241, 196, 15);
    private static final Color VERDE = new Color(46, 204, 113);
    private static final Color AZUL = new Color(52, 152, 219);
    private static final Color ROJO = new Color(231, 76, 60);

    private final SimulacionNewton modelo;

    public PanelNewton(SimulacionNewton modelo) {
        this.modelo = modelo;
        setBackground(FONDO);
        setPreferredSize(new Dimension(700, 360));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        int centroX = ancho / 2;
        int sueloY = alto - 75;

        // 1. Línea del Suelo
        g2.setColor(new Color(127, 140, 141));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(20, sueloY + 20, ancho - 20, sueloY + 20);

        // 2. Título de la Ley
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g2.setColor(Color.WHITE);
        String titulo = switch (modelo.ley) {
            case PRIMERA -> "PRIMERA LEY · INERCIA";
            case SEGUNDA -> "SEGUNDA LEY · F = m · a";
            case TERCERA -> "TERCERA LEY · ACCIÓN Y REACCIÓN";
        };
        g2.drawString(titulo, 25, 30);

        // 3. Panel HUD (Caja semi-transparente de datos)
        int hudX = 25;
        int hudY = 42;
        int hudAncho = Math.min(ancho - 50, 620);
        int hudAlto = (modelo.ley == SimulacionNewton.Ley.TERCERA) ? 72 : 50;

        g2.setColor(new Color(15, 22, 30, 220));
        g2.fillRoundRect(hudX, hudY, hudAncho, hudAlto, 10, 10);
        g2.setColor(new Color(52, 73, 94));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(hudX, hudY, hudAncho, hudAlto, 10, 10);

        // 4. Renderizado del HUD según la Ley activa
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));

        if (modelo.ley == SimulacionNewton.Ley.TERCERA) {
            String txtA = String.format("OBJETO A:  m = %5.2f kg  |  v = %6.2f m/s  |  a = %6.2f m/s²",
                    modelo.masa, modelo.velocidadA, modelo.aceleracionA);
            String txtB = String.format("OBJETO B:  m = %5.2f kg  |  v = %6.2f m/s  |  a = %6.2f m/s²",
                    modelo.masaB, modelo.velocidadB, modelo.aceleracionB);

            g2.setColor(VERDE);
            g2.drawString(txtA, hudX + 15, hudY + 26);
            g2.setColor(AZUL);
            g2.drawString(txtB, hudX + 15, hudY + 52);

            // Carritos y flechas de fuerza para Tercera Ley
            int escala = Math.max(60, Math.min(100, ancho / 8));
            int xA = centroX + (int) (modelo.posicionA * escala) - 42;
            int xB = centroX + (int) (modelo.posicionB * escala) - 42;

            dibujarCarrito(g2, xA, sueloY, modelo.masa, VERDE);
            dibujarCarrito(g2, xB, sueloY, modelo.masaB, AZUL);

            if (modelo.fuerzaAplicada) {
                dibujarFlecha(g2, xA + 4, sueloY - 16, xA - 50, sueloY - 16, FUERZA);
                dibujarFlecha(g2, xB + 84, sueloY - 16, xB + 134, sueloY - 16, ROJO);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(FUERZA);
                g2.drawString("F₁₂ = " + formato(modelo.fuerza) + " N", xA - 65, sueloY - 26);
                g2.setColor(ROJO);
                g2.drawString("F₂₁ = −" + formato(modelo.fuerza) + " N", xB + 10, sueloY - 26);
            }
        } else {
            String estadoFuerza = (modelo.ley == SimulacionNewton.Ley.PRIMERA && !modelo.fuerzaAplicada)
                    ? " (Fuerza Neta = 0 N)"
                    : String.format(" (Fuerza Neta = %.2f N)", modelo.fuerza);

            String txtEstado = String.format("Masa = %.2f kg  |  Velocidad = %.2f m/s  |  Aceleración = %.2f m/s²%s",
                    modelo.masa, modelo.velocidadA, modelo.aceleracionA, estadoFuerza);

            g2.setColor(ACENTO);
            g2.drawString(txtEstado, hudX + 15, hudY + 30);

            // Carrito y flecha de fuerza para Primera/Segunda Ley
            int x = centroX - 42 + (int) (modelo.posicionA * 12);
            dibujarCarrito(g2, x, sueloY, modelo.masa, VERDE);

            if ((modelo.ley == SimulacionNewton.Ley.PRIMERA && modelo.fuerzaAplicada)
                    || modelo.ley == SimulacionNewton.Ley.SEGUNDA) {
                dibujarFlecha(g2, x + 42, sueloY - 18, x + 120, sueloY - 18, FUERZA);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(FUERZA);
                g2.drawString("F = " + formato(modelo.fuerza) + " N", x + 50, sueloY - 28);
            }
        }

        // 5. HUD Inferior del Tiempo
        g2.setColor(new Color(15, 22, 30, 230));
        g2.fillRoundRect(20, alto - 35, 210, 26, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString(String.format("TIEMPO: %6.2f s", modelo.tiempo), 30, alto - 18);

        g2.dispose();
    }

    private void dibujarCarrito(Graphics2D g2, int x, int sueloY, double masa, Color color) {
        int y = sueloY - 38;
        g2.setColor(color);
        g2.fillRoundRect(x, y, 84, 38, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String etiqueta = formato(masa) + " kg";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(etiqueta, x + (84 - fm.stringWidth(etiqueta)) / 2, y + 23);

        // Ruedas
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(x + 12, y + 30, 16, 16);
        g2.fillOval(x + 56, y + 30, 16, 16);
    }

    private void dibujarFlecha(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3));
        g2.drawLine(x1, y1, x2, y2);
        int direccion = x2 >= x1 ? 1 : -1;
        g2.drawLine(x2, y2, x2 - 8 * direccion, y2 - 6);
        g2.drawLine(x2, y2, x2 - 8 * direccion, y2 + 6);
    }

    private String formato(double valor) {
        return String.format("%.2f", valor);
    }
}