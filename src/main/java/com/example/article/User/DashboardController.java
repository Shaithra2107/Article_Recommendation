package com.example.article.User;

import com.example.article.App.User;
import com.example.article.DB.DatabaseHelper;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class DashboardController {

    // MongoDB setup
    private static final String CONNECTION_STRING = "mongodb+srv://shaithra20232694:123shaithra@cluster0.cwjpj.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"; // Update this if necessary
    private static final String DATABASE_NAME = "News_Recommendation";
    private static final String COLLECTION_NAME = "News";
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
    @FXML
    public Button manageProfileButton;
    @FXML
    public Button getRecommendationsButton;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articleCollection;

    // Constructor to initialize MongoDB client and connection
    public DashboardController() {
        //Initialize MongoDB connection
        // MongoDB setup (similar to how it's done in ManageArticle class)
        // Initialize MongoDB connection
        // Access the database and News collection
        // Instantiate DatabaseHelper with connection string
        try{DatabaseHelper dbHelper = new DatabaseHelper("mongodb://localhost:27017");

        // Fetch the "ratings" collection from the database
        MongoCollection<Document> collection = dbHelper.getCollection("News");}
        catch (Exception e) {
            e.printStackTrace();
            showAlert("Error initializing database connection: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

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
            Parent adminRoot = FXMLLoader.load(getClass().getResource("/com/example/article/ViewNews.fxml"));
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

    public void handleManageProfile(ActionEvent actionEvent) {
        try {
            // Load the Welcome Page FXML file
            Parent welcomeRoot = FXMLLoader.load(getClass().getResource("/com/example/article/ManageProfile.fxml"));
            Scene welcomeScene = new Scene(welcomeRoot);

            // Get the current stage (window) and set the new scene
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(welcomeScene);
            stage.setTitle("Welcome Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGetRecommendations(ActionEvent event) {
        try {
            // Load the Recommendations.fxml view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/article/Recommendations.fxml"));
            Parent root = loader.load();

            // Get the user ID of the logged-in user
            String userId = User.getLoggedInUserId();

            // Pass the user ID to the recommendations controller
            ViewRecommendedArticles controller = loader.getController();
            controller.loadRecommendedArticlesConcurrently(); // This method now handles the recommendation loading

            // Switch to the recommendations scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recommended Articles");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void handleLogout(ActionEvent actionEvent) {
        try {
            // Get the logged-in user's ID
            String userId = User.getLoggedInUserId();

            // Ensure the user ID is valid before proceeding
            if (userId == null || userId.isEmpty()) {
                showAlert("No user is logged in.", Alert.AlertType.WARNING);
                return;
            }

            // Access the Users collection
            MongoCollection<Document> userCollection = database.getCollection("Users");

            // Delete the user's record from the database
            userCollection.deleteOne(new Document("_id", new org.bson.types.ObjectId(userId)));

            // Clear the logged-in user state in the application
            User.setLoggedInUserId(null);

            // Redirect to the login page
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/article/Login.fxml"));
            Scene loginScene = new Scene(loginRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An error occurred during logout. Please try again.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }


    public void handleBack(ActionEvent actionEvent) {
        try {
            // Load the Welcome Page FXML file
            Parent welcomeRoot = FXMLLoader.load(getClass().getResource("/com/example/article/Login.fxml"));
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
}
