package com.example.article.User;

import com.example.article.App.Article;
import com.example.article.App.User;
import com.example.article.HelloApplication;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import javafx.application.Platform;
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
    @FXML
    private TableColumn<Article, Integer> ratingsColumn;


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

        ratingsColumn.setCellValueFactory(new PropertyValueFactory<>("rating")); // Bind to Article rating
        ratingsColumn.setCellFactory(col -> new TableCell<Article, Integer>() {
            private final ComboBox<Integer> comboBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));

            {
                comboBox.setOnAction(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    int newRating = comboBox.getValue();
                    article.setRating(newRating); // Update model
                    updateArticleRatingInDatabase(article.getArticleId(), newRating); // Save to database
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        });

        // Load recommendations concurrently
        loadRecommendedArticlesConcurrently();
    }

    // Main method where you fetch articles by category
    public List<Article> fetchArticlesByCategory(String category) {
        List<Article> articles = new ArrayList<>();
        try {
            MongoCollection<Document> categoryCollection = getMongoCollection(category.replace(" ", "_"));
            MongoCursor<Document> cursor = categoryCollection.find().iterator();

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String articleId = doc.getString("articleID");
                String title = doc.getString("title");
                String date = doc.getString("date");
                String description = doc.getString("summary");
                String url = doc.getString("link");

                // Get the rating from the ratings collection for the current article and user
                int rating = getRatingForArticle(articleId);

                if (articleId != null && title != null && date != null) {
                    articles.add(new Article(articleId, title, date, description, url, rating)); // Pass the rating to the Article constructor
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching articles for category " + category + ": " + e.getMessage());
        }
        return articles;
    }

    // Method to fetch the rating for an article based on the logged-in user and articleId

    private int getRatingForArticle(String articleId) {
        int rating = 0;  // Default rating (or any other default you want)

        try {
            MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");
            Bson filter = Filters.and(Filters.eq("articleId", articleId), Filters.eq("userId", User.getLoggedInUserId()));
            Document userRatingDoc = ratingsCollection.find(filter).first();

            if (userRatingDoc != null) {
                rating = userRatingDoc.getInteger("rating", 0);  // If rating exists, use it; otherwise, default to 3
            }
        } catch (Exception e) {
            System.err.println("Error fetching rating for article " + articleId + ": " + e.getMessage());
        }

        return rating;
    }

    //getting the user data from the database
    private User fetchUserData(String userId) {
        System.out.println("Starting to fetch user data for userId: " + userId);
        User user = new User(userId, "username_placeholder");
        MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");

        try (MongoCursor<Document> cursor = ratingsCollection.find(Filters.eq("userId", userId)).iterator()) {
            System.out.println("Querying the database for user ratings...");
            while (cursor.hasNext()) {
                Document ratingDoc = cursor.next();
                String articleId = ratingDoc.getString("articleId");
                int rating = ratingDoc.getInteger("rating", 0);
                System.out.println("Processing rating: ArticleID=" + articleId + ", Rating=" + rating);

                String category = getArticleCategory(articleId);
                if (category != null) {
                    System.out.println("Found category: " + category + " for ArticleID=" + articleId);
                    user.updateRating(articleId, category, rating);
                } else {
                    System.out.println("No category found for ArticleID=" + articleId);
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while fetching user data: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Finished fetching user data for userId: " + userId);
        return user;
    }

    //getting news for new users
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
    }    private List<Article> getRandomArticlesFromAllCategories() {
        List<Future<List<Article>>> futures = new ArrayList<>();
        List<Article> articles = Collections.synchronizedList(new ArrayList<>());
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            Future<List<Article>> future = executorService.submit(() -> {
                List<Article> categoryArticles = new ArrayList<>();
                MongoCollection<Document> collection = getMongoCollection(category);
                try (MongoCursor<Document> cursor = collection.aggregate(List.of(new Document("$sample", new Document("size", 1)))).iterator()) {
                    while (cursor.hasNext()) {
                        categoryArticles.add(createArticleFromDocument(cursor.next()));
                    }
                }
                return categoryArticles;
            });
            futures.add(future);
        }

        for (Future<List<Article>> future : futures) {
            try {
                articles.addAll(future.get()); // Collect results
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error fetching articles: " + e.getMessage());
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
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
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

        if (result.isPresent()) {
            try {
                int rating = Integer.parseInt(result.get());
                if (rating >= 1 && rating <= 5) {
                    return rating;
                } else {
                    showAlert("Invalid Rating", Alert.AlertType.WARNING, "Please enter a rating between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", Alert.AlertType.ERROR, "Please enter a valid integer.");
            }
        }
        return 0; // Return 0 if input is invalid or canceled
    }

    // Utility method to display alerts
    private void showAlert(String title, Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait();
    }



    private void showInvalidRatingAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Rating");
        alert.setHeaderText("Rating must be between 1 and 5.");
        alert.showAndWait();
    }

    private void updateArticleRatingInDatabase(String articleId, int rating) {
        if (rating == 0) return;  // Don't update the database if rating is 0 (i.e., no user rating)
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
    } private Map<String, Integer> categorizeUserRatings(String userId, boolean highRated) {
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

    private Map<String, Double> calculateCategoryScores(String userId) {
        MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");
        Map<String, List<Integer>> categoryRatings = new HashMap<>();

        // Query user ratings and categorize them
        try (MongoCursor<Document> cursor = ratingsCollection.find(Filters.eq("userId", userId)).iterator()) {
            while (cursor.hasNext()) {
                Document ratingDoc = cursor.next();
                String articleId = ratingDoc.getString("articleId");
                int rating = ratingDoc.getInteger("rating", 0);
                String category = getArticleCategory(articleId);

                if (category != null) {
                    categoryRatings.computeIfAbsent(category, k -> new ArrayList<>()).add(rating);
                }
            }
        }

        // Calculate the average rating for each category
        Map<String, Double> categoryScores = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : categoryRatings.entrySet()) {
            List<Integer> ratings = entry.getValue();
            double averageRating = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            categoryScores.put(entry.getKey(), averageRating);
        }

        return categoryScores;
    }

    private List<Article> recommendArticlesBasedOnCategoryScores(Map<String, Double> categoryScores) {
        List<Article> recommendedArticles = new ArrayList<>();

        // Thresholds for preferred, excluded, and neutral categories
        double preferredThreshold = 3.5;
        double excludedThreshold = 2.5;

        for (Map.Entry<String, Double> entry : categoryScores.entrySet()) {
            String category = entry.getKey();
            double score = entry.getValue();

            // Include articles from preferred categories
            if (score >= preferredThreshold) {
                System.out.println("Including articles from preferred category: " + category);
                recommendedArticles.addAll(fetchArticlesByCategoryWithLimit(category, 6));
            } else if (score > excludedThreshold && score < preferredThreshold) {
                System.out.println("Neutral category: " + category + ", skipping for now.");
            } else {
                System.out.println("Excluding articles from low-rated category: " + category);
            }
        }

        return recommendedArticles;
    }

    // Helper method to fetch articles with a limit for a specific category
    private List<Article> fetchArticlesByCategoryWithLimit(String category, int limit) {
        List<Article> articles = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getMongoCollection(category);
            try (MongoCursor<Document> cursor = collection.find().limit(limit).iterator()) {
                while (cursor.hasNext()) {
                    articles.add(createArticleFromDocument(cursor.next()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching articles for category " + category + ": " + e.getMessage());
        }
        return articles;
    }


    public void loadRecommendedArticlesConcurrently() {
        String userId = User.getLoggedInUserId();

        executorService.submit(() -> {
            try {
                User user = fetchUserData(userId);
                System.out.println("User data fetched: " + user);

                ObservableList<Article> recommendations = FXCollections.observableArrayList();

                // Check if the user is new
                boolean isNewUser = isNewUser(userId);

                if (isNewUser) {
                    System.out.println("User is new, fetching random articles...");
                    recommendations.addAll(getRandomArticlesFromAllCategories());
                } else {
                    System.out.println("User is returning, calculating category scores...");
                    Map<String, Double> categoryScores = calculateCategoryScores(userId);
                    recommendations.addAll(recommendArticlesBasedOnCategoryScores(categoryScores));
                }

                // Remove duplicates
                Set<Article> uniqueArticles = new HashSet<>(recommendations);
                recommendations.setAll(uniqueArticles);

                // Update the UI on the JavaFX Application Thread
                Platform.runLater(() -> recommendedArticlesTable.setItems(recommendations));
            } catch (Exception e) {
                System.err.println("Error loading recommendations: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    public void stop() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // Shut down all tasks immediately
        }
        System.out.println("Application stopped and executor service shut down.");
    }


    //getting back to dashboard
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        try {
            Parent dashboardRoot = FXMLLoader.load(getClass().getResource("/com/example/article/DashBoard.fxml"));
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