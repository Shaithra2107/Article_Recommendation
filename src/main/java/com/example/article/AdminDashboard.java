package com.example.article;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboard {
    public void handleDeleteArticle(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("AdminLogin.fxml"));
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

    public void handleEditArticle(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("EditArticle.fxml"));
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

    public void handleAddArticle(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("AddArticle.fxml"));
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

    public void handleBack(ActionEvent actionEvent) {
        try {
            // Load the SignUp FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("AdminLogin.fxml"));
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
}
