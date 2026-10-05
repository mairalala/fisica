package com.pos.graficafisica.presenter;

import com.pos.graficafisica.model.SimulacionResorte;
import com.pos.graficafisica.view.PanelGrafico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class GraficasResortes extends JFrame {

    private SimulacionResorte modelo;
    private ArrayList<double[]> listaDatos = new ArrayList<>();
    private static final int MAX_PUNTOS_TOTALES = 50000; 
    private Timer timerCaptura;
    private Timer timerReproduccion; 
    private int indiceReproduccion = 0; 
    private boolean modoReproduccion = false;

    private JButton btnControlAnimacion;
    private static ArrayList<double[]> listaDatosEstatica;
    private static int contadorMuestreo = 0;

    public GraficasResortes(SimulacionResorte modelo) {
        this.modelo = modelo;
        listaDatosEstatica = this.listaDatos;
        
        setTitle("CENTRO DE ANÁLISIS DINÁMICO - LEY DE HOOKE");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // MODIFICACIÓN: Detener timers al cerrar la ventana para liberar recursos
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (timerCaptura != null) timerCaptura.stop();
                if (timerReproduccion != null) timerReproduccion.stop();
                PanelGrafico.setPuntoCorte(-1);
                PanelGrafico.setModoResaltado(false);
            }
        });

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setBackground(new Color(33, 47, 61));
        pestañas.setForeground(Color.WHITE);

        PanelGrafico gPosicion = new PanelGrafico("Elongación vs Tiempo (m)", listaDatos, 1, Color.CYAN);
        PanelGrafico gVelocidad = new PanelGrafico("Velocidad vs Tiempo (m/s)", listaDatos, 2, new Color(46, 204, 113));
        PanelGrafico gFuerza = new PanelGrafico("Aceleración vs Tiempo (m/s²)", listaDatos, 3, new Color(231, 76, 60));

        pestañas.addTab("📊 POSICIÓN", gPosicion);
        pestañas.addTab("⚡ VELOCIDAD", gVelocidad);
        pestañas.addTab("🚀 ACELERACIÓN", gFuerza);

        add(pestañas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(10, 12, 15));
        panelInferior.setPreferredSize(new Dimension(0, 70));

        JPanel monitorRapido = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(10, 12, 15));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.setColor(modoReproduccion ? Color.YELLOW : Color.GREEN);
                g2.setFont(new Font("Monospaced", Font.BOLD, 12));
                
                String estado = modoReproduccion ? "MODO REPRODUCCIÓN ANÁLISIS" : "TELEMETRÍA EN VIVO";
                String telemetria = String.format("%s >> V: %.3f m/s | Y: %.3f m | T: %.2f s",
                        estado, modelo.velocidad, modelo.elongacion, modelo.tiempo);
                
                g2.drawString(telemetria, 20, 40);
            }
        };
        
        monitorRapido.setOpaque(false);
        panelInferior.add(monitorRapido, BorderLayout.CENTER);

        btnControlAnimacion = new JButton("⏸ PAUSAR MONITOR");
        btnControlAnimacion.setBackground(new Color(231, 76, 60));
        btnControlAnimacion.setForeground(Color.WHITE);
        btnControlAnimacion.setPreferredSize(new Dimension(220, 40));

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        panelBoton.setOpaque(false);
        panelBoton.add(btnControlAnimacion);
        panelInferior.add(panelBoton, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        timerCaptura = new Timer(20, e -> {
            if (modelo.tiempo == 0 && listaDatos.size() > 0) {
                listaDatos.clear();
                modoReproduccion = false;
                btnControlAnimacion.setText("⏸ PAUSAR MONITOR");
            }

            if (modelo.tiempo > 0 && !modoReproduccion) {
                listaDatos.add(new double[]{modelo.tiempo, modelo.elongacion, modelo.velocidad, modelo.aceleracion});
                
                if (Math.abs(modelo.velocidad) < 1e-4 && modelo.tiempo > 2) {
                    btnControlAnimacion.setText("🎬 REPRODUCIR ANÁLISIS");
                    btnControlAnimacion.setBackground(new Color(142, 68, 173)); 
                }

                pestañas.repaint(); 
                monitorRapido.repaint();
            }
        });

        timerReproduccion = new Timer(10, e -> {
            indiceReproduccion += 5; 
            if (indiceReproduccion >= listaDatos.size()) {
                indiceReproduccion = listaDatos.size();
                timerReproduccion.stop();
                btnControlAnimacion.setText("🔄 REPETIR ANÁLISIS");
            }
            PanelGrafico.setPuntoCorte(indiceReproduccion); 
            pestañas.repaint();
        });

        btnControlAnimacion.addActionListener(e -> {
            if (btnControlAnimacion.getText().contains("ANÁLISIS")) {
                modoReproduccion = true;
                indiceReproduccion = 0;
                PanelGrafico.setModoResaltado(true); 
                timerReproduccion.start();
                btnControlAnimacion.setText("⏹ DETENER REPRODUCCIÓN");
                btnControlAnimacion.setBackground(Color.BLACK);
            } 
            else if (modoReproduccion) {
                timerReproduccion.stop();
                modoReproduccion = false;
                PanelGrafico.setModoResaltado(false);
                PanelGrafico.setPuntoCorte(-1); 
                btnControlAnimacion.setText("🎬 REPRODUCIR ANÁLISIS");
                btnControlAnimacion.setBackground(new Color(142, 68, 173));
            }
            else {
                if (timerCaptura.isRunning()) {
                    timerCaptura.stop();
                    btnControlAnimacion.setText("▶ REANUDAR MONITOR");
                    btnControlAnimacion.setBackground(new Color(40, 180, 99));
                } else {
                    timerCaptura.start();
                    btnControlAnimacion.setText("⏸ PAUSAR MONITOR");
                    btnControlAnimacion.setBackground(new Color(231, 76, 60));
                }
            }
        });

        timerCaptura.start();
    }

    public static void registrarPuntoManual(double[] punto) {
        if (listaDatosEstatica != null) {
            contadorMuestreo++;
            if (contadorMuestreo % 10 == 0) {
                listaDatosEstatica.add(punto);
            }
        }
    }
}