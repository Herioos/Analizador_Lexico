package com.example.analizador_lexico;

public class Excepcion {
    public boolean isException(String palabra){
        switch (palabra){
            case "ArithmeticException",
                    "ArrayIndexOutOfBoundsException",
                    "ArrayStoreException",
                    "ClassCastException",
                    "IllegalArgumentException",
                    "IllegalStateException",
                    "NullPointerException",
                    "NumberFormatException",
                    "IndexOutOfBoundsException",
                    "NegativeArraySizeException",
                    "UnsupportedOperationException",
                    "SecurityException",
                    "StringIndexOutOfBoundsException",
                    "ClassNotFoundException",
                    "InstantiationException",
                    "InterruptedException",
                    "IOException",
                    "FileNotFoundException",
                    "EOFException",
                    "MalformedURLException",
                    "CloneNotSupportedException",
                    "NoSuchFieldException",
                    "NoSuchMethodException",
                    "InputMismatchException",
                    "Exception": return true;
            default: return false;
        }
    }
}
