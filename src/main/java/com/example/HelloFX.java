package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main JavaFX application class for YadaChat.
 * Sets up the primary stage, loads the FXML view, applies the stylesheet, and shows the window.
 */

public class HelloFX extends Application {


    /**
     * Called when the JavaFX application is launched.
     * Loads the FXML layout, sets up the scene, applies the CSS stylesheet, and displays the stage.
     *
     * @param stage the primary stage for this application
     * @throws Exception if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 768, 576);
        stage.setTitle("YadaChat");

        scene.getStylesheets().add(Objects.requireNonNull(HelloFX.class.getResource("style.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();


    }

    /**
     * Main method, launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }

}