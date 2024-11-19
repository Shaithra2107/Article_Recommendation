package com.example.article;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.bson.Document;

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

    private final MongoCollection<Document> userCollection;

    public SignUp() {
        MongoDatabase database = MongoDBUtil.getDatabase();
        userCollection = database.getCollection("Users");
    }

    @FXML
    private void handleSignUp() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String dob = (dobField.getValue() != null) ? dobField.getValue().toString() : "";
        String phone = phoneField.getText();
        String gender = maleRadioButton.isSelected() ? "Male" :
                femaleRadioButton.isSelected() ? "Female" : "Other";

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match!");
            return;
        }

        Document existingUser = userCollection.find(new Document("username", username)).first();
        if (existingUser != null) {
            messageLabel.setText("Username already exists!");
            return;
        }

        Document newUser = new Document("fullName", fullName)
                .append("email", email)
                .append("username", username)
                .append("password", password)
                .append("dob", dob)
                .append("phone", phone)
                .append("gender", gender);

        userCollection.insertOne(newUser);
        messageLabel.setText("Account created successfully!");
    }

    @FXML
    private void handleLogin() {
        // Switch to Login screen (implement scene change logic here)
        System.out.println("Redirecting to Login screen...");
    }
}
