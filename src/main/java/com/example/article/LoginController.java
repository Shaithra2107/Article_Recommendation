package com.example.article;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.bson.Document;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final MongoCollection<Document> userCollection;

    public LoginController() {
        MongoDatabase database = MongoDBUtil.getDatabase();
        userCollection = database.getCollection("Users");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Document user = userCollection.find(new Document("username", username)).first();

        if (user != null && user.getString("password").equals(password)) {
            messageLabel.setText("Login successful!");
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void handleSignUp() {
        // Switch to Sign Up screen (implement scene change logic here)
        System.out.println("Redirecting to Sign Up screen...");
    }
}
