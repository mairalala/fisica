package com.pos.graficafisica;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GraficaFisica extends JFrame {

    private PanelDibujo panel;
    private SimulacionFisica modelo;

    private final Color COLOR_FONDO_LATERAL = new Color(33, 47, 61);
    private final Color COLOR_ACCENTO = new Color(52, 152, 219);
    private final Color COLOR_TEXTO = new Color(236, 240, 241);
    private final Color COLOR_BOTON_NORMAL = new Color(44, 62, 80);

    public GraficaFisica() {
        setTitle("Laboratorio Física: Dinámica y Geometría - TEINCO");
        setSize(1350, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        modelo = new SimulacionFisica();
        panel = new PanelDibujo(modelo);
        add(panel, BorderLayout.CENTER);

        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(COLOR_FONDO_LATERAL);
        lateral.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("CONFIGURACIÓN DE SISTEMA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(COLOR_ACCENTO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lateral.add(lblTitulo);
        lateral.add(Box.createVerticalStrut(15));

        JTextField txtH = createInputField(lateral, "Altura h (m):", "2.5");
        JTextField txtL = createInputField(lateral, "Base L (m):", "4.0");
        JTextField txtMuRampa = createInputField(lateral, "Fricción Rampa (μr):", "0.15");
        JTextField txtGravedad = createInputField(lateral, "Gravedad g (m/s²):", "9.8");

        lateral.add(createLabel(" Forma del Objeto:"));
        JComboBox<String> comboForma = createStyledCombo(new String[]{"Esfera", "Cubo", "Diamante"});
        lateral.add(comboForma);

        lateral.add(createLabel(" Material:"));
        JComboBox<String> comboMaterial = createStyledCombo(new String[]{"Goma", "Madera", "Acero", "Plástico", "Hielo"});
        lateral.add(comboMaterial);

        JTextField txtMasa = createInputField(lateral, "Masa (kg):", "0.25");
        JTextField txtK = createInputField(lateral, "Constante k (N/m):", "100.0");
        JTextField txtX0 = createInputField(lateral, "Elongación x (m):", "1.2");
        JTextField txtMuSuelo = createInputField(lateral, "Fricción Suelo (μs):", "0.25");
        JTextField txtViscosa = createInputField(lateral, "C. Viscoso (b):", "0.05");
        JTextField txtAire = createInputField(lateral, "C. Aire (c):", "0.02");

        lateral.add(Box.createVerticalStrut(15));

        JButton btnAplicar = createStyledButton("APLICAR CAMBIOS", COLOR_ACCENTO, Color.WHITE);
        btnAplicar.addActionListener(e -> actualizarModelo(txtGravedad, txtH, txtL, txtMuRampa, txtMasa, txtK, txtX0, txtMuSuelo, txtViscosa, txtAire, comboMaterial, comboForma));

        JButton btnIdeal = createStyledButton("✨ ESCENARIO IDEAL", new Color(46, 204, 113), Color.WHITE);
        btnIdeal.addActionListener(e -> {
            txtMuRampa.setText("0.0");
            txtMuSuelo.setText("0.0");
            txtViscosa.setText("0.0");
            txtAire.setText("0.0");
            txtGravedad.setText("9.8");
            actualizarModelo(txtGravedad, txtH, txtL, txtMuRampa, txtMasa, txtK, txtX0, txtMuSuelo, txtViscosa, txtAire, comboMaterial, comboForma);
        });

        lateral.add(btnAplicar);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(btnIdeal);

        lateral.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(300, 1));
        lateral.add(sep);
        lateral.add(Box.createVerticalStrut(15));

        JLabel lblAnalisis = createLabel(" 📊 REPORTES Y ANÁLISIS");
        lblAnalisis.setForeground(new Color(241, 196, 15));
        lblAnalisis.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAnalisis.setAlignmentX(Component.CENTER_ALIGNMENT);
        lateral.add(lblAnalisis);
        lateral.add(Box.createVerticalStrut(15));

        JButton btnVerDatos = createStyledButton("📋 Ver Tabla de Datos", COLOR_BOTON_NORMAL, COLOR_TEXTO);
        btnVerDatos.addActionListener(e -> mostrarTablaDatos());

        JButton btnVerGraficas = createStyledButton("📈 Ver Gráficas Cinemat.", COLOR_BOTON_NORMAL, COLOR_TEXTO);
        btnVerGraficas.addActionListener(e -> mostrarVentanaGraficas());

        JButton btnHistorial = createStyledButton("📜 Historial Pruebas DB", new Color(155, 89, 182), COLOR_TEXTO);
        btnHistorial.addActionListener(e -> new VentanaHistorial().setVisible(true));

        JButton btnExportar = createStyledButton("💾 Descargar Datos (TXT)", COLOR_BOTON_NORMAL, COLOR_TEXTO);
        btnExportar.addActionListener(e -> exportarDatos());

        lateral.add(btnVerDatos);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(btnVerGraficas);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(btnHistorial);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(btnExportar);

        JScrollPane scrollLateral = new JScrollPane(lateral);
        scrollLateral.setPreferredSize(new Dimension(320, 0));
        scrollLateral.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLateral.setBorder(null);
        add(scrollLateral, BorderLayout.WEST);

        // --- MODIFICACIÓN EN LA SECCIÓN DE JToolBar ---
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        tools.setBackground(new Color(28, 40, 51));

        JButton btnPlay = createStyledButton(" ▶  LANZAR SIMULACIÓN ", new Color(41, 128, 185), Color.WHITE);
        btnPlay.addActionListener(e -> panel.toggle());

        JButton btnReset = createStyledButton(" ↺  REINICIAR ", new Color(192, 57, 43), Color.WHITE);
        btnReset.addActionListener(e -> panel.reset());

// NUEVO BOTÓN: VOLVER AL MENÚ PRINCIPAL
        JButton btnVolver = createStyledButton(" 🏠 MENÚ ", new Color(44, 62, 80), Color.WHITE);
        btnVolver.addActionListener(e -> {
            new MenuPrincipal().setVisible(true);
            this.dispose();
        });

        tools.add(Box.createHorizontalGlue());
        tools.add(btnVolver); // Se agrega aquí
        tools.add(Box.createHorizontalStrut(10));
        tools.add(btnPlay);
        tools.add(Box.createHorizontalStrut(20));
        tools.add(btnReset);
        tools.add(Box.createHorizontalGlue());

        add(tools, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(174, 182, 191));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setMaximumSize(new Dimension(300, 30));
        combo.setBackground(new Color(44, 62, 80));
        combo.setForeground(Color.WHITE);
        return combo;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setMaximumSize(new Dimension(280, 40));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField createInputField(JPanel p, String label, String def) {
        p.add(createLabel(" " + label));
        JTextField t = new JTextField(def);
        t.setMaximumSize(new Dimension(300, 30));
        t.setBackground(new Color(23, 32, 42));
        t.setForeground(Color.WHITE);
        t.setCaretColor(Color.WHITE);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94)),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        p.add(t);
        p.add(Box.createVerticalStrut(5));
        return t;
    }

    private void mostrarTablaDatos() {
        JFrame frameDatos = new JFrame("Tabla de Resultados - TEINCO");
        frameDatos.setSize(750, 550);
        String[] columnas = {"Tiempo (s)", "Distancia X (m)", "Altura Y (m)", "Velocidad (m/s)", "Aceleración (m/s²)"};
        DefaultTableModel tablaModelo = new DefaultTableModel(columnas, 0);
        for (double[] d : modelo.historial) {
            if (d[0] >= 1.0) {
                tablaModelo.addRow(new Object[]{
                    String.format("%.3f", d[0] - 1), String.format("%.3f", d[1]),
                    String.format("%.3f", d[2]), String.format("%.3f", d[3]), String.format("%.3f", d[4])
                });
            }
        }
        JTable table = new JTable(tablaModelo);
        frameDatos.add(new JScrollPane(table));
        frameDatos.setVisible(true);
    }

    private void mostrarVentanaGraficas() {
        if (modelo.historial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para graficar.");
            return;
        }
        JFrame frameGraficas = new JFrame("Análisis Cinematográfico");
        frameGraficas.setSize(1000, 800);
        frameGraficas.setLayout(new GridLayout(2, 2, 10, 10));

        // CORRECCIÓN: Aseguramos que pasamos el ArrayList correctamente
        ArrayList<double[]> historial = (ArrayList<double[]>) modelo.historial;

        frameGraficas.add(new PanelGrafico("Velocidad vs Tiempo", historial, 3, Color.CYAN));
        frameGraficas.add(new PanelGrafico("Altura vs Tiempo", historial, 2, Color.GREEN));
        frameGraficas.add(new PanelGrafico("Distancia X vs Tiempo", historial, 1, Color.MAGENTA));
        frameGraficas.add(new PanelGrafico("Aceleración vs Tiempo", historial, 4, Color.ORANGE));

        frameGraficas.setVisible(true);
    }

    private void exportarDatos() {
        if (modelo.historial.isEmpty()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter out = new FileWriter(chooser.getSelectedFile() + ".txt")) {
                out.write("T(s)\tX(m)\tY(m)\tV(m/s)\tA(m/s2)\n");
                for (double[] d : modelo.historial) {
                    out.write(String.format("%.3f\t%.3f\t%.3f\t%.3f\t%.3f\n", d[0] - 1, d[1], d[2], d[3], d[4]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void actualizarModelo(JTextField txtG, JTextField txtH, JTextField txtL, JTextField txtMuR,
            JTextField txtM, JTextField txtK, JTextField txtX0, JTextField txtMuS,
            JTextField txtV, JTextField txtA, JComboBox<String> cMat, JComboBox<String> cFor) {
        try {
            // 1. Capturar los valores numéricos de los campos de texto
            modelo.g = Double.parseDouble(txtG.getText());
            modelo.h = Double.parseDouble(txtH.getText());
            modelo.L = Double.parseDouble(txtL.getText());
            modelo.muRampa = Double.parseDouble(txtMuR.getText());
            modelo.m = Double.parseDouble(txtM.getText());
            modelo.k = Double.parseDouble(txtK.getText());
            modelo.x0 = Double.parseDouble(txtX0.getText());
            modelo.muSuelo = Double.parseDouble(txtMuS.getText());
            modelo.bViscoso = Double.parseDouble(txtV.getText());
            modelo.cAire = Double.parseDouble(txtA.getText());

            // 2. CONFIGURAR MATERIAL (Esto activa la elasticidad y fricción específica)
            String materialSeleccionado = (String) cMat.getSelectedItem();
            modelo.configurarMaterial(materialSeleccionado);

            // 3. Pasar la forma y el material al panel para el dibujo visual
            panel.setFormaYMaterial((String) cFor.getSelectedItem(), materialSeleccionado);

            // 4. Recalcular v1 (velocidad al final de la rampa) y reiniciar estado
            modelo.recalcular();
            panel.reset();

            // Opcional: Mostrar confirmación en consola para debug
            System.out.println("Modelo actualizado: " + materialSeleccionado + " | v0: " + String.format("%.2f", modelo.vInicial) + " m/s");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, verifica los datos. Usa punto (.) para decimales.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // 1. Iniciar Pantalla de Carga
        PantallaCarga splash = new PantallaCarga();
        splash.setVisible(true);

        // 2. Inicializar base de datos
        ConexionDB.inicializarDB();

        try {
            Thread.sleep(3500); // Tiempo para el Splash
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Lanzar Menú Principal en lugar de la App directa
        java.awt.EventQueue.invokeLater(() -> {
            try {
                // Configuración de estilo visual
                com.formdev.flatlaf.FlatLightLaf.setup();

                // Lanzamos el menú que creaste en la clase nueva
                MenuPrincipal menu = new MenuPrincipal();
                menu.setVisible(true);

                splash.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
