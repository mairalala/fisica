package com.pos.graficafisica.view;

import com.pos.graficafisica.model.ConexionDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaHistorialCatapulta extends JFrame {

    private JTable tabla;
    private DefaultTableModel modeloTabla;

    public VentanaHistorialCatapulta() {
        setTitle("Historial de Pruebas - Módulo de Catapultas");
        setSize(1050, 550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(24, 30, 42));
        panelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(panelPrincipal);

        JLabel lblTitulo = new JLabel("REGISTRO HISTÓRICO DE LANZAMIENTOS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        modeloTabla = ConexionDB.obtenerHistorialCatapultas();
        tabla = new JTable(modeloTabla);

        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(33, 43, 58));
        tabla.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(new Color(30, 35, 45));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelInferior.setOpaque(false);
        panelInferior.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnActualizar = new JButton("ACTUALIZAR");
        btnActualizar.setBackground(new Color(41, 128, 185));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.addActionListener(e -> recargarDatos());

        JButton btnCerrar = new JButton("CERRAR");
        btnCerrar.setBackground(new Color(192, 57, 43));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(e -> dispose());

        panelInferior.add(btnActualizar);
        panelInferior.add(btnCerrar);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
    }

    private void recargarDatos() {
        DefaultTableModel nuevoModelo = ConexionDB.obtenerHistorialCatapultas();
        tabla.setModel(nuevoModelo);
        if (nuevoModelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay lanzamientos guardados todavía. Realiza una simulación completa.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}