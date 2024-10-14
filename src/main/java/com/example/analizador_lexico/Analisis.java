package com.example.analizador_lexico;

public class Analisis {
    private String expresion;
    private String tipo;
    private String renglon;
    private String columna;

    public Analisis(String expresion, String tipo, String renglon, String columna) {
        this.expresion = expresion;
        this.tipo = tipo;
        this.renglon = renglon;
        this.columna = columna;
    }

    public String getExpresion() {
        return expresion;
    }

    public void setExpresion(String expresion) {
        this.expresion = expresion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRenglon() {
        return renglon;
    }

    public void setRenglon(String renglon) {
        this.renglon = renglon;
    }

    public String getColumna() {
        return columna;
    }

    public void setColumna(String columna) {
        this.columna = columna;
    }
}
