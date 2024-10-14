package com.example.analizador_lexico;

import javafx.collections.ObservableList;

public class semantico {
    private HelloController helloController;

    // Constructor que recibe la instancia de HelloController
    public semantico(HelloController helloController) {
        this.helloController = helloController;
        mostrarExpresiones(); // Llamar a mostrarExpresiones al crear la instancia
    }

    private void mostrarExpresiones() {
        ObservableList<Analisis> lista_expresiones = helloController.getListaExpresiones();

        for (Analisis analisis : lista_expresiones) {
            System.out.println("Expresion: " + analisis.getExpresion());
            System.out.println("Tipo: " + analisis.getTipo());
            System.out.println("Renglon: " + analisis.getRenglon());
            System.out.println("Columna: " + analisis.getColumna());
            System.out.println("--------------------");  // Separador entre cada análisis
        }
    }
    
    // Metodos para las diferentes etapas del análisis semántico

    public void verificarTipos() {
        // Aquí iría la logica para verificar tipos
    }

    public void verificarExistenciaVariablesYFunciones() {
        // Aquí iría la logica para verificar la existencia de variables y funciones
    }

    public void controlDeAlcance() {
        // Aquí iría la logica para el control de alcance
    }

}
