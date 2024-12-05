package com.example.article;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {


    public void handleAdmin(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminLogin.fxml"));
            Scene signUpScene = new Scene(signUpRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleUser(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/Login.fxml"));
            Scene signUpScene = new Scene(signUpRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
