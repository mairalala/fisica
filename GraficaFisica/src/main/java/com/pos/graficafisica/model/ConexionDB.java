package com.pos.graficafisica.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class ConexionDB {

    private static final String URL = "jdbc:sqlite:grafica_fisica.db";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.out.println("Error al conectar a SQLite: " + e.getMessage());
            return null;
        }
    }

    public static void inicializarDB() {
        String sqlResorte = "CREATE TABLE IF NOT EXISTS historial_resorte ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "masa REAL, "
                + "constante_k REAL, "
                + "elongacion REAL, "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String sqlParabola = "CREATE TABLE IF NOT EXISTS historial_parabolas ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "altura REAL, "
                + "base REAL, "
                + "masa REAL, "
                + "velocidad_inicial REAL, "
                + "distancia_total REAL, "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String sqlCatapulta = "CREATE TABLE IF NOT EXISTS historial_catapulta ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "masa REAL, "
                + "angulo REAL, "
                + "deformacion REAL, "
                + "v_max REAL, "
                + "h_max REAL, "
                + "impulso REAL, "
                + "estado TEXT, "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String sqlNewton = "CREATE TABLE IF NOT EXISTS historial_newton ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ley TEXT, "
                + "masa REAL, "
                + "masa_b REAL, "
                + "fuerza REAL, "
                + "aceleracion_a REAL, "
                + "aceleracion_b REAL, "
                + "tiempo_total REAL, "
                + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sqlResorte);
                stmt.execute(sqlParabola);
                stmt.execute(sqlCatapulta);
                stmt.execute(sqlNewton);
                System.out.println("Tablas de base de datos inicializadas correctamente.");
            }
        } catch (Exception e) {
            System.out.println("Error al inicializar DB: " + e.getMessage());
        }
    }

    // --- MÉTODOS PARA RESORTES ---

    public static void guardarSimulacionResorte(double masa, double k, double elongacion) {
        String sql = "INSERT INTO historial_resorte(masa, constante_k, elongacion) VALUES(?,?,?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, masa);
            pstmt.setDouble(2, k);
            pstmt.setDouble(3, elongacion);
            pstmt.executeUpdate();
            System.out.println("Simulación de resorte guardada.");
        } catch (Exception e) {
            System.out.println("Error al guardar resorte: " + e.getMessage());
        }
    }

    // --- MÉTODOS PARA PARÁBOLAS ---

    public static void insertarLanzamientoParabola(double h, double l, double m, double v0, double dist) {
        String sql = "INSERT INTO historial_parabolas(altura, base, masa, velocidad_inicial, distancia_total) VALUES(?,?,?,?,?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, h);
            pstmt.setDouble(2, l);
            pstmt.setDouble(3, m);
            pstmt.setDouble(4, v0);
            pstmt.setDouble(5, dist);
            pstmt.executeUpdate();
            System.out.println("Lanzamiento de parábola registrado en la DB.");
        } catch (Exception e) {
            System.out.println("Error al insertar lanzamiento de parábola: " + e.getMessage());
        }
    }

    public static void guardarSimulacionParabola(double h, double l, double m, double v0, double dist) {
        insertarLanzamientoParabola(h, l, m, v0, dist);
    }

    public static DefaultTableModel obtenerHistorialParabolas() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Altura (m)", "Base (m)", "Masa (kg)", "V. Inicial (m/s)", "Alcance (m)", "Fecha"}, 0
        );
        String sql = "SELECT * FROM historial_parabolas ORDER BY fecha DESC";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("altura"),
                        rs.getDouble("base"),
                        rs.getDouble("masa"),
                        rs.getDouble("velocidad_inicial"),
                        rs.getDouble("distancia_total"),
                        rs.getString("fecha")
                });
            }
        } catch (Exception e) {
            System.out.println("Error al obtener historial de parábolas: " + e.getMessage());
        }
        return modelo;
    }

    // --- MÉTODOS PARA CATAPULTAS ---

    public static void guardarLanzamientoCatapulta(double masa, double angulo, double deformacion,
                                                   double vMax, double hMax, double impulso, String estado) {
        String sql = "INSERT INTO historial_catapulta (masa, angulo, deformacion, v_max, h_max, impulso, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, masa);
            pstmt.setDouble(2, angulo);
            pstmt.setDouble(3, deformacion);
            pstmt.setDouble(4, vMax);
            pstmt.setDouble(5, hMax);
            pstmt.setDouble(6, impulso);
            pstmt.setString(7, estado);
            pstmt.executeUpdate();
            System.out.println("-> [BD] Lanzamiento de catapulta guardado exitosamente.");
        } catch (Exception e) {
            System.out.println("-> [BD] Error al guardar catapulta: " + e.getMessage());
        }
    }

    public static DefaultTableModel obtenerHistorialCatapultas() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Masa (kg)", "Ángulo (°)", "Deformación (m)", "V. Máx (m/s)", "H. Máx (m)", "Impulso (N·s)", "Estado", "Fecha"}, 0
        );
        String sql = "SELECT * FROM historial_catapulta ORDER BY fecha DESC";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("masa"),
                        rs.getDouble("angulo"),
                        rs.getDouble("deformacion"),
                        rs.getDouble("v_max"),
                        rs.getDouble("h_max"),
                        rs.getDouble("impulso"),
                        rs.getString("estado"),
                        rs.getString("fecha")
                });
            }
        } catch (Exception e) {
            System.out.println("-> [BD] Error al obtener historial de catapultas: " + e.getMessage());
        }
        return modelo;
    }

    // --- MÉTODOS PARA LEYES DE NEWTON ---

    public static void guardarSimulacionNewton(String ley, double masa, double masaB, double fuerza,
                                               double aceleracionA, double aceleracionB, double tiempoTotal) {
        String sql = "INSERT INTO historial_newton (ley, masa, masa_b, fuerza, aceleracion_a, aceleracion_b, tiempo_total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ley);
            pstmt.setDouble(2, masa);
            pstmt.setDouble(3, masaB);
            pstmt.setDouble(4, fuerza);
            pstmt.setDouble(5, aceleracionA);
            pstmt.setDouble(6, aceleracionB);
            pstmt.setDouble(7, tiempoTotal);
            pstmt.executeUpdate();
            System.out.println("-> [BD] Simulación de Leyes de Newton guardada exitosamente.");
        } catch (Exception e) {
            System.out.println("-> [BD] Error al guardar simulación de Newton: " + e.getMessage());
        }
    }

    public static DefaultTableModel obtenerHistorialNewton() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"ID", "Ley", "Masa A (kg)", "Masa B (kg)", "Fuerza (N)", "Acel. A (m/s²)", "Acel. B (m/s²)", "Tiempo (s)", "Fecha"}, 0
        );
        String sql = "SELECT * FROM historial_newton ORDER BY fecha DESC";
        try (Connection conn = conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("ley"),
                        rs.getDouble("masa"),
                        rs.getDouble("masa_b"),
                        rs.getDouble("fuerza"),
                        rs.getDouble("aceleracion_a"),
                        rs.getDouble("aceleracion_b"),
                        rs.getDouble("tiempo_total"),
                        rs.getString("fecha")
                });
            }
        } catch (Exception e) {
            System.out.println("-> [BD] Error al obtener historial de Newton: " + e.getMessage());
        }
        return modelo;
    }

    // Método genérico
    public static void guardarSimulacion(String tipo, double val1, double val2, int val3) {
        System.out.println("Guardado general llamado para tipo: " + tipo);
    }
}