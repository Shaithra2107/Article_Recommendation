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
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class AdminDashboard {

    // Declare TextField for articleID input
    @FXML
    private TextField txtArticleID;

    private MongoClient mongoClient;
    private MongoDatabase database;  // MongoDatabase object for connecting to MongoDB
    private MongoCollection<Document> articleCollection;  // MongoDB's collection for articles

    public AdminDashboard() {
        // Initialize MongoDB connection
        // MongoDB setup (similar to how it's done in ManageArticle class)
        ConnectionString connectionString = new ConnectionString("mongodb://127.0.0.1:27017");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);

        // Access the database and collection
        database = mongoClient.getDatabase("News_Recommendation");
        articleCollection = database.getCollection("News");
    }

    // Method to handle article deletion
    public void handleDeleteArticle(ActionEvent actionEvent) {
        String articleId = txtArticleID.getText();  // Get article ID from the TextField

        // Validate input (make sure articleID is not empty)
        if (articleId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter an Article ID to delete.");
            return;
        }

        try {
            // Search for the article by its ID
            Document filter = new Document("articleID", articleId);
            Document article = articleCollection.find(filter).first();

            if (article != null) {
                // Article found, proceed with deletion
                articleCollection.deleteOne(filter);  // Delete the article from the collection
                showAlert(Alert.AlertType.INFORMATION, "Success", "Article deleted successfully!");

                // Optionally, clear the text field after deletion
                txtArticleID.clear();
            } else {
                // Article not found
                showAlert(Alert.AlertType.WARNING, "Not Found", "No article found with the given Article ID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the article: " + e.getMessage());
        }
    }

    // Method to show alerts to the user
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Navigate to EditArticle.fxml for editing articles
    public void handleEditArticle(ActionEvent actionEvent) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("EditArticle.fxml"));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Edit Article");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigate to AddArticle.fxml for adding new articles
    public void handleAddArticle(ActionEvent actionEvent) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("AddArticle.fxml"));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Add Article");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Navigate back to AdminLogin.fxml
    public void handleBack(ActionEvent actionEvent) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("AdminLogin.fxml"));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
