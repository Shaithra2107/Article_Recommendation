package com.example.article.Admin;

import com.example.article.Article.ArticleCategorizer;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

public class ManageArticle {

    // Add Articles Section
    @FXML
    public TextField txtArticleID;
    @FXML
    public TextField txtArticleName;
    @FXML
    public TextField txtDate;
    @FXML
    public TextArea txtDescription;
    @FXML
    public TextField txtTags;
    @FXML
    public TextField txtURL;

    // Edit Articles Section
    @FXML
    public TextField txtArticleTitle;

    private MongoClient mongoClient;  // Declare MongoClient
    private MongoDatabase database;   // Declare MongoDatabase
    private MongoCollection<Document> articleCollection;

    @FXML
    public Button btnFetchDetails;
    @FXML
    public Button btnSaveChanges;
    @FXML
    public Button btnCancel;

    public ManageArticle() {
        // Initialize MongoDB connection
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        mongoClient = MongoClients.create(settings);  // Initialize MongoClient

        // Access database and collection
        database = mongoClient.getDatabase("News_Recommendation");  // Get the database
        articleCollection = database.getCollection("News");  // Get the collection
    }

    public void handleSave(ActionEvent actionEvent) {
        // Retrieve input values from UI components
        String articleId = txtArticleID.getText();
        String articleName = txtArticleName.getText();
        String date = txtDate.getText();
        String description = txtDescription.getText();
        String tags = txtTags.getText();
        String url = txtURL.getText();

        // Validate fields
        if (articleName.isEmpty() || date.isEmpty() || description.isEmpty() || tags.isEmpty() || url.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields except Article ID are required!");
            return;
        }

        // Check if the article already exists in the database based on articleID
        Document filter = new Document("articleID", articleId);
        Document existingArticle = articleCollection.find(filter).first();

        if (existingArticle != null) {
            // If an article with the same articleID exists, show an error
            showAlert(Alert.AlertType.ERROR, "Error", "An article with this ID already exists. Please use a different Article ID.");
            return;
        }

        // Perform the save operation asynchronously
        new Thread(() -> {
            try {
                // Prepare article document
                Document article = new Document()
                        .append("articleID", articleId)
                        .append("title", articleName)
                        .append("date", date)
                        .append("keywords", tags)
                        .append("link", url)
                        .append("summary", description);

                // Insert the article into the main "News" collection
                articleCollection.insertOne(article);

                // Categorize the article using the ArticleCategorizer
                String category = ArticleCategorizer.categorizeArticle(
                        List.of(tags.split(",")), articleName, description);  // Split tags into a list

                // Save to the relevant category collection
                MongoCollection<Document> categoryCollection = database.getCollection(category.replace(" ", "_"));
                categoryCollection.insertOne(article);  // Save to the category-specific collection

                // Update the UI in the JavaFX thread
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Article added successfully!");
                    clearFields();
                });

            } catch (Exception e) {
                // Update the UI in case of an error
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the article: " + e.getMessage()));
            }
        }).start();  // Start the thread to run the database operations
    }




    public void handleBack(ActionEvent actionEvent) {
        try {
            // Load the Admin Dashboard FXML file
            Parent adminRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminDashboard.fxml"));
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

    public void handleSaveChanges(ActionEvent actionEvent) {
        // Retrieve input values
        String articleId = txtArticleID.getText();
        String updatedTitle = txtArticleTitle.getText();
        String updatedDate = txtDate.getText();
        String updatedDescription = txtDescription.getText();
        String updatedTags = txtTags.getText();
        String updatedURL = txtURL.getText();

        // Validate input fields
        if (articleId.isEmpty() || updatedTitle.isEmpty() || updatedDate.isEmpty() || updatedDescription.isEmpty() || updatedTags.isEmpty() || updatedURL.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required for editing!");
            return;
        }

        try {
            // Re-categorize the article based on the updated content
            String newCategory = ArticleCategorizer.categorizeArticle(
                    List.of(updatedTags.split(",")), updatedTitle, updatedDescription);

            Document filter = new Document("articleID", articleId);

            // Fetch the old article to determine the current category
            Document oldArticle = articleCollection.find(filter).first();
            if (oldArticle == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Article not found!");
                return;
            }

            // Prepare the updated article document
            Document updatedArticle = new Document("articleID", articleId)
                    .append("title", updatedTitle)
                    .append("date", updatedDate)
                    .append("keywords", updatedTags)
                    .append("link", updatedURL)
                    .append("summary", updatedDescription);

            // Update the article in the main "News" collection
            articleCollection.replaceOne(filter, updatedArticle);

            // Identify the old category
            String oldCategory = ArticleCategorizer.categorizeArticle(
                    List.of(oldArticle.getString("keywords").split(",")),
                    oldArticle.getString("title"),
                    oldArticle.getString("summary"));

            if (!oldCategory.equals(newCategory)) {
                // If the category has changed, move the article to the new category

                // Remove from the old category collection
                MongoCollection<Document> oldCategoryCollection = database.getCollection(oldCategory.replace(" ", "_"));
                oldCategoryCollection.deleteOne(filter);

                // Add to the new category collection
                MongoCollection<Document> newCategoryCollection = database.getCollection(newCategory.replace(" ", "_"));
                newCategoryCollection.insertOne(updatedArticle);

                System.out.println("Article moved from " + oldCategory + " to " + newCategory);
            } else {
                // If the category has not changed, update it in the same category collection
                MongoCollection<Document> categoryCollection = database.getCollection(oldCategory.replace(" ", "_"));
                categoryCollection.replaceOne(filter, updatedArticle);
                System.out.println("Article updated in the same category: " + oldCategory);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Article updated successfully!");
            clearField();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update the article: " + e.getMessage());
        }
    }



    private void clearFields() {
        txtArticleID.clear();
        txtArticleName.clear();
        txtDate.clear();
        txtDescription.clear();
        txtTags.clear();
        txtURL.clear();
    }

    private void clearField() {
        txtArticleID.clear();
        txtArticleTitle.clear();
        txtDate.clear();
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

    public void handleCancelEdit(ActionEvent actionEvent) {
        try {
            // Load the Admin Dashboard FXML file
            Parent adminRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminDashboard.fxml"));
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

    public void handleFetchDetails(ActionEvent actionEvent) {
        System.out.println("txtArticleID is null: " + (txtArticleID == null));

        String articleId = txtArticleID.getText();

        if (articleId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter an Article ID.");
            return;
        }

        try {
            Document filter = new Document("articleID", articleId);
            Document article = articleCollection.find(filter).first();

            if (article != null) {
                txtArticleTitle.setText(article.getString("title"));
                txtDate.setText(article.getString("date"));
                txtDescription.setText(article.getString("summary"));
                txtTags.setText(article.getString("keywords"));
                txtURL.setText(article.getString("link"));
            } else {
                showAlert(Alert.AlertType.WARNING, "Not Found", "No article found with the given Article ID.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print error details
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to fetch the article: " + e.getMessage());
        }
    }
}
