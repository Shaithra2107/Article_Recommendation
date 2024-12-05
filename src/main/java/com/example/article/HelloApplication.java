package com.example.article;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.HostServices;

import java.io.IOException;

public class HelloApplication extends Application {

    private static HostServices hostServices; // To store HostServices instance

    @Override
    public void start(Stage stage) throws IOException {
        // Store HostServices instance
        hostServices = getHostServices();

        // Load the FXML file (WelcomePage.fxml in this case)
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/article/WelcomePage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 630, 550);

        // Set up the stage (window)
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    // Static method to access HostServices instance
    public static HostServices getHostServicesInstance() {
        return hostServices;
    }

    public static void main(String[] args) {
        launch();
    }
}
