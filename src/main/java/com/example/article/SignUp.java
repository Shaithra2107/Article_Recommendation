package com.example.article;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

public class SignUp {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private DatePicker dobField;
    @FXML
    private TextField phoneField;
    @FXML
    private RadioButton maleRadioButton;
    @FXML
    private RadioButton femaleRadioButton;
    @FXML
    private RadioButton otherRadioButton;
    @FXML
    private Label messageLabel;
    private ToggleGroup genderField;

    @FXML
    public void initialize() {
        genderField = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderField);
        femaleRadioButton.setToggleGroup(genderField);
        otherRadioButton.setToggleGroup(genderField);
    }


    @FXML
    private void handleSignUp() {
        // Validate inputs
        if (fullNameField.getText().isEmpty() || emailField.getText().isEmpty() || usernameField.getText().isEmpty()
                || passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()
                || dobField.getValue() == null || genderField.getSelectedToggle() == null) {
            showAlert("Please fill in all fields.", Alert.AlertType.WARNING);
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Passwords do not match.", Alert.AlertType.WARNING);
            return;
        }

        String selectedGender = genderField.getSelectedToggle() == maleRadioButton ? "Male"
                : genderField.getSelectedToggle() == femaleRadioButton ? "Female" : "Other";

        try {
            // Set up MongoDB connection
            ConnectionString connectionString = new ConnectionString("mongodb://127.0.0.1:27017");
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);

            MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
            MongoCollection<Document> collection = database.getCollection("Users");

            // Check for duplicate username or email
            Document duplicateCheck = collection.find(new Document("$or", List.of(
                    new Document("username", usernameField.getText()),
                    new Document("email", emailField.getText())
            ))).first();

            if (duplicateCheck != null) {
                if (duplicateCheck.getString("username").equals(usernameField.getText())) {
                    showAlert("This username is already taken. Please choose another.", Alert.AlertType.WARNING);
                } else {
                    showAlert("This email is already registered. Please use another.", Alert.AlertType.WARNING);
                }
                return;
            }

            // Prepare user data
            Document user = new Document("fullName", fullNameField.getText())
                    .append("email", emailField.getText())
                    .append("username", usernameField.getText())
                    .append("password", passwordField.getText()) // TODO: Hash passwords in production!
                    .append("dateOfBirth", dobField.getValue().toString())
                    .append("gender", selectedGender)
                    .append("phoneNumber", phoneField.getText());

            // Insert into MongoDB
            collection.insertOne(user);

            showAlert("Account created successfully!", Alert.AlertType.INFORMATION);
            resetForm();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to save data to the database. Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleLogin(ActionEvent actionEvent) {
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
            showAlert("Error loading Login page.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack(ActionEvent actionEvent) {
        try {
            // Load the Welcome Page FXML file
            Parent welcomeRoot = FXMLLoader.load(getClass().getResource("WelcomePage.fxml"));
            Scene welcomeScene = new Scene(welcomeRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(welcomeScene);
            stage.setTitle("Welcome Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading Welcome Page.", Alert.AlertType.ERROR);
        }
    }

    private void resetForm() {
        fullNameField.clear();
        emailField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        dobField.setValue(null);
        phoneField.clear();
        genderField.selectToggle(null);
    }

    // Method to show alert messages
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }
}
