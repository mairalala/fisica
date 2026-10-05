package com.pos.graficafisica.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        setTitle("Laboratorio Virtual de Física - TEINCO");
        setSize(880, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con un tono oscuro moderno y elegante
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(24, 30, 42));
        setContentPane(panelPrincipal);

        // Panel superior para la cabecera y el título
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BoxLayout(panelSuperior, BoxLayout.Y_AXIS));
        panelSuperior.setOpaque(false);
        panelSuperior.setBorder(new EmptyBorder(40, 20, 25, 20));

        JLabel lblSubtitulo = new JLabel("CORPORACIÓN TECNOLÓGICA INDUSTRIAL COLOMBIANA", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSubtitulo.setForeground(new Color(97, 175, 239));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("SELECCIONE UN LABORATORIO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setBorder(new EmptyBorder(10, 0, 0, 0));

        panelSuperior.add(lblSubtitulo);
        panelSuperior.add(lblTitulo);
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // Panel central de botones con rejilla espaciada
        JPanel panelBotones = new JPanel(new GridLayout(1, 3, 22, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(10, 45, 55, 45));

        panelBotones.add(crearBotonModulo("DINÁMICA Y GEOMETRÍA", "Rampa, Fricción y Lanzamiento", e -> {
            new LaboratorioParabolas().setVisible(true);
            this.dispose();
        }));

        panelBotones.add(crearBotonModulo("LEY DE HOOKE", "Constante elástica del Resorte", e -> {
            new LaboratorioResorte().setVisible(true);
            this.dispose();
        }));

        panelBotones.add(crearBotonModulo("CATAPULTAS", "Simulación y Resistencia", e -> {
            new LaboratorioCatapulta().setVisible(true);
            this.dispose();
        }));

        panelPrincipal.add(panelBotones, BorderLayout.CENTER);
    }

    private JButton crearBotonModulo(String titulo, String subtitulo, java.awt.event.ActionListener accion) {
        // Estructura HTML interna optimizada para centrar y separar título y subtítulo
        String textoBoton = "<html><div style='text-align: center; padding: 5px;'>"
                + "<span style='font-size: 13px; font-weight: bold; color: #FFFFFF;'>" + titulo + "</span>"
                + "<br/><br/>"
                + "<span style='font-size: 10px; color: #A0ACC0;'>" + subtitulo + "</span>"
                + "</div></html>";

        JButton btn = new JButton(textoBoton);
        btn.setBackground(new Color(33, 43, 58));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);

        // Propiedad de FlatLaf para esquinas redondeadas
        btn.putClientProperty("JButton.buttonType", "roundRect");

        // Borde inicial sutil
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 71, 98), 1, true),
                new EmptyBorder(15, 10, 15, 10)
        ));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(accion);

        // Animación visual de hover (cambio de color y borde al pasar el cursor)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(45, 62, 86));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(97, 175, 239), 2, true),
                        new EmptyBorder(14, 9, 14, 9)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(33, 43, 58));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(55, 71, 98), 1, true),
                        new EmptyBorder(15, 10, 15, 10)
                ));
            }
        });

        return btn;
    }
}