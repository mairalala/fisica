package com.pos.graficafisica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class VentanaHistorial extends JFrame {
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaHistorial() {
        setTitle("HISTORIAL DE LABORATORIO - LEY DE HOOKE");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Columnas adaptadas a los datos de tu laboratorio de resorte
        modeloTabla = new DefaultTableModel(new String[]{
            "ID", "Fecha", "Masa (kg)", "Constante K (N/m)", "Elongación (m)"
        }, 0);
        
        tabla = new JTable(modeloTabla);
        tabla.setBackground(new Color(44, 62, 80));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(52, 73, 94));
        tabla.setRowHeight(25);
        
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pBotones = new JPanel();
        pBotones.setBackground(new Color(33, 47, 61));
        
        JButton btnRefrescar = new JButton("🔄 Actualizar Datos");
        JButton btnBorrar = new JButton("🗑️ Limpiar Historial");
        
        btnRefrescar.addActionListener(e -> cargarDatos());
        btnBorrar.addActionListener(e -> borrarTodo());
        
        pBotones.add(btnRefrescar);
        pBotones.add(btnBorrar);
        add(pBotones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        // Usamos la tabla 'historial_resorte' que definimos en ConexionDB
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM historial_resorte ORDER BY id DESC")) {
            
            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("fecha"), 
                    rs.getDouble("masa"), 
                    rs.getDouble("constante_k"), 
                    String.format("%.4f", rs.getDouble("elongacion"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la base de datos.");
        }
    }

    private void borrarTodo() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Borrar todos los registros?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionDB.conectar();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM historial_resorte");
                cargarDatos();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}