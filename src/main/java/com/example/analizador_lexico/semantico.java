package com.example.analizador_lexico;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

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
        verificarTipos(lista_expresiones); // Llama a verificarTipos
    }

    private void verificarTipos(ObservableList<Analisis> lista_expresiones) {
        for (int i = 0; i < lista_expresiones.size(); i++) {
            Analisis analisis = lista_expresiones.get(i);
            String expresion = analisis.getExpresion();
            String tipo = analisis.getTipo();

            // Verifica si es una palabra reservada y si es una declaración de variable
            if (new Palabra_Reservada().isPalabraReservada(expresion)) {
                // Esperamos que la siguiente expresión sea el nombre de la variable
                if (i + 1 < lista_expresiones.size() && lista_expresiones.get(i + 1).getTipo().equals("Variable")) {
                    // siguiente elemento  de asignación
                    if (i + 2 < lista_expresiones.size() && lista_expresiones.get(i + 2).getExpresion().equals("=")) {
                        // siguiente elemento  valor o una variable
                        if (i + 3 < lista_expresiones.size()) {
                            String valor = lista_expresiones.get(i + 3).getExpresion();
                            String tipoVariable = expresion; // La palabra reservada (ejemplo: "int")

                            // Verifica si el valor es compatible con el tipo de variable o si es otra variable
                            if (!esValorCompatible(tipoVariable, valor) && !lista_expresiones.get(i + 3).getTipo().equals("Variable")) {
                                mostrarAlerta("Error Semántico",
                                        "Asignación inválida: " + tipoVariable + " " + lista_expresiones.get(i + 1).getExpresion() + " = " + valor);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean esValorCompatible(String tipoVariable, String valor) {
        switch (tipoVariable) {
            case "int":
                return valor.matches("\\d+"); // Solo números enteros
            case "double":
                return valor.matches("\\d+\\.\\d+"); // Números con decimales
            case "float":
                return valor.matches("\\d+\\.\\d+f"); // Números con decimales terminados en 'f'
            case "char":
                return valor.matches("'.'"); // Un solo carácter entre comillas simples
            case "boolean":
                return valor.equals("true") || valor.equals("false"); // Solo true o false
            case "String":
                return valor.matches("\".*\""); // Cadenas entre comillas dobles
            // Agregar más tipos según sea necesario
            default:
                return false; // Tipo no reconocido
        }
    }


   // Codigo para mostrar alerta de error semantico
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
