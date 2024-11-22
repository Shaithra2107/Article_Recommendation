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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ManageArticle {

    // Add Articles Section
    @FXML
    public TextField txtArticled;
    @FXML
    public TextField txtArticleName;
    @FXML
    public TextField txtAuthor;
    @FXML
    public TextArea txtDescription;
    @FXML
    public TextField txtTags;
    @FXML
    public TextField txtURL;

    // Edit Articles Section
    @FXML
    public TextField txtTitle;
    @FXML
    public TextField txtArticleid;

    private final MongoCollection<Document> articleCollection;


    public ManageArticle() {
        // Initialize MongoDB connection
        ConnectionString connectionString = new ConnectionString("mongodb://127.0.0.1:27017");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        // Access database and collection
        MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
        articleCollection = database.getCollection("Articles");
    }

    public void handleSave(ActionEvent actionEvent) {
        // Retrieve input values
        String articleId = txtArticled.getText(); // Custom articleId field
        String articleName = txtArticleName.getText();
        String author = txtAuthor.getText();
        String description = txtDescription.getText();
        String tags = txtTags.getText();
        String url = txtURL.getText();

        // Validate input fields
        if (articleId.isEmpty() || articleName.isEmpty() || author.isEmpty() || description.isEmpty() || tags.isEmpty() || url.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required!");
            return;
        }

        // Prepare the article document with a custom articleId
        Document article = new Document("_id", articleId) // Adding the custom articleId
                .append("title", articleName)
                .append("author", author)
                .append("description", description)
                .append("tags", tags)
                .append("url", url);

        // Insert the article into the database
        try {
            articleCollection.insertOne(article);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Article added successfully!");

            // Clear input fields after saving
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the article: " + e.getMessage());
        }
    }


    public void handleBack(ActionEvent actionEvent) {
        try {
            // Load the Admin Dashboard FXML file
            Parent adminRoot = FXMLLoader.load(getClass().getResource("AdminDashboard.fxml"));
            Scene adminScene = new Scene(adminRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(adminScene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Edit Articles Section

    // Rest of your methods remain unchanged...

    public void handleSaveChanges(ActionEvent actionEvent) {
        // Retrieve input values
        String articleId = txtArticleid.getText();
        String updatedTitle = txtTitle.getText();
        String updatedAuthor = txtAuthor.getText();
        String updatedDescription = txtDescription.getText();
        String updatedTags = txtTags.getText();
        String updatedURL = txtURL.getText();

        // Validate input fields
        if (articleId.isEmpty() || updatedTitle.isEmpty() || updatedAuthor.isEmpty() || updatedDescription.isEmpty() || updatedTags.isEmpty() || updatedURL.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required for editing!");
            return;
        }

        try {
            // Prepare the updated document
            Document updatedArticle = new Document("title", updatedTitle)
                    .append("author", updatedAuthor)
                    .append("description", updatedDescription)
                    .append("tags", updatedTags)
                    .append("url", updatedURL);

            // Update the document in the database
            Document filter = new Document("_id", articleId);
            Document update = new Document("$set", updatedArticle);

            long updatedCount = articleCollection.updateOne(filter, update).getModifiedCount();

            if (updatedCount > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Article updated successfully!");
                clearFields();
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "No article found with the given ID!");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Article ID: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the article: " + e.getMessage());
        }
    }

    public void handleCancelEdit(ActionEvent actionEvent) {
        try {
            // Load the Admin Dashboard FXML file
            Parent adminRoot = FXMLLoader.load(getClass().getResource("AdminDashboard.fxml"));
            Scene adminScene = new Scene(adminRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(adminScene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to fetch details based on Article ID
    public void handleFetchDetails(ActionEvent actionEvent) {
        String articleId = txtArticleid.getText();

        // Validate input
        if (articleId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter an Article ID.");
            return;
        }

        try {
            // Query the database using the provided articleId
            Document filter = new Document("_id", articleId);
            Document article = articleCollection.find(filter).first();

            if (article != null) {
                // Populate fields with the retrieved article details
                txtTitle.setText(article.getString("title"));
                txtAuthor.setText(article.getString("author"));
                txtDescription.setText(article.getString("description"));
                txtTags.setText(article.getString("tags"));
                txtURL.setText(article.getString("url"));
            } else {
                // Show a warning if no article is found
                showAlert(Alert.AlertType.WARNING, "Not Found", "No article found with the given ID.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to fetch the article: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtArticleName.clear();
        txtAuthor.clear();
        txtDescription.clear();
        txtTags.clear();
        txtURL.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
