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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class AdminLoginController {

    @FXML
    private TextField adminIdField;

    @FXML
    private PasswordField adminPasswordField;

    @FXML
    private Label adminMessageLabel;

    private final MongoCollection<Document> adminCollection;

    public AdminLoginController() {
        // Initialize MongoDB connection
        ConnectionString connectionString = new ConnectionString("mongodb://127.0.0.1:27017");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        // Access the database and Admin collection
        MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
        adminCollection = database.getCollection("Admin");
    }

    @FXML
    private void handleAdminLogin(ActionEvent actionEvent) {
        String adminId = adminIdField.getText();
        String adminPassword = adminPasswordField.getText();

        if (adminId.isEmpty() || adminPassword.isEmpty()) {
            adminMessageLabel.setText("Please enter both Admin ID and Password.");
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
            adminMessageLabel.setText("Error loading Welcome Page.");
        }
    }

    private void redirectToAdminDashboard(ActionEvent actionEvent) {
        try {
            // Load the Admin Dashboard FXML file
            Parent adminDashboardRoot = FXMLLoader.load(getClass().getResource("AdminDashboard.fxml"));
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
