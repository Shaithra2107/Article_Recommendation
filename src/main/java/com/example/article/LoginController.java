package com.example.article;

import com.example.article.App.User;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final MongoCollection<Document> userCollection;

    public LoginController() {
        // Initialize MongoDB connection
        ConnectionString connectionString = new ConnectionString("mongodb+srv://shaithra20232694:123shaithra@cluster0.cwjpj.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        // Access database and collection
        MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
        userCollection = database.getCollection("Users");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        // Fetch user from MongoDB
        Document user = userCollection.find(new Document("username", username)).first();

        if (user != null && user.getString("password").equals(password)) {
            // Set the logged-in user ID
            String userId = user.getObjectId("_id").toString(); // Assuming MongoDB ID is used
            User.setLoggedInUserId(userId);

            messageLabel.setText("Login successful!");
            redirectToDashboard(); // Redirect to the dashboard
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void handleSignUp(ActionEvent actionEvent) {
        try {
            // Load the Sign Up FXML file
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
            Scene signUpScene = new Scene(signUpRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading Sign Up page.");
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
            messageLabel.setText("Error loading Welcome Page.");
        }
    }

    private void redirectToDashboard() {
        try {
            // Load the Dashboard FXML file
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("DashBoard.fxml"));
            Scene dashboardScene = new Scene(dashboardRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading Dashboard.");
        }
    }
}
