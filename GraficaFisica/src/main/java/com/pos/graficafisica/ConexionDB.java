package com.pos.graficafisica;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

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

    // Cambiado de crearTablas a inicializarDB para que coincida con GraficaFisica.java
    public static void inicializarDB() {
        String sql = "CREATE TABLE IF NOT EXISTS historial_resorte ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "masa REAL, "
                   + "constante_k REAL, "
                   + "elongacion REAL, "
                   + "fecha DATETIME DEFAULT CURRENT_TIMESTAMP"
                   + ");";
        
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sql);
                System.out.println("Base de datos y tabla 'historial_resorte' listas.");
            }
        } catch (Exception e) {
            System.out.println("Error al inicializar DB: " + e.getMessage());
        }
    }

    // Sobrecarga de método para que PanelDibujo.java no falle
    public static void guardarSimulacion(String tipo, double val1, double val2, int val3) {
        // Este método vacío evita el error de compilación en PanelDibujo
        System.out.println("Guardado general llamado desde PanelDibujo");
    }

    // Método específico para el Laboratorio de Resorte
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
}