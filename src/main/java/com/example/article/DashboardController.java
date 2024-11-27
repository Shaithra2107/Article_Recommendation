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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class DashboardController {

    // MongoDB setup
    private static final String CONNECTION_STRING = "mongodb://localhost:27017"; // Update this if necessary
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
        ConnectionString connectionString = new ConnectionString("mongodb://127.0.0.1:27017");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);  // Initialize MongoClient

        // Access database and collection
        database = mongoClient.getDatabase("News_Recommendation");  // Get the database
        articleCollection = database.getCollection("News");  // Get the collection
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
            Parent adminRoot = FXMLLoader.load(getClass().getResource("ViewNews.fxml"));
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
    }

    @FXML
    private void handleGetRecommendations(ActionEvent event) {
        try {
            // Load the Recommendations.fxml view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Recommendations.fxml"));
            Parent root = loader.load();

            // Get the user ID of the logged-in user
            String userId = User.getLoggedInUserId();

            // Pass the user ID to the recommendations controller
            ViewRecommendedArticles controller = loader.getController();
            controller.initializeRecommendations(userId); // This method will fetch and display recommendations

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
    }
}