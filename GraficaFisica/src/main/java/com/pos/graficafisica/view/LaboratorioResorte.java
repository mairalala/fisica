package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionResorte;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class LaboratorioResorte extends JFrame {
    private SimulacionResorte modeloResorte = new SimulacionResorte();
    private PanelResorte panelDibujo;
    private JTextField txtMasa, txtK, txtG, txtViscosidad, txtRadio;
    private JComboBox<String> comboFluidos;
    
    private ArrayList<double[]> listaDatos = new ArrayList<>();
    private JTabbedPane pestañas;
    private Timer timerCaptura;

    // --- NUEVA VARIABLE DE CONTROL DE ZOOM GLOBAL ---
    private double zoomGlobal = 1.0;

    public LaboratorioResorte() {
        setTitle("LABORATORIO LEY DE HOOKE - VISTA EN TIEMPO REAL");
        setSize(1400, 950);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panelDibujo != null) panelDibujo.detener();
                if (timerCaptura != null) timerCaptura.stop();
                dispose();
            }
        });

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. PANEL LATERAL (HUD de parámetros) ---
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

        // --- BOTONES DE ACCIÓN ---
        JButton btnCalcular = crearBotonPrincipal("▶ EJECUTAR SIMULACIÓN", new Color(40, 180, 99));
        btnCalcular.addActionListener(e -> {
            try {
                cargarDatosAModelo();
                modeloResorte.reiniciarHistorial();
                listaDatos.clear();
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
                    listaDatos.add(new double[]{modeloResorte.tiempo, modeloResorte.elongacion, modeloResorte.velocidad, modeloResorte.aceleracion});
                    if (Math.abs(modeloResorte.velocidad) < 1e-6 && Math.abs(modeloResorte.aceleracion) < 1e-6) break;
                }
                panelDibujo.repaint(); 
                pestañas.repaint();
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
            listaDatos.clear();
            zoomGlobal = 1.0; // Reset zoom
            actualizarZoomComponentes();
            panelDibujo.repaint(); 
            pestañas.repaint();
        });

        JButton btnTabla = crearBotonPrincipal("📊 TABLA DE RESULTADOS", new Color(46, 64, 83));
        btnTabla.addActionListener(e -> new VentanaTablaResultados(modeloResorte).setVisible(true));

        JButton btnVerDatos = crearBotonPrincipal("📋 VER HISTORIAL DB", new Color(52, 73, 94));
        btnVerDatos.addActionListener(e -> new VentanaHistorial().setVisible(true));

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
        lateral.add(btnVolver);
        
        add(new JScrollPane(lateral), BorderLayout.WEST);

        // --- 2. CONTENEDOR DERECHO (Dibujo y Gráficas) ---
        JPanel contenedorDerecho = new JPanel(new BorderLayout());
        panelDibujo = new PanelResorte(modeloResorte);
        contenedorDerecho.add(panelDibujo, BorderLayout.CENTER);

        JPanel panelGraficasSur = new JPanel(new BorderLayout());
        panelGraficasSur.setPreferredSize(new Dimension(0, 380));

        pestañas = new JTabbedPane();
        pestañas.setBackground(new Color(33, 47, 61));
        pestañas.setForeground(Color.WHITE);

        pestañas.addTab("📊 POSICIÓN", new PanelGrafico("Elongación (m)", listaDatos, 1, Color.CYAN));
        pestañas.addTab("⚡ VELOCIDAD", new PanelGrafico("Velocidad (m/s)", listaDatos, 2, new Color(46, 204, 113)));
        pestañas.addTab("🚀 ACELERACIÓN", new PanelGrafico("Aceleración (m/s²)", listaDatos, 3, new Color(231, 76, 60)));
        pestañas.addTab("🌀 FASE", new PanelGrafico("Espacio de Fase (y vs v)", listaDatos, 4, new Color(155, 89, 182)));

        panelGraficasSur.add(pestañas, BorderLayout.CENTER);

        // --- HUD INFERIOR CON BOTONES DE ZOOM ---
        JPanel monitorRapido = new JPanel(new BorderLayout());
        monitorRapido.setBackground(new Color(10, 12, 15));
        monitorRapido.setPreferredSize(new Dimension(0, 50));
        monitorRapido.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 50)));

        JLabel lblInfo = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GREEN);
                g2.setFont(new Font("Monospaced", Font.BOLD, 13));
                String info = String.format(" ESTADO: %s | Y: %.3f m | V: %.3f m/s | T: %.2f s", 
                        modeloResorte.obtenerTipoFlujo(), modeloResorte.elongacion, modeloResorte.velocidad, modeloResorte.tiempo);
                g2.drawString(info, 10, 28);
            }
        };
        monitorRapido.add(lblInfo, BorderLayout.CENTER);

        JPanel panelZoomUI = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        panelZoomUI.setOpaque(false);

        JButton btnZoomIn = new JButton(" ZOOM + ");
        JButton btnZoomOut = new JButton(" ZOOM - ");
        estilizarBotonZoom(btnZoomIn, new Color(46, 204, 113));
        estilizarBotonZoom(btnZoomOut, new Color(231, 76, 60));

        // Lógica de Zoom que afecta a TODO el entorno
        btnZoomIn.addActionListener(e -> {
            zoomGlobal *= 1.2;
            actualizarZoomComponentes();
        });

        btnZoomOut.addActionListener(e -> {
            zoomGlobal /= 1.2;
            actualizarZoomComponentes();
        });

        panelZoomUI.add(new JLabel("🔍 ZOOM ENTORNO:"));
        panelZoomUI.add(btnZoomIn);
        panelZoomUI.add(btnZoomOut);
        monitorRapido.add(panelZoomUI, BorderLayout.EAST);

        panelGraficasSur.add(monitorRapido, BorderLayout.SOUTH);
        contenedorDerecho.add(panelGraficasSur, BorderLayout.SOUTH);
        add(contenedorDerecho, BorderLayout.CENTER);

        timerCaptura = new Timer(20, e -> {
            if (modeloResorte.tiempo > 0) {
                listaDatos.add(new double[]{
                    modeloResorte.tiempo, 
                    modeloResorte.elongacion, 
                    modeloResorte.velocidad, 
                    modeloResorte.aceleracion
                });
                pestañas.repaint();
                lblInfo.repaint();
            }
        });
        timerCaptura.start();
    }

    // --- NUEVO MÉTODO PARA SINCRONIZAR ZOOM ---
    private void actualizarZoomComponentes() {
        // 1. Afectar al Laboratorio/Resorte
        if (panelDibujo != null) {
            panelDibujo.setEscalaVisual(zoomGlobal); 
            panelDibujo.repaint();
        }
        // 2. Afectar a la Gráfica visible
        PanelGrafico pg = (PanelGrafico) pestañas.getSelectedComponent();
        if (pg != null) {
            pg.setZoomFactor(zoomGlobal);
            pg.repaint();
        }
    }

    private void estilizarBotonZoom(JButton btn, Color c) {
        btn.setBackground(new Color(30, 30, 30));
        btn.setForeground(c);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createLineBorder(c, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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