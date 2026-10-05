/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pos.graficafisica.view;

import com.pos.graficafisica.model.ConexionDB;
import com.formdev.flatlaf.FlatLightLaf;

/**
 *
 * @author maira
 */
public class start {
    public static void main(String[] args) {
        // Configuración de estilo visual FlatLaf antes de inicializar componentes
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("No se pudo inicializar FlatLaf");
        }

        // 1. Iniciar Pantalla de Carga
        PantallaCarga splash = new PantallaCarga();
        splash.setVisible(true);

        // 2. Inicializar base de datos desde el Modelo (crea tablas de resortes, parábolas y catapultas)
        ConexionDB.inicializarDB();

        try {
            Thread.sleep(3500); // Tiempo de espera para la animación del Splash
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Lanzar el Menú Principal en el hilo de eventos de Swing
        java.awt.EventQueue.invokeLater(() -> {
            try {
                // Lanzamos el menú principal
                MenuPrincipal menu = new MenuPrincipal();
                menu.setVisible(true);

                // Cerramos la pantalla de carga
                splash.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}