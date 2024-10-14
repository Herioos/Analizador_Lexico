package com.example.analizador_lexico;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    private Button btnCompilar;

    @FXML
    private Button btnFile;

    @FXML
    private TableColumn<Analisis, String> tblColumnColumna;

    @FXML
    private TableColumn<Analisis, String> tblColumnExpresion;

    @FXML
    private TableColumn<Analisis, String> tblColumnRenglon;

    @FXML
    private TableColumn<Analisis, String> tblColumnTipo;

    @FXML
    private TableView<Analisis> tblResultado;

    @FXML
    private TextArea txtCode;

    @FXML
    private TextField txtFile;

    int saltos_palabra;
    String texto_error = "";
    ObservableList<String> lista_tokens = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Manejar el evento de teclado cuando se presiona Enter
        txtCode.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                txtCode.appendText("\n");
                event.consume();
            }
        });
        tblColumnExpresion.setCellValueFactory(new PropertyValueFactory<>("expresion"));
        tblColumnTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tblColumnRenglon.setCellValueFactory(new PropertyValueFactory<>("renglon"));
        tblColumnColumna.setCellValueFactory(new PropertyValueFactory<>("columna"));
    }

    public void compilar() {
        tblResultado.getItems().clear();
        // Declaración de variables
        Palabra_Reservada palabraReservada = new Palabra_Reservada();
        Excepcion excepcion = new Excepcion();
        Simbolos simbolos = new Simbolos();
        String tipoLexema, next_palabra;
        boolean isAsignando = false;
        boolean isString = false;
        boolean isClassName = false;
        String codigo = txtCode.getText();

        // Separación del código de entrada en palabras y símbolos
        String[] listaPalabras = codigo.split("(?<=\\W)|(?=\\W)");
        /*
        "?<=\\W": Verifica que un determinado caracter esté precedido por un símbolo.
        "?=\\W": Verifica que el siguiente caracter en una posición sea un símbolo.
        * Si el caracter analizado es un símbolo, también lo considera como palabra en la función .split
         */

        // Contadores de líneas y columnas
        int contColumna = 1;
        int contRenglon = 1;

        /* CLASIFICACIÓN DE PALABRAS */
        for (int i = 0; i < listaPalabras.length; i++) {
            if (listaPalabras[i].equals("\n")) {
                contRenglon++;
                contColumna = 0;
            }
            // Evita las palabras en blanco
            if (listaPalabras[i].matches("^\\s") || listaPalabras[i].matches("\t")) {
                contColumna++;
            } else {
                // Agarra la palabra
                String palabra = listaPalabras[i];
                if (isClassName) {
                    isClassName = false;
                    tipoLexema = "Clase";
                    tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += palabra.length();
                    continue;

                }
                if (isString) {
                    isString = false;
                    tipoLexema = "Símbolo";
                    tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += palabra.length();
                    continue;
                }
                if (palabra.equals("\"")) {
                    isString = true;
                    int saltos = 1;
                    String auxiliar = "";
                    tipoLexema = "Símbolo";
                    tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += palabra.length();

                    try {
                        while (!listaPalabras[i + saltos].equals("\"")) {
                            if (listaPalabras[i + saltos].equals("\n")) {
                                texto_error = "Línea " + contRenglon + ", Columna " + contColumna + "\n" + "Se esperaba un cierre de comillas.";
                                Alert alerta = new Alert(Alert.AlertType.ERROR);
                                alerta.setHeaderText("ERROR.\nError de sintaxis.");
                                alerta.setContentText(texto_error);
                                alerta.showAndWait();
                                return;
                            }

                            auxiliar = auxiliar.concat(listaPalabras[i + saltos]);
                            saltos++;
                        }
                    } catch (Exception e) {
                        Alert alerta = new Alert(Alert.AlertType.ERROR);
                        alerta.setHeaderText("FATAL ERROR.\nError de sintaxis.");
                        alerta.setContentText("Se esperaba un cierre de comillas");
                        alerta.showAndWait();
                        return;
                    }

                    tipoLexema = "Valor de cadena";
                    tblResultado.getItems().add(new Analisis(auxiliar, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += auxiliar.length();
                    i += saltos - 1;
                    continue;
                }
                // Evaluación de símbolos dobles (++,!=,...)
                switch (palabra) {
                    case "+": {  //++, +=
                        if (listaPalabras[i + 1].equals("+") || listaPalabras[i + 1].equals("=")) {
                            palabra = palabra.concat(listaPalabras[i + 1]);
                            i++;
                        }
                        break;
                    }
                    case "-": {  //--, -=
                        if (listaPalabras[i + 1].equals("-") || listaPalabras[i + 1].equals("=")) {
                            palabra = palabra.concat(listaPalabras[i + 1]);
                            i++;
                        }
                        break;
                    }
                    case "*", "/", "%", "=", "!", ">", "<": {  // Todos los demás símbolos dobles...
                        if (listaPalabras[i + 1].equals("=")) {
                            palabra = palabra.concat(listaPalabras[i + 1]);
                            i++;
                        }
                        break;
                    }
                    case "|": {
                        if (listaPalabras[i + 1].equals("|")) {
                            palabra = palabra.concat(listaPalabras[i + 1]);
                            i++;
                        }
                        break;
                    }
                    case "&": {
                        if (listaPalabras[i + 1].equals("&")) {
                            palabra = palabra.concat(listaPalabras[i + 1]);
                            i++;
                        }
                        break;
                    }
                }
                // Evalúa si la palabra es palabra reservada
                if (palabraReservada.isPalabraReservada(palabra)) {
                    tipoLexema = "Palabra Reservada";
                    tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += palabra.length();

                    // Verifica si la palabra reservada corresponde a una clase
                    if (palabra.equals("class")) {
                        isClassName = true;
                    }
                    continue;

                }

                // Evalúa si la palabra es el nombre de un tipo de excepción
                if(excepcion.isException(palabra)){
                    tipoLexema = "Clase";
                    tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += palabra.length();
                    continue;
                }

                // Evalúa si la palabra es un símbolo
                else if (simbolos.isSimbolo(palabra)) {
                    tipoLexema = "Símbolo";
                    tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                    contColumna += palabra.length();
                    continue;
                } else {
                    // Evalúa si la palabra es un número
                    if (palabra.matches("[0-9]+")) {
                        try {
                            if (listaPalabras[i + 1].equals(".")) {
                                palabra = palabra.concat(listaPalabras[i + 1]);
                                if (listaPalabras[i + 2].matches("[0-9]+")) {
                                    palabra = palabra.concat(listaPalabras[i + 2]);
                                    i++;
                                }
                                i++;
                            }
                        } catch (Exception e) {
                        }
                        tipoLexema = "Valor";
                    } else {
                        // Evalúa si la palabra es una función
                        int j = 1;
                        while ((listaPalabras[i + j].matches("^\\s") || listaPalabras[i + j].matches("\t"))) {
                            j++;
                        }
                        next_palabra = listaPalabras[i + j];
                        if (next_palabra.equals("(")) {
                            tipoLexema = "Función";
                            tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                            contColumna += palabra.length();
                            continue;
                        }
                        //
                        tipoLexema = "Variable";
                    }
                }
                // Agrega el registro de la palabra a la tabla
                tblResultado.getItems().add(new Analisis(palabra, tipoLexema, String.valueOf(contRenglon), String.valueOf(contColumna)));
                contColumna += palabra.length();
            }
        }
        List<Analisis> lista_expresiones = tblResultado.getItems();
        estructura(lista_expresiones);
        if (!texto_error.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("ERROR.\nEstructura de código incorrecta.");
            alerta.setContentText(texto_error);
            alerta.showAndWait();
        }
        verificacion_semantica(lista_expresiones);
    }

    public void verificacion_semantica(List<Analisis> lista_expresiones) {
        String expresion, tipo;
        String token = "";
        lista_tokens.clear();
        boolean error_found;
        boolean inSwitch = false;
        saltos_palabra = 0;

        // Recorriendo cada palabra de la tabla de expresiones
        for (int i = 0; i < lista_expresiones.size(); i++) {
            // Obtención de expresión y tipo de expresión
            expresion = lista_expresiones.get(i).getExpresion();
            tipo = lista_expresiones.get(i).getTipo();
            System.out.println("Evaluando expresión: "+expresion);

            if(!lista_tokens.isEmpty()){
                // Verifica que el último elemento de la lista de tokens sea "switch"
                if(lista_tokens.get(lista_tokens.size()-1).equals("switch") && expresion.equals("case")){
                    inSwitch = true;
                }
                else{
                    inSwitch = false;
                }
            }

            if(inSwitch){
                error_found = reviewCase(lista_expresiones, i);
                if (error_found) {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setHeaderText("ERROR.\nExiste un error en la declaración del Switch.");
                    alerta.setContentText(texto_error);
                    alerta.showAndWait();
                }
                i = saltos_palabra;
                token = "";
                continue;
            }

            if (expresion.equals("break")){
                lista_tokens.remove(lista_tokens.size()-1);
                i += 1;
                continue;
            }

            // Revisar valor del token para completar expresiones
            switch (token) {
                case "if": {
                    System.out.println("La expresion después del cierre del if es: "+expresion);
                    if (expresion.equals("else")) {
                        error_found = reviewElse(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración del Else.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        }
                        i = saltos_palabra;
                        token = "";
                        continue;
                    }
                    break;
                }
                case "do": {
                    System.out.println("La expresion después del cierre del do es: " + expresion);
                    if (expresion.equals("while")) {
                        error_found = reviewDoWhile(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración del While.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        }
                        i = saltos_palabra;
                        token = "";
                        continue;
                    } else {
                        Alert alerta = new Alert(Alert.AlertType.ERROR);
                        alerta.setHeaderText("ERROR.\nSe esperaba una cláusula While después del Do.");
                        alerta.setContentText(texto_error);
                        alerta.showAndWait();
                    }
                    break;
                }

                case "try": {
                    System.out.println("La expresión después del cierre del Try es: " + expresion);
                    if (expresion.equals("catch")) {
                        error_found = reviewCatch(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración del Catch.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        }
                        i = saltos_palabra;
                        token = "";
                        continue;
                    } else {
                        Alert alerta = new Alert(Alert.AlertType.ERROR);
                        alerta.setHeaderText("ERROR.\nSe esperaba una cláusula Catch después del Try.");
                        alerta.setContentText(texto_error);
                        alerta.showAndWait();
                    }
                    break;
                }
            }

            // Detección de palabra reservada
            if (tipo.equals("Palabra Reservada")) {
                switch (expresion) {
                    // Evaluación semántica ciclo for
                    case "for": {
                        error_found = reviewFor(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración del ciclo.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("El ciclo For está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra - 1;
                        break;
                    }

                    case "System": {
                        error_found = verificarSystemOutPrint(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración de la función");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La función System.out.print() está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra -1;
                        break;
                    }

                    case "if": {
                        error_found = reviewIf(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración de la estructura.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La estructura condicional If  está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra;
                        break;
                    }

                    case "while":{
                        error_found = reviewWhile(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración de la estructura.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La estructura While está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra - 1;
                        break;
                    }

                    case "do":{
                        error_found = reviewDo(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración de la estructura.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La estructura Do está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra - 1;
                        break;
                    }

                    case "try":{
                        error_found = reviewTry(lista_expresiones, i);
                        if (error_found) {
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración de la estructura.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La estructura Try está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra;
                        break;
                    }

                    case "switch":{
                        error_found = reviewSwitch(lista_expresiones, i);
                        if(error_found){
                            Alert alerta = new Alert(Alert.AlertType.ERROR);
                            alerta.setHeaderText("ERROR.\nExiste un error en la declaración de la estructura.");
                            alerta.setContentText(texto_error);
                            alerta.showAndWait();
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La estructura Switch está bien.");
                            alerta.showAndWait();
                        }
                        i = saltos_palabra;
                        break;
                    }
                }
            }
            if (expresion.equals("}")) {
                try{
                    token = lista_tokens.get(lista_tokens.size() - 1);
                    lista_tokens.remove(lista_tokens.size() - 1);
                    if (inSwitch){
                        lista_tokens.remove(lista_tokens.size() - 1);
                    }
                } catch (Exception e){}
            }
        }
    }

    public boolean verificarSystemOutPrint(List<Analisis> lista_expresiones, int i) {
        String expresion_cadena;
        int estado = 0; // Definimos una variable estado cuyo valor inicial es cero y va a ir cambiando segun el comportamiento del análisis
        texto_error = ""; // Mensaje de error que sera empleada si la instruccion es incorrecta sintacticamente

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            switch (estado) {
                case 0: {
                    // Revisión de la palabra reservada "System"
                    if (expresion.equals("System")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    // Revisión del punto '.'
                    if (expresion.equals(".")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un punto para instanciar un atributo de la clase System.";
                    }
                    break;
                }

                case 2: {
                    // Revisión de la expresión 'out'
                    if (expresion.equals("out")) {
                        estado = 3;
                    } else {
                        estado = -1;
                        texto_error = "Atributo " + expresion + " desconocido.";
                    }
                    break;
                }

                case 3: {
                    // Revisión del punto '.'
                    if (expresion.equals(".")) {
                        estado = 4;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un punto para instanciar un método del atributo 'out'.";
                    }
                    break;
                }

                case 4: {
                    // Revisión de la función 'print' o 'println'
                    if (expresion.matches("(print|println)")) {
                        estado = 5;
                    } else {
                        estado = -1;
                        texto_error = "Método " + expresion + " desconocido";
                    }
                    break;
                }

                case 5: {
                    // Revisión de apertura de paréntesis '('
                    if (expresion.equals("(")) {
                        estado = 6;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una apertura de paréntesis '('";
                    }
                    break;
                }

                case 6: {
                    /* REVISIÓN DE EXPRESIÓN A IMPRIMIR (Valor de cadena o Variable) */
                    // Verificación de variable
                    if (tipo_expresion.equals("Variable")) {
                        estado = 7;
                        break;
                        // Verificación de valor de cadena
                    } else if (expresion.equals("\"")) {
                        // Concatenar las comillas dobles con el valor para formar la cadena
                        expresion_cadena = expresion + lista_expresiones.get(j + 1).getExpresion() + lista_expresiones.get(j + 2).getExpresion();
                        if (expresion_cadena.matches("\".*\"")) {
                            estado = 7;
                            j += 2;   // Salta a la siguiente expresión después del valor de cadena
                            break;
                        }
                        // Revisión de paréntesis de cierre ')'
                    } else if (expresion.equals(")")) {
                        estado = 9;
                        break;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                        break;
                    }
                }

                case 7: {
                    // Revisión de operador '+' para concatenar cadenas
                    if (expresion.equals("+")) {
                        estado = 8;
                        break;
                    }
                    // Revisión de paréntesis de cierre ')'
                    if (expresion.equals(")")) {
                        estado = 9;
                        break;
                    }
                    estado = -1;
                    texto_error = "Se esperaba un operador de concatenación '+' o un paréntesis de cierre ')'.";
                    break;
                }

                case 8: {
                    /* REVISIÓN DE EXPRESIÓN CONCATENADA A IMPRIMIR (Valor de cadena o Variable) */
                    // Verificación de variable
                    if (tipo_expresion.equals("Variable")) {
                        estado = 7;
                        // Verificación de valor de cadena
                    } else if (expresion.equals("\"")) {
                        // Concatenar las comillas dobles con el valor para formar la cadena
                        expresion_cadena = expresion + lista_expresiones.get(j + 1).getExpresion() + lista_expresiones.get(j + 2).getExpresion();
                        if (expresion_cadena.matches("\".*\"")) {
                            estado = 7;
                            j += 2; // Salta a la siguiente expresión después del valor de cadena
                        }
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }

                case 9: {
                    if (expresion.equals(";")) {
                        estado = 10;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un punto y coma ';'.";
                    }
                    break;
                }
                case 10: {
                    // Estado final correcto
                    System.out.println("La función System.out.println() está correcta.");
                    saltos_palabra = j;
                    return false;
                }
                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración de la función System.out.println().");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        if (estado != 10) {
            texto_error = "Función System.out.print() incompleta.";
            return true;
        }
        return false;
    }

    public boolean reviewFor(List<Analisis> lista_expresiones, int i) {
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis del ciclo

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO DEL CICLO FOR */
            switch (estado) {
                case 0: {
                    // Revisión de la palabra reservada "for"
                    if (expresion.equals("for")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }
                case 1: {
                    // Revisión del paréntesis de apertura después de la palabra "for"
                    if (expresion.equals("(")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de apertura '('.";
                    }
                    break;
                }
                /* 1. SENTENCIA DE DECLARACIÓN */
                case 2: {
                    // Revisión del tipo de variable a declarar (int, float o double)
                    if (expresion.matches("^(int|float|double|char)")) {
                        estado = 3;
                    } else {
                        if (tipo_expresion.equals("Variable")) {
                            estado = 4;
                        } else {
                            estado = -1;
                            texto_error = "Tipo de dato " + expresion + " no válido en la declaración.";
                        }
                    }
                    break;
                }
                case 3: {
                    // Revisión del nombre de la variable a declarar
                    if (tipo_expresion.equals("Variable")) {
                        estado = 4;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba nombre de variable.";
                    }
                    break;
                }
                case 4: {
                    // Revisión del operador de asignación '='
                    if (expresion.equals("=")) {
                        estado = 5;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba operador de asignación '='.";
                    }
                    break;
                }
                case 5: {
                    // Revisión de la expresión después del operador de asignación (Valor numérico o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 6;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 6: {
                    // Revisión del punto y coma para terminar la sentencia de declaración
                    if (expresion.equals(";")) {
                        estado = 7;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba ';'";
                    }
                    break;
                }

                /* 2. SENTENCIA DE CONDICIÓN */
                case 7: {
                    // Revisión de la primera expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 8;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 8: {
                    // Revisión del operador relacional
                    if (expresion.matches("(!=|==|<|<=|>|>=)")) {
                        estado = 9;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un operador de comparación válido.";
                    }
                    break;
                }
                case 9: {
                    // Revisión de la segunda expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 10;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 10: {
                    // Revisión del punto y coma para terminar la sentencia de condición
                    if (expresion.equals(";")) {
                        estado = 11;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba ';'.";
                    }
                    break;
                }

                /* 3. SENTENCIA DE SALTO */
                case 11: {
                    // Revisión del nombre de la variable de salto
                    if (tipo_expresion.equals("Variable")) {
                        estado = 12;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba nombre de variable.";
                    }
                    break;
                }
                case 12: {
                    /* OPERADORES VÁLIDOS DESPUÉS DE VARIABLE DE SALTO */
                    // Revisión de símbolos ++ o --
                    if (expresion.matches("(\\+\\+|--)")) {
                        estado = 13;
                    }
                    // Revisión de operador de asignación '='
                    else {
                        if (expresion.matches("=")) {
                            estado = 14;
                        }
                        // Revisión de otros operadores de asignación válidos
                        else {
                            if (expresion.matches("(\\+=|-=|\\*=|/=)")) {
                                estado = 16;
                            } else {
                                estado = -1;
                                texto_error = "Se esperaba un operador válido.";
                            }
                        }
                    }
                    break;
                }
                case 13: {
                    // Revisión de paréntesis de cierre ')'
                    if (expresion.equals(")")) {
                        estado = 17;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba cierre de paréntesis ')'.";
                    }
                    break;
                }
                case 14: {
                    // Revisión de expresión después del operador de asignación '=' (Variable o valor numérico)
                    if (tipo_expresion.matches("Variable") | tipo_expresion.equals("Valor")) {
                        estado = 15;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba nombre de variable.";
                    }
                    break;
                }
                case 15: {
                    // Revisión de operador aritmético después de la expresión
                    if (expresion.matches("(\\+|-|\\*|/)")) {
                        estado = 16;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un operador válido.";
                    }
                    break;
                }
                case 16: {
                    // Revisión de expresión después del operador aritmético
                    if (tipo_expresion.equals("Variable") | tipo_expresion.equals("Valor")) {
                        estado = 13;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 17: {
                    // Revisión de llave de apertura para introducir el bloque de código
                    if (expresion.equals("{")) {
                        estado = 18;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }
                case 18: {
                    // Estado final correcto
                    System.out.println("El ciclo For está bien.");
                    saltos_palabra = j;
                    lista_tokens.add("for");
                    return false;
                }
                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración del ciclo.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        // Analiza si el ciclo for se declaró en su totalidad
        if (estado != 18) {
            texto_error = "La declaración del ciclo For está incompleta";
            return true;
        }
        return false;
    }

    public boolean reviewIf(List<Analisis> lista_expresiones, int i) {
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis del ciclo

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA IF */
            switch (estado) {
                case 0: {
                    if (expresion.equals("if")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    // Revisión del paréntesis de apertura después de la palabra "for"
                    if (expresion.equals("(")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de apertura '('.";
                    }
                    break;
                }

                case 2: {
                    // Revisión de la primera expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 3;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 3: {
                    // Revisión del operador relacional
                    if (expresion.matches("(!=|==|<|<=|>|>=)")) {
                        estado = 4;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un operador de comparación válido.";
                    }
                    break;
                }
                case 4: {
                    // Revisión de la segunda expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 5;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }

                case 5: {
                    // Revisión de un operador lógico de expresiones ('||' o '&&')
                    if (expresion.equals("||") || expresion.equals("&&")) {
                        estado = 2;
                    } else if (expresion.equals(")")) {
                        estado = 6;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de cierre.";
                    }
                    break;
                }

                case 6: {
                    // Revisión de apertura de llave
                    if (expresion.equals("{")) {
                        estado = 7;
                        saltos_palabra = j;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }

                case 7: {
                    // Estado final correcto
                    System.out.println("La estructura condicional If está bien.");
                    lista_tokens.add("if");
                    return false;
                }

                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración del ciclo.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reviewWhile(List<Analisis> lista_expresiones, int i) {
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis del ciclo

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA IF */
            switch (estado) {
                case 0: {
                    if (expresion.equals("while")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    // Revisión del paréntesis de apertura después de la palabra "while"
                    if (expresion.equals("(")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de apertura '('.";
                    }
                    break;
                }

                case 2: {
                    // Revisión de la primera expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 3;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 3: {
                    // Revisión del operador relacional
                    if (expresion.matches("(!=|==|<|<=|>|>=)")) {
                        estado = 4;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un operador de comparación válido.";
                    }
                    break;
                }
                case 4: {
                    // Revisión de la segunda expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 5;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }

                case 5: {
                    // Revisión de un operador lógico de expresiones ('||' o '&&')
                    if (expresion.equals("||") || expresion.equals("&&")) {
                        estado = 2;
                    } else if (expresion.equals(")")) {
                        estado = 6;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de cierre.";
                    }
                    break;
                }

                case 6: {
                    // Revisión de apertura de llave
                    if (expresion.equals("{")) {
                        estado = 7;
                        saltos_palabra = j;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }

                case 7: {
                    // Estado final correcto
                    System.out.println("La estructura condicional While está bien.");
                    lista_tokens.add("while");
                    return false;
                }

                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración del ciclo.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reviewElse(List<Analisis> lista_expresiones, int i) {
        texto_error = "";
        int estado = 0;
        boolean error_found = false;

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA ELSE */
            switch (estado) {
                case 0: {
                    if (expresion.equals("else")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    if (expresion.equals("if")) {
                        error_found = reviewIf(lista_expresiones, j);
                        if (error_found) {
                            estado = -1;
                        } else {
                            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                            alerta.setHeaderText("La estructura condicional Else-If está bien.");
                            alerta.showAndWait();
                            lista_tokens.add("if");
                            estado = 2;
                        }
                    } else if (expresion.equals("{")) {
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setHeaderText("La estructura condicional Else está bien.");
                        alerta.showAndWait();
                        lista_tokens.add("else");
                        saltos_palabra = j;
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }
                case 2:{
                    return false;
                }
                case -1:{
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la estructura Else.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reviewDo(List<Analisis> lista_expresiones, int i){
        texto_error = "";
        int estado = 0;

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA ELSE */
            switch (estado) {
                case 0: {
                    if (expresion.equals("do")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    if (expresion.equals("{")) {
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setHeaderText("La estructura Do está bien.");
                        alerta.showAndWait();
                        lista_tokens.add("do");
                        saltos_palabra = j;
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }
                case 2:{
                    return false;
                }
                case -1:{
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la estructura Do.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reviewDoWhile(List<Analisis> lista_expresiones, int i){
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis del ciclo

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA IF */
            switch (estado) {
                case 0: {
                    if (expresion.equals("while")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    // Revisión del paréntesis de apertura después de la palabra "while"
                    if (expresion.equals("(")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de apertura '('.";
                    }
                    break;
                }

                case 2: {
                    // Revisión de la primera expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 3;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 3: {
                    // Revisión del operador relacional
                    if (expresion.matches("(!=|==|<|<=|>|>=)")) {
                        estado = 4;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un operador de comparación válido.";
                    }
                    break;
                }
                case 4: {
                    // Revisión de la segunda expresión en la condición de tope (Valor o variable)
                    if (tipo_expresion.equals("Valor") || tipo_expresion.equals("Variable")) {
                        estado = 5;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }

                case 5: {
                    // Revisión de un operador lógico de expresiones ('||' o '&&')
                    if (expresion.equals("||") || expresion.equals("&&")) {
                        estado = 2;
                    } else if (expresion.equals(")")) {
                        estado = 6;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de cierre.";
                    }
                    break;
                }

                case 6: {
                    // Revisión de apertura de llave
                    if (expresion.equals(";")) {
                        estado = 7;
                        saltos_palabra = j;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un punto y coma.";
                    }
                    break;
                }

                case 7: {
                    // Estado final correcto
                    System.out.println("La estructura condicional Do-While está bien.");
                    return false;
                }

                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración del ciclo.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reviewTry(List<Analisis> lista_expresiones, int i){
        texto_error = "";
        int estado = 0;

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA ELSE */
            switch (estado) {
                case 0: {
                    if (expresion.equals("try")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    if (expresion.equals("{")) {
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setHeaderText("La estructura Try está bien.");
                        alerta.showAndWait();
                        lista_tokens.add("try");
                        saltos_palabra = j;
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }
                case 2:{
                    return false;
                }
                case -1:{
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la estructura Try.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reviewCatch(List<Analisis> lista_expresiones, int i){
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis del ciclo
        Excepcion excepcion = new Excepcion();

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO ESTRUCTURA CATCH */
            switch (estado) {
                case 0: {
                    if (expresion.equals("catch")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }

                case 1: {
                    // Revisión del paréntesis de apertura después de la palabra "catch"
                    if (expresion.equals("(")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de apertura '('.";
                    }
                    break;
                }

                case 2: {
                    // Revisión del tipo de excepción a capturar
                    if (excepcion.isException(expresion)) {
                        estado = 3;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba el nombre de una clase de excepción.";
                    }
                    break;
                }
                case 3: {
                    // Revisión de nombre de variable para identificar la excepción
                    if (tipo_expresion.equals("Variable")) {
                        estado = 4;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba el nombre de un identificador para la excepción capturada.";
                    }
                    break;
                }
                case 4: {
                    // Revisión de cierre de paréntesis
                    if (expresion.equals(")")) {
                        estado = 5;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un cierre de paréntesis.";
                    }
                    break;
                }

                case 5: {
                    // Revisión de apertura de llave para el bloque de código
                    if (expresion.equals("{")) {
                        estado = 6;
                        lista_tokens.add("catch");
                        saltos_palabra = j;

                    }
                    else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura.";
                    }
                    break;
                }

                case 6: {
                    // Estado final correcto
                    System.out.println("La estructura Catch está bien.");
                    return false;
                }

                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración de la estructura Catch.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }
    public boolean reviewSwitch(List<Analisis> lista_expresiones, int i){
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis del ciclo

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO DEL CICLO SWITCH */
            switch (estado) {
                case 0: {
                    // Revisión de la palabra reservada "switch"
                    if (expresion.equals("switch")) {
                        estado = 1;
                    } else {
                        estado = -1;
                    }
                    break;
                }
                case 1: {
                    // Revisión del paréntesis de apertura después de la declaración del switch
                    if (expresion.equals("(")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de apertura '('.";
                    }
                    break;
                }

                case 2: {
                    // Revisión del valor o variable a evaluar dentro del switch
                    if (tipo_expresion.equals("Variable") || tipo_expresion.equals("Valor")){
                        estado = 3;
                    } else{
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 3: {
                    // Revisión del cierre de paréntesis
                    if (expresion.equals(")")){
                        estado = 4;
                    }
                    else{
                        estado = -1;
                        texto_error = "Se esperaba un paréntesis de cierre.";
                    }
                    break;
                }
                case 4: {
                    // Revisión de llave de apertura
                    if (expresion.equals("{")) {
                        estado = 5;
                        lista_tokens.add("switch");
                        saltos_palabra = j;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba una llave de apertura";
                    }
                    break;
                }
                case 5: {
                    // Estado final correcto
                    System.out.println("La estructura Switch está bien.");
                    return false;
                }

                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración de la estructura switch.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }
    public boolean reviewCase(List<Analisis> lista_expresiones, int i){
        texto_error = "";
        int estado = 0; // Auxiliar numérico para el análisis de la estructura
        String palabra = "";

        /* Recorrido de la lista de expresiones */
        for (int j = i; j < lista_expresiones.size(); j++) {
            /* Obtención de la expresión y su tipo (Reservada, Símbolo, Variable, etc.)*/
            String expresion = lista_expresiones.get(j).getExpresion();
            String tipo_expresion = lista_expresiones.get(j).getTipo();

            /* ANÁLISIS SINTÁCTICO DEL CICLO SWITCH */
            switch (estado) {
                case 0: {
                    // Revisión de palabra reservada "case" o "default"
                    if (expresion.equals("case")) {
                        estado = 1;
                    } else if (expresion.equals("default")){
                        palabra = "default";
                        estado = 2;
                    } else {
                        estado = -1;
                    }
                    break;
                }
                case 1: {
                    // Revisión de valor del caso (Constante)
                    if (tipo_expresion.equals("Valor")) {
                        estado = 2;
                    } else {
                        estado = -1;
                        texto_error = "Se esperaba un valor constante para evaluar el caso.";
                    }
                    break;
                }

                case 2: {
                    // Revisión de coma ',' después del valor del caso o dos puntos ':'
                    if (expresion.equals(",")){
                        estado = 1;
                    } else if (expresion.equals(":")){
                        estado = 3;
                        if (!palabra.equals("default")){
                            lista_tokens.add("case");
                        }
                        saltos_palabra = j;
                    } else{
                        estado = -1;
                        texto_error = "Se esperaba una expresión válida.";
                    }
                    break;
                }
                case 3: {
                    // Estado final correcto
                    System.out.println("La estructura Case está bien.");
                    return false;
                }
                case -1: {
                    // Caso de error (Generación de mensaje de error)
                    String numLinea = lista_expresiones.get(j - 1).getRenglon();
                    String numColumna = lista_expresiones.get(j - 1).getColumna();

                    texto_error = "Línea " + numLinea + ", Columna " + numColumna + "\n" + texto_error;
                    System.out.println("ERROR.\nExiste un error en la declaración de la estructura Case.");
                    saltos_palabra = j;
                    return true;
                }
            }
        }
        return false;
    }

    // CONTADOR DE LLAVES Y PARÉNTESIS
    public void estructura(List<Analisis> lista_expresiones) {
        texto_error = "";
        int i = 0; // Lleva el conteo de las llaves abiertas
        int j = 0; // Leva el conteo de llaves cerradas
        int k = 0; // Lleva el contero de parentesis abiertos
        int l = 0, c = 0; // Lleva el contero de parentesis cerrados
        for (Analisis etiqueta : lista_expresiones) { // Recorre los elementos de la lista guasda para comprobar los elemento
            switch (etiqueta.getExpresion()) {
                case "{":
                    i++;
                    break;
                case "}":
                    j++;
                    break;
                case "(":
                    k++;
                    break;
                case ")":
                    l++;
                    break;

            }
        }
        if (i < j) { //Evalua si la cantidad de { es menor que las de }, entonces hay un error en la abertura de estas
            texto_error = "Error en abertura de llaves";
        } else if (i > j) {
            texto_error = "Error en cerrado de llaves";
        } //En caso contrario se evalua si la cantidad de { es mayor que las de }, entonces hay un error con el cerrado de estas
        if (k < l) { //Evalua si la cantidad de ( es menor que las de ), entonces hay un error en la abertura de estas
            texto_error = "Error en abertura de parentesis";
        } else if (k > l) {
            texto_error = "Error en cerrado de parentesis";
        } //En caso contrario se evalua si la cantidad de ( es mayor que las de ), entonces hay un error con el cerrado de estas
        //  c++;
    }

}


