package com.example.analizador_lexico;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("AnalizadorView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("¡Analizador Léxico!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        launch();
        int i = 2;
        int j = 0;

        switch(i) {
            case 0:
                System.out.println("i es cero.");
                break;
            case 1:
                System.out.println("i es uno.");
                break;
            case 2:
                System.out.println("i es dos.");
                break;
            case 3:
                System.out.println("i es tres.");
                break;
            default:
                System.out.println("i es mayor a tres.");
        }
       }
    }