package com.example.analizador_lexico;

public class Simbolos {
    public boolean isSimbolo(String palabra){
        switch (palabra){
            case "+",
                    "-",
                    "*",
                    "/",
                    "%",
                    "=",
                    "+=",
                    "-=",
                    "*=",
                    "/=",
                    "%=",
                    "==",
                    "!=",
                    ">",
                    "<",
                    ">=",
                    "<=",
                    "++",
                    "--",
                    "(",
                    ")",
                    "{",
                    "}",
                    "[",
                    "]",
                    ".",
                    ",",
                    ";",
                    "'",
                    "\"",
                    ":",
                    "|",
                    "&",
                    "||",
                    "&&": return true;
            default: return false;
        }
    }


}
