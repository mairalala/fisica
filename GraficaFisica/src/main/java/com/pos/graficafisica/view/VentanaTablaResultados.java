package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionResorte;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class VentanaTablaResultados extends JFrame {
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private SimulacionResorte modeloFisico;

    public VentanaTablaResultados(SimulacionResorte modelo) {
        this.modeloFisico = modelo;
        setTitle("REGISTRO DE ITERACIONES - MÉTODO RUNGE-KUTTA");
        setSize(750, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columnas = {"Tiempo (s)", "Posición (m)", "Velocidad (m/s)", "Aceleración (m/s²)", "Flujo"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modeloTabla);
        
        tabla.setBackground(new Color(236, 240, 241));
        tabla.getTableHeader().setBackground(new Color(44, 62, 80));
        tabla.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(52, 73, 94));

        JButton btnCargar = new JButton("🔄 ACTUALIZAR TABLA");
        btnCargar.addActionListener(e -> cargarDatos());

        JButton btnExportar = new JButton("📥 DESCARGAR (.CSV)");
        btnExportar.addActionListener(e -> exportarCSV());

        panelSuperior.add(btnCargar);
        panelSuperior.add(btnExportar);
        add(panelSuperior, BorderLayout.NORTH);

        cargarDatos();
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        List<double[]> historial = modeloFisico.getHistorialDatos(); 
        for (double[] fila : historial) {
            modeloTabla.addRow(new Object[]{
                String.format("%.3f", fila[0]),
                String.format("%.4f", fila[1]),
                String.format("%.4f", fila[2]),
                String.format("%.4f", fila[3]),
                modeloFisico.obtenerTipoFlujo()
            });
        }
    }

    private void exportarCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile() + ".csv")) {
                for (int i = 0; i < modeloTabla.getColumnCount(); i++) 
                    fw.write(modeloTabla.getColumnName(i) + ",");
                fw.write("\n");
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    for (int j = 0; j < modeloTabla.getColumnCount(); j++) 
                        fw.write(modeloTabla.getValueAt(i, j).toString() + ",");
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Archivo guardado exitosamente.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}