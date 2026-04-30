package com.pos.graficafisica;

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
        // SQL para la tabla de resortes
        String sqlResorte = "CREATE TABLE IF NOT EXISTS historial_resorte ("
                           + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                           + "masa REAL, "
                           + "constante_k REAL, "
                           + "elongacion REAL, "
                           + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                           + ");";

        // SQL para la nueva tabla de parábolas
        String sqlParabola = "CREATE TABLE IF NOT EXISTS historial_parabolas ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + "altura REAL, "
                            + "base REAL, "
                            + "masa REAL, "
                            + "velocidad_inicial REAL, "
                            + "distancia_total REAL, "
                            + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                            + ");";
        
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sqlResorte);
                stmt.execute(sqlParabola);
                System.out.println("Tablas 'historial_resorte' e 'historial_parabolas' listas.");
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

    /**
     * Inserta un nuevo lanzamiento en la base de datos.
     * Este es el método que invoca automáticamente el PanelParabola al finalizar la simulación.
     */
    public static void insertarLanzamientoParabola(double h, double l, double m, double v0, double dist) {
        String sql = "INSERT INTO historial_parabolas(altura, base, masa, velocidad_inicial, distancia_total) VALUES(?,?,?,?,?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, h);
            pstmt.setDouble(2, l);
            pstmt.setDouble(3, m);
            pstmt.setDouble(4, v0);
            pstmt.setDouble(5, dist);
            pstmt.executeUpdate();
            System.out.println("Lanzamiento registrado exitosamente en la DB.");
        } catch (Exception e) {
            System.out.println("Error al insertar lanzamiento: " + e.getMessage());
        }
    }

    // Mantenemos este método por compatibilidad con versiones anteriores del código
    public static void guardarSimulacionParabola(double h, double l, double m, double v0, double dist) {
        insertarLanzamientoParabola(h, l, m, v0, dist);
    }

    /**
     * Obtiene todos los registros de parábolas para mostrarlos en el JTable del historial.
     */
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

    // Método genérico para futuras expansiones
    public static void guardarSimulacion(String tipo, double val1, double val2, int val3) {
        System.out.println("Guardado general llamado para tipo: " + tipo);
    }
}