package com.example.article;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    // Method to handle navigation to the Sign-Up page
    public void handleSignUp(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
            Scene signUpScene = new Scene(signUpRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to handle navigation to the Login page
    public void handleLogin(ActionEvent actionEvent) {
        try {
            // Load the Login FXML file
            Parent loginRoot = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Scene loginScene = new Scene(loginRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
