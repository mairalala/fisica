package com.pos.graficafisica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        setTitle("Laboratorio Virtual de Física - TEINCO");
        setSize(800, 500);
        
        // MODIFICACIÓN: EXIT_ON_CLOSE asegura que se cierre todo el proceso de Java
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(33, 47, 61));
        setLayout(new BorderLayout());

        JLabel lblTitulo = new JLabel("SELECCIONE UN LABORATORIO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(52, 152, 219));
        lblTitulo.setBorder(new EmptyBorder(30, 0, 30, 0));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 20, 0));
        panelBotones.setBackground(new Color(33, 47, 61));
        panelBotones.setBorder(new EmptyBorder(20, 50, 50, 50));

        // CORRECCIÓN: Ahora llama a LaboratorioParabolas en lugar de GraficaParabolas
        panelBotones.add(crearBotonModulo("DINÁMICA Y GEOMETRÍA", "Rampa, Fricción y Lanzamiento", e -> {
            new LaboratorioParabolas().setVisible(true);
            this.dispose();
        }));

        panelBotones.add(crearBotonModulo("LEY DE HOOKE", "Constante elástica del Resorte", e -> {
            new LaboratorioResorte().setVisible(true);
            this.dispose();
        }));

        add(panelBotones, BorderLayout.CENTER);
    }

    private JButton crearBotonModulo(String titulo, String subtitulo, java.awt.event.ActionListener accion) {
        JButton btn = new JButton("<html><center><font size='5'><b>" + titulo + "</b></font><br><font size='3'>" + subtitulo + "</font></center></html>");
        btn.setBackground(new Color(44, 62, 80));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(accion);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 94));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(44, 62, 80));
            }
        });

        return btn;
    }
}