package com.example.article.Admin;

import com.mongodb.*;
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

public class AdminLoginController {

    @FXML
    public TextField adminEmailField;
    @FXML
    private TextField adminIdField;

    @FXML
    private PasswordField adminPasswordField;

    @FXML
    private Label adminMessageLabel;

    private MongoCollection<Document> adminCollection;

    public AdminLoginController() {
        try {
            // Initialize MongoDB connection
            String connectionString = "mongodb://localhost:27017";
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .serverApi(serverApi)
                    .build();
            // Create a new client and connect to the server
            MongoClient mongoClient = MongoClients.create(settings);

            // Access the database and Admin collection
            MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
            adminCollection = database.getCollection("Admin");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing MongoDB connection: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdminLogin(ActionEvent actionEvent) {
        String adminId = adminIdField.getText();
        String adminPassword = adminPasswordField.getText();
        String adminEmail = adminEmailField.getText();

        // Check for empty fields
        if (adminId.isEmpty() || adminPassword.isEmpty()) {
            adminMessageLabel.setText("Please enter both Admin ID and Password.");
            return;
        }

        // Validate adminId as email format
        if (!isValidEmail(adminEmail)) {
            adminMessageLabel.setText("Invalid email format. Please enter a valid email.");
            return;
        }

        // Fetch admin document from MongoDB
        Document admin = adminCollection.find(new Document("adminId", adminId)).first();

        if (admin != null && admin.getString("password").equals(adminPassword)) {
            adminMessageLabel.setText("Login successful!");
            redirectToAdminDashboard(actionEvent); // Redirect to Admin Dashboard
        } else {
            adminMessageLabel.setText("Invalid Admin ID or Password.");
        }
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailRegex);
    }


    @FXML
    private void handleBack(ActionEvent actionEvent) {
        try {
            // Load the Welcome Page FXML file
            Parent welcomeRoot = FXMLLoader.load(getClass().getResource("/com/example/article/WelcomePage.fxml"));
            Scene welcomeScene = new Scene(welcomeRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(welcomeScene);
            stage.setTitle("Welcome Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            adminMessageLabel.setText("Error loading Welcome Page.");
        }
    }

    private void redirectToAdminDashboard(ActionEvent actionEvent) {
        try {
            // Load the Admin Dashboard FXML file
            Parent adminDashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminDashboard.fxml"));
            Scene adminDashboardScene = new Scene(adminDashboardRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(adminDashboardScene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            adminMessageLabel.setText("Error loading Admin Dashboard.");
        }
    }
}
