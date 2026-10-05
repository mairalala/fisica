package com.pos.graficafisica.view;

import com.pos.graficafisica.presenter.GraficaParabolas;
import com.pos.graficafisica.model.SimulacionParabola;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;

public class LaboratorioParabolas extends JFrame {

    private PanelParabola panelSimulacion;
    private SimulacionParabola modelo;
    private GraficaParabolas panelGraficas;

    private JTextField txtH, txtL, txtMasa, txtK, txtX0, txtG;
    private JComboBox<String> cMat;

    public LaboratorioParabolas() {
        setTitle("Laboratorio Física - TEINCO 2026");
        setSize(1550, 950);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modelo = new SimulacionParabola();
        panelGraficas = new GraficaParabolas((ArrayList<double[]>) modelo.historial);
        panelSimulacion = new PanelParabola(modelo, panelGraficas.getgVelocidad(),
                panelGraficas.getgAltura(), panelGraficas.getgDistancia(), panelGraficas.getgAceleracion());

        JPanel contenedorCentro = new JPanel(new BorderLayout());
        contenedorCentro.add(panelSimulacion, BorderLayout.CENTER);
        contenedorCentro.add(panelGraficas, BorderLayout.SOUTH);

        add(contenedorCentro, BorderLayout.CENTER);
        configurarPanelLateral();
        configurarBarraControl();
    }

    private void configurarPanelLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(new Color(23, 32, 42));
        lateral.setPreferredSize(new Dimension(300, 0));
        lateral.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 2, new Color(52, 152, 219)), new EmptyBorder(20, 20, 20, 20)));

        JLabel titulo = new JLabel("CONFIGURACIÓN");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(52, 152, 219));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lateral.add(titulo);
        lateral.add(Box.createVerticalStrut(25));

        txtH = createInputField(lateral, "Altura Rampa (m):", "2.5");
        txtL = createInputField(lateral, "Base Rampa (m):", "4.0");
        txtMasa = createInputField(lateral, "Masa Objeto (kg):", "0.25");
        txtK = createInputField(lateral, "Constante K (N/m):", "100.0");
        txtX0 = createInputField(lateral, "Compresión (m):", "1.2");
        txtG = createInputField(lateral, "Gravedad (m/s²):", "9.8");

        lateral.add(new JLabel("Material:") {
            { setForeground(Color.LIGHT_GRAY); }
        });
        cMat = new JComboBox<>(new String[]{"Goma", "Madera", "Acero", "Hielo"});
        cMat.setMaximumSize(new Dimension(260, 35));
        lateral.add(cMat);
        lateral.add(Box.createVerticalStrut(20));

        // BOTÓN: ACTUALIZAR ESCENARIO
        JButton btnAplicar = crearBotonEstilizado("ACTUALIZAR ESCENARIO", new Color(41, 128, 185));
        btnAplicar.addActionListener(e -> aplicarCambios());
        lateral.add(btnAplicar);
        lateral.add(Box.createVerticalStrut(10));

        // NUEVO BOTÓN: ENTORNO IDEAL
        JButton btnIdeal = crearBotonEstilizado("ENTORNO IDEAL", new Color(22, 160, 133));
        btnIdeal.addActionListener(e -> {
            txtH.setText("2.0");
            txtL.setText("3.0");
            txtMasa.setText("1.0");
            txtK.setText("200.0");
            txtX0.setText("0.5");
            txtG.setText("9.81");
            cMat.setSelectedItem("Hielo"); // El hielo representa menor fricción/escenario ideal
            aplicarCambios();
            JOptionPane.showMessageDialog(this, "Escenario ideal establecido (Fricción mínima).");
        });
        lateral.add(btnIdeal);
        lateral.add(Box.createVerticalStrut(15));

        // BOTÓN: VER TABLA DE DATOS
        JButton btnTabla = crearBotonEstilizado("VER TABLA DE DATOS", new Color(52, 73, 94));
        btnTabla.addActionListener(e -> {
            VentanaTablaResultadosParabolas vt = new VentanaTablaResultadosParabolas(modelo);
            vt.setVisible(true);
        });
        lateral.add(btnTabla);
        lateral.add(Box.createVerticalStrut(15));

        // BOTÓN: HISTORIAL BASE DATOS
        JButton btnHistorial = crearBotonEstilizado("HISTORIAL BASE DATOS", new Color(34, 153, 84));
        btnHistorial.addActionListener(e -> {
            VentanaHistorialParabolas vh = new VentanaHistorialParabolas();
            vh.setVisible(true);
        });
        lateral.add(btnHistorial);

        add(lateral, BorderLayout.WEST);
    }

    // Método auxiliar para evitar repetir la lógica de actualización
    private void aplicarCambios() {
        try {
            modelo.h = Double.parseDouble(txtH.getText());
            modelo.L = Double.parseDouble(txtL.getText());
            modelo.m = Double.parseDouble(txtMasa.getText());
            modelo.k = Double.parseDouble(txtK.getText());
            modelo.x0 = Double.parseDouble(txtX0.getText());
            modelo.g = Double.parseDouble(txtG.getText());
            String mat = (String) cMat.getSelectedItem();
            modelo.configurarMaterial(mat);
            modelo.recalcular();
            panelSimulacion.setFormaYMaterial("Esfera", mat);
            panelSimulacion.reset();
            panelGraficas.refrescar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos");
        }
    }

    private void configurarBarraControl() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        barra.setBackground(new Color(30, 30, 35));
        barra.setBorder(new MatteBorder(2, 0, 0, 0, new Color(52, 152, 219)));

        JButton btnPlay = crearBotonEstilizado("INICIAR SIMULACIÓN", new Color(39, 174, 96));
        btnPlay.addActionListener(e -> panelSimulacion.play());

        JButton btnReset = crearBotonEstilizado("REINICIAR", new Color(211, 84, 0));
        btnReset.addActionListener(e -> {
            panelSimulacion.reset();
            panelGraficas.refrescar();
        });

        JButton btnMenu = crearBotonEstilizado("VOLVER AL MENÚ", new Color(142, 68, 173));
        btnMenu.addActionListener(e -> {
            new MenuPrincipal().setVisible(true);
            this.dispose();
        });

        barra.add(btnPlay);
        barra.add(btnReset);
        barra.add(btnMenu);
        add(barra, BorderLayout.SOUTH);
    }

    private JTextField createInputField(JPanel p, String lbl, String def) {
        JLabel l = new JLabel(lbl);
        l.setForeground(Color.LIGHT_GRAY);
        p.add(l);
        JTextField t = new JTextField(def);
        t.setMaximumSize(new Dimension(260, 30));
        t.setBackground(new Color(44, 62, 80));
        t.setForeground(Color.WHITE);
        t.setCaretColor(Color.WHITE);
        t.setBorder(new LineBorder(new Color(52, 73, 94), 1));
        p.add(t);
        p.add(Box.createVerticalStrut(12));
        return t;
    }

    private JButton crearBotonEstilizado(String texto, Color colorBase) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, colorBase, 0, getHeight(), colorBase.darker()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setMaximumSize(new Dimension(260, 40));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}