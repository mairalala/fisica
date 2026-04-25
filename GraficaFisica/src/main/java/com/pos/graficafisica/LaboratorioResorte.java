package com.pos.graficafisica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LaboratorioResorte extends JFrame {
    private SimulacionResorte modeloResorte = new SimulacionResorte();
    private PanelResorte panelDibujo;
    private JTextField txtMasa, txtK, txtG, txtViscosidad, txtRadio;
    private JComboBox<String> comboFluidos;

    public LaboratorioResorte() {
        setTitle("LABORATORIO LEY DE HOOKE - INGENIERÍA DE PRECISIÓN");
        setSize(1350, 850);
        
        // CORRECCIÓN: La constante correcta es DO_NOTHING_ON_CLOSE
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panelDibujo != null) {
                    panelDibujo.detener();
                }
                dispose();
            }
        });

        setLocationRelativeTo(null);

        panelDibujo = new PanelResorte(modeloResorte);
        add(panelDibujo, BorderLayout.CENTER);

        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(new Color(28, 40, 51));
        lateral.setBorder(new EmptyBorder(25, 20, 25, 20));
        lateral.setPreferredSize(new Dimension(320, 0));

        JLabel lblTitulo = new JLabel("PARÁMETROS FÍSICOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(93, 173, 226));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lateral.add(lblTitulo);
        lateral.add(Box.createVerticalStrut(20));

        txtMasa = createInputField(lateral, "Masa (kg):", "0.5");
        txtK = createInputField(lateral, "Constante K (N/m):", "50.0");
        txtG = createInputField(lateral, "Gravedad (m/s²):", "9.81");
        
        JLabel lblFluido = new JLabel("Entorno / Fluido:");
        lblFluido.setForeground(Color.LIGHT_GRAY);
        lblFluido.setAlignmentX(Component.CENTER_ALIGNMENT);
        lateral.add(lblFluido);
        
        String[] fluidos = {"Personalizado", "Aire (Baja viscosidad)", "Agua", "Aceite", "Glicerina (Alta)"};
        comboFluidos = new JComboBox<>(fluidos);
        comboFluidos.setMaximumSize(new Dimension(280, 35));
        comboFluidos.addActionListener(e -> actualizarViscosidad());
        lateral.add(comboFluidos);
        lateral.add(Box.createVerticalStrut(10));

        txtViscosidad = createInputField(lateral, "Viscosidad η (Pa·s):", "0.15");
        txtRadio = createInputField(lateral, "Radio Esfera (m):", "0.05");

        lateral.add(Box.createVerticalGlue());

        JButton btnCalcular = crearBotonPrincipal("▶ EJECUTAR SIMULACIÓN", new Color(40, 180, 99));
        btnCalcular.addActionListener(e -> {
            try {
                cargarDatosAModelo();
                modeloResorte.reiniciarHistorial(); 
                ConexionDB.guardarSimulacionResorte(modeloResorte.masa, modeloResorte.constanteK, modeloResorte.elongacion);
                panelDibujo.iniciarAnimacion();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en los datos numéricos.");
            }
        });

        JButton btnAcelerar = crearBotonPrincipal("⏩ OBTENER EQUILIBRIO (Fast)", new Color(241, 196, 15));
        btnAcelerar.setForeground(Color.BLACK); 
        btnAcelerar.addActionListener(e -> {
            if (panelDibujo != null) {
                for (int i = 0; i < 10000; i++) {
                    modeloResorte.actualizarEstadoRK4();
                    
                    VentanaGraficas.registrarPuntoManual(new double[]{
                        modeloResorte.tiempo,
                        modeloResorte.elongacion,
                        modeloResorte.velocidad,
                        modeloResorte.aceleracion
                    });

                    if (Math.abs(modeloResorte.velocidad) < 1e-6 && Math.abs(modeloResorte.aceleracion) < 1e-6) break;
                }
                panelDibujo.repaint(); 
            }
        });

        JButton btnIdeal = crearBotonPrincipal("⭐ CONFIG. ENTORNO IDEAL", new Color(41, 128, 185));
        btnIdeal.addActionListener(e -> {
            txtG.setText("9.806"); txtViscosidad.setText("0.0"); 
            txtRadio.setText("0.01"); comboFluidos.setSelectedIndex(0);
        });

        JButton btnReiniciar = crearBotonPrincipal("🔄 REINICIAR SISTEMA", new Color(127, 140, 141));
        btnReiniciar.addActionListener(e -> {
            panelDibujo.detener(); 
            modeloResorte.reset(); 
            panelDibujo.repaint(); 
        });

        JButton btnTabla = crearBotonPrincipal("📊 TABLA DE RESULTADOS", new Color(46, 64, 83));
        btnTabla.addActionListener(e -> new VentanaTablaResultados(modeloResorte).setVisible(true));

        JButton btnVerDatos = crearBotonPrincipal("📋 VER HISTORIAL DB", new Color(52, 73, 94));
        btnVerDatos.addActionListener(e -> new VentanaHistorial().setVisible(true));

        JButton btnVerGraficas = crearBotonPrincipal("📈 MONITOR DE GRÁFICAS", new Color(142, 68, 173));
        btnVerGraficas.addActionListener(e -> new VentanaGraficas(modeloResorte).setVisible(true));

        JButton btnVolver = crearBotonPrincipal("🏠 VOLVER AL MENÚ", new Color(211, 84, 0));
        btnVolver.addActionListener(e -> {
            panelDibujo.detener(); 
            new MenuPrincipal().setVisible(true);
            this.dispose();
        });

        lateral.add(btnCalcular); lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnAcelerar); lateral.add(Box.createVerticalStrut(10)); 
        lateral.add(btnIdeal); lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnReiniciar); lateral.add(Box.createVerticalStrut(10)); 
        lateral.add(btnTabla); lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnVerDatos); lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnVerGraficas); lateral.add(Box.createVerticalStrut(25));
        lateral.add(btnVolver);
        
        add(new JScrollPane(lateral), BorderLayout.WEST);
    }

    private void cargarDatosAModelo() {
        modeloResorte.masa = Double.parseDouble(txtMasa.getText());
        modeloResorte.constanteK = Double.parseDouble(txtK.getText());
        modeloResorte.gravedad = Double.parseDouble(txtG.getText());
        modeloResorte.viscosidadFluido = Double.parseDouble(txtViscosidad.getText());
        modeloResorte.radioEsfera = Double.parseDouble(txtRadio.getText());
    }

    private void actualizarViscosidad() {
        switch (comboFluidos.getSelectedIndex()) {
            case 1: txtViscosidad.setText("0.000018"); break;
            case 2: txtViscosidad.setText("0.001"); break;
            case 3: txtViscosidad.setText("0.04"); break;
            case 4: txtViscosidad.setText("1.5"); break;
        }
    }

    private JTextField createInputField(JPanel p, String label, String def) {
        JLabel l = new JLabel(label); l.setForeground(Color.LIGHT_GRAY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT); p.add(l);
        JTextField t = new JTextField(def); t.setMaximumSize(new Dimension(280, 35));
        t.setBackground(new Color(33, 47, 61)); t.setForeground(Color.WHITE);
        p.add(t); p.add(Box.createVerticalStrut(10));
        return t;
    }

    private JButton crearBotonPrincipal(String texto, Color bg) {
        JButton btn = new JButton(texto); btn.setBackground(bg);
        btn.setForeground(Color.WHITE); btn.setMaximumSize(new Dimension(280, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}