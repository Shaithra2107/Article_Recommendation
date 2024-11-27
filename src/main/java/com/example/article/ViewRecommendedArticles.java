package com.example.article;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
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
    private MongoCollection<Document> newsCollection;

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

        // Load recommendations
        loadRecommendedArticles();
    }

    private void loadRecommendedArticles() {
        try {
            String userId = User.getLoggedInUserId();
            ObservableList<Article> recommendations = FXCollections.observableArrayList();

            // Get user ratings
            ratingsCollection = getMongoCollection("ratings");
            MongoCursor<Document> userRatings = ratingsCollection.find(Filters.eq("userId", userId)).iterator();

            // Categorize ratings
            Map<String, Integer> highRatedCounts = new HashMap<>();
            Map<String, Integer> lowRatedCounts = new HashMap<>();

            while (userRatings.hasNext()) {
                Document ratingDoc = userRatings.next();
                String articleId = ratingDoc.getString("articleId");
                int rating = ratingDoc.getInteger("rating", 0);

                // Fetch category from the article
                String category = getArticleCategory(articleId);
                if (rating >= 3) {
                    highRatedCounts.put(category, highRatedCounts.getOrDefault(category, 0) + 1);
                } else {
                    lowRatedCounts.put(category, lowRatedCounts.getOrDefault(category, 0) + 1);
                }
            }

            // Recommendation logic
            if (highRatedCounts.isEmpty() && lowRatedCounts.isEmpty()) {
                // New user: Recommend one article from each category
                recommendations.addAll(getRandomArticlesFromAllCategories());
            } else if (!lowRatedCounts.isEmpty() && highRatedCounts.isEmpty()) {
                // User has only low ratings: Recommend from other categories
                recommendations.addAll(getArticlesFromCategoriesExcluding(lowRatedCounts.keySet()));
            } else {
                // User with mixed or high ratings
                recommendations.addAll(getArticlesFromPreferredCategories(highRatedCounts));
            }

            // Set data to the TableView
            recommendedArticlesTable.setItems(recommendations);

        } catch (Exception e) {
            System.err.println("Error loading recommendations: " + e.getMessage());
        }
    }

    private List<Article> getRandomArticlesFromAllCategories() {
        List<Article> articles = new ArrayList<>();
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            MongoCollection<Document> collection = getMongoCollection(category.replace(" ", "_"));
            MongoCursor<Document> cursor = collection.aggregate(Arrays.asList(new Document("$sample", new Document("size", 1)))).iterator();

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                articles.add(createArticleFromDocument(doc));
            }
        }
        return articles;
    }

    private List<Article> getArticlesFromCategoriesExcluding(Set<String> excludedCategories) {
        List<Article> articles = new ArrayList<>();
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            if (!excludedCategories.contains(category)) {
                MongoCollection<Document> collection = getMongoCollection(category.replace(" ", "_"));
                MongoCursor<Document> cursor = collection.find().limit(10).iterator();

                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    articles.add(createArticleFromDocument(doc));
                }
            }
        }
        return articles;
    }

    private List<Article> getArticlesFromPreferredCategories(Map<String, Integer> preferredCategories) {
        List<Article> articles = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : preferredCategories.entrySet()) {
            String category = entry.getKey();
            MongoCollection<Document> collection = getMongoCollection(category.replace(" ", "_"));
            MongoCursor<Document> cursor = collection.find().limit(10).iterator();

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                articles.add(createArticleFromDocument(doc));
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
        // Search in all categories to find the article
        String[] categories = {"Business_and_Economy", "Geopolitics_and_Regional_Focus", "Health_and_Pandemic", "Technology", "Sports_and_Competition", "Others"};

        for (String category : categories) {
            MongoCollection<Document> collection = getMongoCollection(category.replace(" ", "_"));
            Document doc = collection.find(Filters.eq("articleID", articleId)).first();
            if (doc != null) {
                return category;
            }
        }
        return null; // No category found
    }

    public void handleRateArticle(Article article) {
        int rating = showRatingDialog();
        if (rating > 0) {
            updateArticleRatingInDatabase(article.getArticleId(), rating);
            article.setRating(rating);
            recommendedArticlesTable.refresh();
        }
    }

    private int showRatingDialog() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(3, 1, 2, 3, 4, 5);
        dialog.setTitle("Rate the Article");
        dialog.setHeaderText("Please rate the article:");
        dialog.setContentText("Choose a rating (1-5):");

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1);
    }

    private void updateArticleRatingInDatabase(String articleId, int rating) {
        Bson filter = Filters.and(Filters.eq("articleId", articleId), Filters.eq("userId", User.getLoggedInUserId()));
        Document existingRating = ratingsCollection.find(filter).first();

        if (existingRating != null) {
            ratingsCollection.updateOne(filter, Updates.set("rating", rating));
        } else {
            Document newRating = new Document("userId", User.getLoggedInUserId())
                    .append("articleId", articleId)
                    .append("rating", rating);
            ratingsCollection.insertOne(newRating);
        }
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

    public void initializeRecommendations(String userId) {
        // Set the label or other relevant details if necessary
        categoryLabel.setText("Recommended Articles for User: " + userId);

        // Load recommended articles for the given user
        loadRecommendedArticles();
    }

}
