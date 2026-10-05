package com.pos.graficafisica.view;

import com.pos.graficafisica.model.SimulacionParabola;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class VentanaTablaResultadosParabolas extends JFrame {
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private SimulacionParabola modeloFisico; // Adaptado a parábolas

    public VentanaTablaResultadosParabolas(SimulacionParabola modelo) {
        this.modeloFisico = modelo;
        setTitle("REGISTRO DE ITERACIONES - MÉTODO RUNGE-KUTTA 4");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Columnas adaptadas para trayectoria en 2D y cinemática
        String[] columnas = {
            "Tiempo (s)", 
            "Posición X (m)", 
            "Posición Y (m)", 
            "Velocidad Total (m/s)", 
            "Aceleración (m/s²)"
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modeloTabla);
        
        // Estética de la tabla
        tabla.setBackground(new Color(236, 240, 241));
        tabla.getTableHeader().setBackground(new Color(44, 62, 80));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setRowHeight(22);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(52, 73, 94));

        JButton btnCargar = new JButton("🔄 ACTUALIZAR DATOS");
        btnCargar.addActionListener(e -> cargarDatos());

        JButton btnExportar = new JButton("📥 EXPORTAR CSV");
        btnExportar.addActionListener(e -> exportarCSV());

        panelSuperior.add(btnCargar);
        panelSuperior.add(btnExportar);
        add(panelSuperior, BorderLayout.NORTH);

        cargarDatos();
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        // Se obtiene el historial generado por el método RK4
        List<double[]> historial = modeloFisico.historial; 
        
        for (double[] fila : historial) {
            modeloTabla.addRow(new Object[]{
                String.format("%.3f", fila[0]), // Tiempo
                String.format("%.4f", fila[1]), // Posición X
                String.format("%.4f", fila[2]), // Posición Y
                String.format("%.4f", fila[3]), // Velocidad Total
                String.format("%.4f", fila[4])  // Aceleración Resultante
            });
        }
    }

    private void exportarCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar resultados del Laboratorio de Parábolas");
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = chooser.getSelectedFile().toString();
            if (!ruta.toLowerCase().endsWith(".csv")) {
                ruta += ".csv";
            }
            
            try (FileWriter fw = new FileWriter(ruta)) {
                // Escribir encabezados
                for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
                    fw.write(modeloTabla.getColumnName(i) + (i == modeloTabla.getColumnCount() - 1 ? "" : ","));
                }
                fw.write("\n");
                
                // Escribir datos de las iteraciones
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                        fw.write(modeloTabla.getValueAt(i, j).toString() + (j == modeloTabla.getColumnCount() - 1 ? "" : ","));
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Datos de cinemática exportados correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}