package com.pos.graficafisica.view;

import com.pos.graficafisica.model.ConexionDB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class VentanaHistorialNewton extends JFrame {
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaHistorialNewton() {
        setTitle("HISTORIAL DE LABORATORIO - LEYES DE NEWTON");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        modeloTabla = new DefaultTableModel(new String[]{
                "ID", "Fecha", "Ley", "Masa A (kg)", "Masa B (kg)", "Fuerza (N)", "Acel. A (m/s²)", "Tiempo (s)"
        }, 0);

        tabla = new JTable(modeloTabla);
        configurarEstiloTabla();

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel pBotones = new JPanel();
        pBotones.setBackground(new Color(33, 47, 61));

        JButton btnRefrescar = new JButton("🔄 Actualizar Historial");
        JButton btnBorrar = new JButton("🗑️ Limpiar Todo");

        btnRefrescar.addActionListener(e -> cargarDatos());
        btnBorrar.addActionListener(e -> borrarTodo());

        pBotones.add(btnRefrescar);
        pBotones.add(btnBorrar);
        add(pBotones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void configurarEstiloTabla() {
        tabla.setBackground(new Color(44, 62, 80));
        tabla.setForeground(Color.WHITE);
        tabla.setGridColor(new Color(52, 73, 94));
        tabla.setRowHeight(25);
        tabla.getTableHeader().setBackground(new Color(23, 32, 42));
        tabla.getTableHeader().setForeground(new Color(52, 152, 219));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        String sql = "SELECT * FROM historial_newton ORDER BY id DESC";

        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getString("ley"),
                        rs.getDouble("masa"),
                        rs.getDouble("masa_b"),
                        String.format("%.2f", rs.getDouble("fuerza")),
                        String.format("%.2f", rs.getDouble("aceleracion_a")),
                        String.format("%.2f", rs.getDouble("tiempo_total"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al acceder al historial de Newton: " + e.getMessage());
        }
    }

    private void borrarTodo() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Deseas eliminar permanentemente todos los registros de Newton?",
                "Confirmar Limpieza",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if(confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionDB.conectar();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM historial_newton");
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Historial vaciado correctamente.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al borrar registros: " + e.getMessage());
            }
        }
    }
}