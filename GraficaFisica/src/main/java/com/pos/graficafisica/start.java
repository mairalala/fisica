/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pos.graficafisica;

/**
 *
 * @author maira
 */
public class start {
    public static void main(String[] args) {
        // 1. Iniciar Pantalla de Carga
        PantallaCarga splash = new PantallaCarga();
        splash.setVisible(true);

        // 2. Inicializar base de datos
        ConexionDB.inicializarDB();

        try {
            Thread.sleep(3500); // Tiempo para el Splash
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Lanzar Menú Principal en lugar de la App directa
        java.awt.EventQueue.invokeLater(() -> {
            try {
                // Configuración de estilo visual
                com.formdev.flatlaf.FlatLightLaf.setup();

                // Lanzamos el menú que creaste en la clase nueva
                MenuPrincipal menu = new MenuPrincipal();
                menu.setVisible(true);

                splash.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
