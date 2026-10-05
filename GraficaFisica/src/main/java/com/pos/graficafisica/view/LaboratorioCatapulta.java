package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionCatapulta;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LaboratorioCatapulta extends JFrame {

    private PanelCatapulta panelSimulacion;
    private SimulacionCatapulta modelo;
    private GraficaCatapulta panelGraficas;

    private JTextField txtMasa, txtAngulo, txtDeformacion, txtGravedad;
    private JComboBox<String> cMatProyectil, cMatCatapulta;

    public LaboratorioCatapulta() {
        setTitle("Laboratorio de Catapultas - TEINCO 2026");
        setSize(1550, 950);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modelo = new SimulacionCatapulta();
        panelGraficas = new GraficaCatapulta(modelo.getHistorialDatos());
        panelSimulacion = new PanelCatapulta(modelo, panelGraficas);

        JPanel contenedorSur = new JPanel(new BorderLayout());
        contenedorSur.add(panelGraficas, BorderLayout.CENTER);
        contenedorSur.add(configurarBarraControl(), BorderLayout.SOUTH);

        add(panelSimulacion, BorderLayout.CENTER);
        add(contenedorSur, BorderLayout.SOUTH);

        configurarPanelLateral();
    }

    private void configurarPanelLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(new Color(23, 32, 42));
        lateral.setPreferredSize(new Dimension(300, 0));
        lateral.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 2, new Color(192, 57, 43)), new EmptyBorder(20, 20, 20, 20)));

        JLabel titulo = new JLabel("CONFIG. CATAPULTA");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(192, 57, 43));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lateral.add(titulo);
        lateral.add(Box.createVerticalStrut(25));

        txtMasa = createInputField(lateral, "Masa Proyectil (kg):", "1.0");
        txtAngulo = createInputField(lateral, "Ángulo Lanzamiento (°):", "45.0");
        txtDeformacion = createInputField(lateral, "Deformación Caucho (m):", "0.5");
        txtGravedad = createInputField(lateral, "Gravedad (m/s²):", "9.81");

        lateral.add(new JLabel("Material Proyectil:") { { setForeground(Color.LIGHT_GRAY); } });
        cMatProyectil = new JComboBox<>(new String[]{"Piedra", "Madera", "Metal"});
        cMatProyectil.setMaximumSize(new Dimension(260, 35));
        lateral.add(cMatProyectil);

        lateral.add(Box.createVerticalStrut(15));
        lateral.add(new JLabel("Material Estructura (Resistencia):") { { setForeground(Color.LIGHT_GRAY); } });
        cMatCatapulta = new JComboBox<>(new String[]{"Cartón", "Madera", "Hierro"});
        cMatCatapulta.setMaximumSize(new Dimension(260, 35));
        lateral.add(cMatCatapulta);
        lateral.add(Box.createVerticalStrut(20));

        JButton btnAplicar = crearBotonEstilizado("APLICAR CONFIGURACIÓN", new Color(192, 57, 43));
        btnAplicar.addActionListener(e -> aplicarCambios());
        lateral.add(btnAplicar);

        add(lateral, BorderLayout.WEST);
    }

    private void aplicarCambios() {
        try {
            modelo.masaProyectil = Double.parseDouble(txtMasa.getText());
            modelo.anguloLanzamiento = Double.parseDouble(txtAngulo.getText());
            modelo.deformacion = Double.parseDouble(txtDeformacion.getText());
            modelo.gravedad = Double.parseDouble(txtGravedad.getText());

            String material = (String) cMatCatapulta.getSelectedItem();
            switch (material) {
                case "Cartón": modelo.resistenciaEstructura = 200.0; break;
                case "Madera": modelo.resistenciaEstructura = 500.0; break;
                case "Hierro": modelo.resistenciaEstructura = 1200.0; break;
            }
            modelo.reset();
            panelSimulacion.reset();
            panelGraficas.refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados.");
        }
    }

    private JPanel configurarBarraControl() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        barra.setBackground(new Color(30, 30, 35));

        JButton btnPlay = crearBotonEstilizado("LANZAR", new Color(39, 174, 96));
        btnPlay.addActionListener(e -> panelSimulacion.iniciarLanzamiento());

        // NUEVO: Botón para abrir el historial de pruebas desde SQLite
        JButton btnHistorial = crearBotonEstilizado("VER HISTORIAL", new Color(41, 128, 185));
        btnHistorial.addActionListener(e -> new VentanaHistorialCatapulta().setVisible(true));

        JButton btnMenu = crearBotonEstilizado("VOLVER AL MENÚ", new Color(142, 68, 173));
        btnMenu.addActionListener(e -> {
            new MenuPrincipal().setVisible(true);
            this.dispose();
        });

        barra.add(btnPlay);
        barra.add(btnHistorial);
        barra.add(btnMenu);
        return barra;
    }

    private JTextField createInputField(JPanel p, String lbl, String def) {
        JLabel l = new JLabel(lbl);
        l.setForeground(Color.LIGHT_GRAY);
        p.add(l);
        JTextField t = new JTextField(def);
        t.setMaximumSize(new Dimension(260, 30));
        t.setBackground(new Color(44, 62, 80));
        t.setForeground(Color.WHITE);
        p.add(t);
        p.add(Box.createVerticalStrut(12));
        return t;
    }

    private JButton crearBotonEstilizado(String texto, Color colorBase) {
        JButton btn = new JButton(texto);
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 40));
        return btn;
    }
}