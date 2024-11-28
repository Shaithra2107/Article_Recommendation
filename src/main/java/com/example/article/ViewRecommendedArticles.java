package com.example.article;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class ViewRecommendedArticles implements Initializable {

    @FXML
    public Label categoryLabel;
    @FXML
    public TableView<Article> recommendedArticlesTable;
    @FXML
    public TableColumn<Article, String> articleIdColumn;
    @FXML
    public TableColumn<Article, String> titleColumn;
    @FXML
    public TableColumn<Article, String> dateColumn;
    @FXML
    public TableColumn<Article, String> descriptionColumn;
    @FXML
    public TableColumn<Article, String> urlColumn;
    @FXML
    private TableColumn<Article, Void> actionsColumn;

    private MongoCollection<Document> ratingsCollection;

    // ExecutorService for concurrency
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up TableView columns
        articleIdColumn.setCellValueFactory(new PropertyValueFactory<>("articleId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // URL column setup
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlColumn.setCellFactory(col -> new TableCell<Article, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    setOnMouseClicked(event -> openUrlInBrowser(item));
                } else {
                    setText(null);
                    setStyle(null);
                }
            }
        });

        // Actions column setup
        actionsColumn.setCellFactory(param -> new TableCell<Article, Void>() {
            private final Button btn = new Button("Rate");

            {
                btn.setOnAction(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    handleRateArticle(article);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Load recommendations concurrently
        loadRecommendedArticlesConcurrently();
    }

    void loadRecommendedArticlesConcurrently() {
        try {
            String userId = User.getLoggedInUserId();
            ObservableList<Article> recommendations = FXCollections.observableArrayList();

            // Check if the user is new
            boolean isNewUser = isNewUser(userId);

            // Create tasks for fetching articles
            List<Callable<List<Article>>> tasks = new ArrayList<>();
            if (isNewUser) {
                // Fetch articles for a new user (6 articles from different categories)
                tasks.add(() -> getArticlesForNewUser());
            } else {
                // Fetch articles for returning user (based on ratings and preferences)
                tasks.add(() -> getArticlesForLowRatedCategories(userId));
                tasks.add(() -> getArticlesForPreferredCategories(userId));
            }

            // Execute tasks concurrently
            List<Future<List<Article>>> results = executorService.invokeAll(tasks);

            // Aggregate results and limit to 6 articles per category for new user
            for (Future<List<Article>> future : results) {
                recommendations.addAll(future.get());
            }

            // Remove duplicates if any (just in case)
            Set<Article> uniqueArticles = new HashSet<>(recommendations);
            recommendations.setAll(uniqueArticles);

            // Set data to the TableView
            recommendedArticlesTable.setItems(recommendations);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error loading recommendations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Article> getArticlesForNewUser() {
        System.out.println("Fetching articles for a new user...");
        return getRandomArticlesFromAllCategories();
    }

    private List<Article> getArticlesForLowRatedCategories(String userId) {
        System.out.println("Fetching articles excluding low-rated categories for user: " + userId);
        Map<String, Integer> lowRatedCounts = categorizeUserRatings(userId, false);
        return getArticlesFromCategoriesExcluding(lowRatedCounts.keySet());
    }

    private List<Article> getArticlesForPreferredCategories(String userId) {
        System.out.println("Fetching articles for user's preferred categories: " + userId);
        Map<String, Integer> highRatedCounts = categorizeUserRatings(userId, true);
        return getArticlesFromPreferredCategories(highRatedCounts);
    }

    private List<Article> getRandomArticlesFromAllCategories() {
        List<Article> articles = new ArrayList<>();
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            MongoCollection<Document> collection = getMongoCollection(category);
            try (MongoCursor<Document> cursor = collection.aggregate(List.of(new Document("$sample", new Document("size", 1)))).iterator()) {
                while (cursor.hasNext()) {
                    articles.add(createArticleFromDocument(cursor.next()));
                }
            }
        }
        return articles;
    }

    private List<Article> getArticlesFromCategoriesExcluding(Set<String> excludedCategories) {
        List<Article> articles = new ArrayList<>();
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            if (!excludedCategories.contains(category)) {
                MongoCollection<Document> collection = getMongoCollection(category);
                try (MongoCursor<Document> cursor = collection.find().limit(6).iterator()) {
                    while (cursor.hasNext()) {
                        articles.add(createArticleFromDocument(cursor.next()));
                    }
                }
            }
        }
        return articles;
    }

    private List<Article> getArticlesFromPreferredCategories(Map<String, Integer> preferredCategories) {
        List<Article> articles = new ArrayList<>();

        for (String category : preferredCategories.keySet()) {
            MongoCollection<Document> collection = getMongoCollection(category);
            try (MongoCursor<Document> cursor = collection.find().limit(6).iterator()) {
                while (cursor.hasNext()) {
                    articles.add(createArticleFromDocument(cursor.next()));
                }
            }
        }
        return articles;
    }

    private Article createArticleFromDocument(Document doc) {
        return new Article(
                doc.getString("articleID"),
                doc.getString("title"),
                doc.getString("date"),
                doc.getString("summary"),
                doc.getString("link")
        );
    }

    private String getArticleCategory(String articleId) {
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            MongoCollection<Document> collection = getMongoCollection(category);
            Document doc = collection.find(Filters.eq("articleID", articleId)).first();
            if (doc != null) {
                return category;
            }
        }
        return null;
    }

    private MongoCollection<Document> getMongoCollection(String collectionName) {
        try {
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("News_Recommendation");
            return database.getCollection(collectionName);
        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            return null;
        }
    }

    private void openUrlInBrowser(String url) {
        HelloApplication.getHostServicesInstance().showDocument(url);
    }

    public void handleRateArticle(Article article) {
        int rating = showRatingDialog();
        if (rating > 0 && rating <= 5) {
            updateArticleRatingInDatabase(article.getArticleId(), rating);
            article.setRating(rating);  // Update rating in the table model
            recommendedArticlesTable.refresh(); // Refresh the table view to reflect the new rating
        } else {
            showInvalidRatingAlert();
        }
    }

    private int showRatingDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rate Article");
        dialog.setHeaderText("Enter rating (1 to 5):");
        dialog.setContentText("Rating:");

        Optional<String> result = dialog.showAndWait();
        return result.map(rating -> {
            try {
                return Integer.parseInt(rating);
            } catch (NumberFormatException e) {
                return -1;
            }
        }).orElse(-1);
    }

    private void showInvalidRatingAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Rating");
        alert.setHeaderText("Rating must be between 1 and 5.");
        alert.showAndWait();
    }

    private void updateArticleRatingInDatabase(String articleId, int rating) {
        MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");

        // Insert the rating into the ratings collection
        Bson filter = Filters.and(Filters.eq("userId", User.getLoggedInUserId()), Filters.eq("articleId", articleId));
        Bson update = Updates.set("rating", rating);

        ratingsCollection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    private boolean isNewUser(String userId) {
        MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");
        Document userRating = ratingsCollection.find(Filters.eq("userId", userId)).first();
        return userRating == null;  // If no ratings exist for the user, they are new
    }

    private Map<String, Integer> categorizeUserRatings(String userId, boolean highRated) {
        MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");
        Map<String, Integer> categoryCounts = new HashMap<>();

        // Query ratings by the user
        try (MongoCursor<Document> cursor = ratingsCollection.find(Filters.eq("userId", userId)).iterator()) {
            while (cursor.hasNext()) {
                Document ratingDoc = cursor.next();
                String articleId = ratingDoc.getString("articleId");
                int rating = ratingDoc.getInteger("rating", 0);
                String category = getArticleCategory(articleId);

                if (category != null) {
                    if (highRated ? rating >= 4 : rating <= 2) {
                        categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
                    }
                }
            }
        }
        return categoryCounts;
    }

    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("DashBoard.fxml"));
            Scene dashboardScene = new Scene(dashboardRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(dashboardScene);
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
