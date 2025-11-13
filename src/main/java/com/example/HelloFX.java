package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 768, 576);
        stage.setTitle("YadaYada");
        stage.setScene(scene);
        stage.show();

        scene.getStylesheets().add(Objects.requireNonNull(HelloFX.class.getResource("style.css")).toExternalForm());
    }

    public static void main(String[] args) {
        launch();
    }

}