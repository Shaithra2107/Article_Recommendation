package com.example.article.Admin;

import com.example.article.DB.DatabaseHelper;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
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
        // Initialize MongoDB connection
        // Access the database and Admin collection
        // Instantiate DatabaseHelper with connection string
        DatabaseHelper dbHelper = new DatabaseHelper("mongodb://localhost:27017");

        // Fetch the "ratings" collection from the database
        MongoCollection<Document> collection = dbHelper.getCollection("News");


    }

    // Method to handle article deletion
    public void handleDeleteArticle(ActionEvent actionEvent) {
        String articleId = txtArticleID.getText(); // Get article ID from the TextField

        // Validate input (make sure articleID is not empty)
        if (articleId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter an Article ID to delete.");
            return;
        }

        try {
            // Delete from the main 'News' collection
            Document filter = new Document("articleID", articleId);
            Document article = articleCollection.find(filter).first();

            if (article != null) {
                // Article found, delete it from the 'News' collection
                articleCollection.deleteOne(filter);

                // Fetch all category collections
                for (String category : database.listCollectionNames()) {
                    if (!category.equals("News")) { // Skip the main 'News' collection
                        MongoCollection<Document> categoryCollection = database.getCollection(category);

                        // Delete the article from the category collection if it exists
                        categoryCollection.deleteOne(filter);
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Article deleted successfully from all collections!");

                // Optionally, clear the text field after deletion
                txtArticleID.clear();
            } else {
                // Article not found in the 'News' collection
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
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/EditArticle.fxml"));
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
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AddArticle.fxml"));
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
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminLogin.fxml"));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Admin Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleViewArticle(ActionEvent actionEvent) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminView.fxml"));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Edit Article");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //Admin view articles code

    @FXML
    public Label welcomeLabel;
    @FXML
    public TilePane categoryPane;
    @FXML
    public ImageView businessIcon;
    @FXML
    public ImageView techIcon;
    @FXML
    public ImageView healthIcon;
    @FXML
    public ImageView geoIcon;
    @FXML
    public ImageView sportsIcon;
    @FXML
    public ImageView otherIcon;

    private static String selectedCategory = "";



    // Method to return the collection
    public MongoCollection<Document> getCollection() {
        return articleCollection;
    }

    // Method to return the database
    public MongoDatabase getDatabase() {
        return database;
    }

    // Method to get the selected category
    public static String getCategory() {
        return selectedCategory;
    }

    // Navigate to the ViewNews page for a specific category
    private void navigateToViewNews(String category) {
        selectedCategory = category;  // Set the selected category
        try {
            Parent adminRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminViewNews.fxml"));
            Scene adminScene = new Scene(adminRoot);
            Stage stage = (Stage) categoryPane.getScene().getWindow();
            stage.setScene(adminScene);
            stage.setTitle(category);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Business(ActionEvent actionEvent) {
        navigateToViewNews("Business_and_Economy");
    }

    public void Technology(ActionEvent actionEvent) {
        navigateToViewNews("Technology");
    }

    public void politics(ActionEvent actionEvent) {
        navigateToViewNews("Geopolitics_and_Regional_Focus");
    }

    public void Health(ActionEvent actionEvent) {
        navigateToViewNews("Health_and_Pandemic");
    }

    public void Sports(ActionEvent actionEvent) {
        navigateToViewNews("Sports_and_Competition");
    }

    public void other(ActionEvent actionEvent) {
        navigateToViewNews("Others");
    }


    //to get back to the dashboard
    public void handleBackTo(ActionEvent actionEvent) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminDashboard.fxml"));
            Scene signUpScene = new Scene(signUpRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(signUpScene);
            stage.setTitle("Edit Article");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
