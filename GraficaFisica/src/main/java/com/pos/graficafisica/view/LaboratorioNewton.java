package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionNewton;
import com.pos.graficafisica.presenter.GraficaNewton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LaboratorioNewton extends JFrame {
    private final SimulacionNewton modelo = new SimulacionNewton();
    private final PanelNewton panelSimulacion = new PanelNewton(modelo);
    private final GraficaNewton panelGraficas = new GraficaNewton(modelo.historial);
    private final JTextField txtMasa = new JTextField("2.0");
    private final JTextField txtMasaB = new JTextField("3.0");
    private final JTextField txtFuerza = new JTextField("10.0");
    private final JComboBox<String> comboLey = new JComboBox<>(new String[]{
            "Primera ley: inercia", "Segunda ley: fuerza y aceleración", "Tercera ley: acción-reacción"
    });
    private final JLabel lblDescripcion = new JLabel();
    private final JButton btnInteractuar = new JButton();
    private final Timer timer;

    public LaboratorioNewton() {
        setTitle("Laboratorio de las tres leyes de Newton");
        setSize(1100, 800);
        setMinimumSize(new Dimension(950, 700));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        timer = new Timer(30, e -> {
            modelo.actualizar(0.03);
            panelSimulacion.repaint();
            panelGraficas.refrescar();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                timer.stop();
                dispose();
            }
        });

        // Panel lateral de controles
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(new Color(28, 40, 51));
        lateral.setBorder(new EmptyBorder(24, 20, 24, 20));
        lateral.setPreferredSize(new Dimension(300, 0));

        JLabel titulo = new JLabel("EXPERIMENTO");
        titulo.setForeground(new Color(93, 173, 226));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(titulo);
        lateral.add(Box.createVerticalStrut(20));

        agregarEtiqueta(lateral, "Ley de Newton:");
        comboLey.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboLey.addActionListener(e -> seleccionarLey());
        lateral.add(comboLey);
        lateral.add(Box.createVerticalStrut(14));

        agregarEtiqueta(lateral, "Masa del objeto A (kg):");
        agregarCampo(lateral, txtMasa);
        agregarEtiqueta(lateral, "Masa del objeto B (kg):");
        agregarCampo(lateral, txtMasaB);
        agregarEtiqueta(lateral, "Fuerza aplicada (N):");
        agregarCampo(lateral, txtFuerza);

        lblDescripcion.setForeground(new Color(210, 220, 230));
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDescripcion.setVerticalAlignment(SwingConstants.TOP);
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(Box.createVerticalStrut(14));
        lateral.add(lblDescripcion);
        lateral.add(Box.createVerticalGlue());

        JButton btnIniciar = crearBoton("INICIAR / REINICIAR", new Color(40, 180, 99));
        btnIniciar.addActionListener(e -> iniciarSimulacion());
        lateral.add(btnIniciar);
        lateral.add(Box.createVerticalStrut(10));

        JButton btnPausar = crearBoton("PAUSAR / CONTINUAR", new Color(41, 128, 185));
        btnPausar.addActionListener(e -> {
            if (timer.isRunning()) {
                timer.stop();
            } else {
                timer.start();
            }
        });
        lateral.add(btnPausar);
        lateral.add(Box.createVerticalStrut(10));

        btnInteractuar.addActionListener(e -> {
            modelo.fuerzaAplicada = !modelo.fuerzaAplicada;
            actualizarTextoInteraccion();
            panelSimulacion.repaint();
        });
        lateral.add(btnInteractuar);
        lateral.add(Box.createVerticalStrut(10));

        // Botón para guardar el estado actual en la base de datos SQLite
        JButton btnGuardar = crearBoton("💾 GUARDAR EN HISTORIAL", new Color(39, 174, 96));
        btnGuardar.addActionListener(e -> {
            modelo.guardarEnBaseDatos();
            JOptionPane.showMessageDialog(this, "¡Simulación de Newton guardada exitosamente en la base de datos!");
        });
        lateral.add(btnGuardar);
        lateral.add(Box.createVerticalStrut(10));

        // Botón para abrir la ventana de consulta del historial de Newton
        JButton btnHistorial = crearBoton("📋 VER HISTORIAL", new Color(142, 68, 173));
        btnHistorial.addActionListener(e -> new VentanaHistorialNewton().setVisible(true));
        lateral.add(btnHistorial);
        lateral.add(Box.createVerticalStrut(10));

        JButton btnVolver = crearBoton("VOLVER AL MENÚ", new Color(211, 84, 0));
        btnVolver.addActionListener(e -> {
            timer.stop();
            new MenuPrincipal().setVisible(true);
            dispose();
        });
        lateral.add(btnVolver);

        // Estructura central dividida con JSplitPane para ubicar arriba la animación y abajo las gráficas
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelSimulacion, panelGraficas);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);

        add(lateral, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);

        comboLey.setSelectedIndex(0);
        actualizarControles();
        modelo.iniciar(SimulacionNewton.Ley.PRIMERA, 2.0, 3.0, 10.0);
    }

    private void iniciarSimulacion() {
        try {
            double masa = Double.parseDouble(txtMasa.getText().trim());
            double masaB = Double.parseDouble(txtMasaB.getText().trim());
            double fuerza = Double.parseDouble(txtFuerza.getText().trim());
            if (!Double.isFinite(masa) || !Double.isFinite(masaB) || !Double.isFinite(fuerza)
                    || masa <= 0 || masaB <= 0 || fuerza < 0) {
                throw new IllegalArgumentException();
            }

            SimulacionNewton.Ley ley = obtenerLeySeleccionada();
            modelo.iniciar(ley, masa, masaB, fuerza);
            if (ley == SimulacionNewton.Ley.SEGUNDA) {
                modelo.fuerzaAplicada = true;
            }
            actualizarTextoInteraccion();
            panelSimulacion.repaint();
            panelGraficas.refrescar();
            timer.start();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa masas positivas y una fuerza igual o mayor que cero.",
                    "Parámetros no válidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SimulacionNewton.Ley obtenerLeySeleccionada() {
        return switch (comboLey.getSelectedIndex()) {
            case 1 -> SimulacionNewton.Ley.SEGUNDA;
            case 2 -> SimulacionNewton.Ley.TERCERA;
            default -> SimulacionNewton.Ley.PRIMERA;
        };
    }

    private void actualizarControles() {
        SimulacionNewton.Ley ley = obtenerLeySeleccionada();
        txtMasaB.setEnabled(ley == SimulacionNewton.Ley.TERCERA);
        switch (ley) {
            case PRIMERA -> lblDescripcion.setText("<html>Un cuerpo conserva el reposo o su velocidad constante si la fuerza neta es cero. Inicia con movimiento uniforme y aplica o retira una fuerza para observar el cambio.</html>");
            case SEGUNDA -> lblDescripcion.setText("<html>La aceleración depende de la fuerza neta y la masa: a = F / m. Cambia los valores y compara el resultado.</html>");
            case TERCERA -> lblDescripcion.setText("<html>Al interactuar, A y B ejercen fuerzas iguales y opuestas. Si sus masas son distintas, sus aceleraciones también lo serán.</html>");
        }
        actualizarTextoInteraccion();
        panelSimulacion.repaint();
        panelGraficas.refrescar();
    }

    private void seleccionarLey() {
        timer.stop();
        modelo.iniciar(obtenerLeySeleccionada(), modelo.masa, modelo.masaB, modelo.fuerza);
        actualizarControles();
    }

    private void actualizarTextoInteraccion() {
        SimulacionNewton.Ley ley = obtenerLeySeleccionada();
        boolean activa = modelo.fuerzaAplicada;
        btnInteractuar.setVisible(ley != SimulacionNewton.Ley.SEGUNDA);
        if (ley == SimulacionNewton.Ley.TERCERA) {
            btnInteractuar.setText(activa ? "DETENER INTERACCIÓN" : "APLICAR INTERACCIÓN");
        } else {
            btnInteractuar.setText(activa ? "RETIRAR FUERZA" : "APLICAR FUERZA");
        }
    }

    private void agregarEtiqueta(JPanel panel, String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Color.LIGHT_GRAY);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(etiqueta);
        panel.add(Box.createVerticalStrut(5));
    }

    private void agregarCampo(JPanel panel, JTextField campo) {
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(campo);
        panel.add(Box.createVerticalStrut(12));
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }
}