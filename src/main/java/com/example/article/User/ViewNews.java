package com.example.article.User;

import com.example.article.App.Article;
import com.example.article.App.User;
import com.example.article.HelloApplication;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import javafx.application.HostServices;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class ViewNews implements Initializable {

    @FXML
    public Label categoryLabel;
    @FXML
    public TableView<Article> newsTable;
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
    private TableColumn<Article, Integer> ratingsColumn;  // Integer column for ratings
    @FXML
    private TableColumn<Article, Void> actionsColumn; // The column containing the "Rate" button

    private MongoCollection<Document> newsCollection;

    // ExecutorService for managing threads
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing ViewNews...");
        articleIdColumn.setCellValueFactory(new PropertyValueFactory<>("articleId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

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

        actionsColumn.setCellFactory(param -> new TableCell<Article, Void>() {
            private final Button btn = new Button("Rate");

            {
                btn.setOnAction(event -> {
                    Article article = getTableView().getItems().get(getIndex());
                    System.out.println("Rate button clicked for Article ID: " + article.getArticleId());
                    handleRateArticle(article);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Updated to handle Integer rating correctly
        ratingsColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));  // Bind to Article rating
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
                    comboBox.setValue(item); // Set the ComboBox value to the current rating
                    setGraphic(comboBox);
                }
            }
        });

        String category = DashboardController.getCategory();
        if (category != null) {
            System.out.println("Category loaded: " + category);
            categoryLabel.setText(category);
            loadArticlesConcurrently(category); // Load articles using concurrency
        } else {
            System.out.println("Category is null!");
        }
    }

    private void loadArticlesConcurrently(String category) {
        ObservableList<Article> articles = FXCollections.observableArrayList();
        try {
            List<Callable<List<Article>>> tasks = new ArrayList<>();

            // Split tasks per category (or other logic if necessary)
            tasks.add(() -> {
                System.out.println("Thread [" + Thread.currentThread().getName() + "] is fetching articles for category: " + category);
                return fetchArticlesByCategory(category);
            });

            // Execute tasks concurrently
            List<Future<List<Article>>> results = executorService.invokeAll(tasks);

            // Collect results from all threads
            for (Future<List<Article>> future : results) {
                articles.addAll(future.get());
                System.out.println("Thread [" + Thread.currentThread().getName() + "] has completed fetching articles.");
            }

            // Update TableView with the loaded articles
            newsTable.setItems(articles);
            System.out.println("All threads completed. Articles loaded successfully into TableView.");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error loading articles concurrently: " + e.getMessage());
        }
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
                    System.out.println("Thread [" + Thread.currentThread().getName() + "] fetched article: " + title + " (ID: " + articleId + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching articles for category " + category + ": " + e.getMessage());
        }
        return articles;
    }

    // Method to fetch the rating for an article based on the logged-in user and articleId
    private int getRatingForArticle(String articleId) {
        int rating = 0;  // Default to 0, indicating no rating.

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

    public void handleRateArticle(Article article) {
        System.out.println("Handling rating for Article: " + article);
        int rating = showRatingDialog();
        if (rating > 0) {
            updateArticleRatingInDatabase(article.getArticleId(), rating);
            article.setRating(rating);
            newsTable.refresh();
        }
    }

    private int showRatingDialog() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(3, 1, 2, 3, 4, 5);
        dialog.setTitle("Rate the Article");
        dialog.setHeaderText("Please rate the article:");
        dialog.setContentText("Choose a rating (1-5):");

        Optional<Integer> result = dialog.showAndWait();
        return result.orElse(-1); // Return -1 for no choice
    }

    private void updateArticleRatingInDatabase(String articleId, int rating) {
        if (rating == 0) return;  // Don't update the database if rating is -1 (i.e., no user rating).

        try {
            MongoCollection<Document> ratingsCollection = getMongoCollection("ratings");
            Bson filter = Filters.and(Filters.eq("articleId", articleId), Filters.eq("userId", User.getLoggedInUserId()));
            Document existingRating = ratingsCollection.find(filter).first();

            if (existingRating != null) {
                System.out.println("Updating existing rating for Article ID: " + articleId);
                ratingsCollection.updateOne(filter, Updates.set("rating", rating));
            } else {
                System.out.println("Inserting new rating for Article ID: " + articleId);
                Document newRating = new Document("userId", User.getLoggedInUserId())
                        .append("articleId", articleId)
                        .append("rating", rating);
                ratingsCollection.insertOne(newRating);
            }
        } catch (Exception e) {
            System.err.println("Error updating/inserting rating: " + e.getMessage());
            e.printStackTrace();
        }
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
        HostServices hostServices = HelloApplication.getHostServicesInstance();
        hostServices.showDocument(url);
    }

    // Handle the back button to return to the Admin Dashboard
    public void handleBack(ActionEvent actionEvent) {
        try {
            Parent adminRoot = FXMLLoader.load(getClass().getResource("/com/example/article/DashBoard.fxml"));
            Scene adminScene = new Scene(adminRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(adminScene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUrlClick(MouseEvent event) {
        // Get the selected article (assumes you have a method to get selected article)
        Article selectedArticle = newsTable.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            // Get the URL from the selected article
            String url = selectedArticle.getUrl();
            openUrlInBrowser(url);  // Open in browser
        }
    }
    // ExecutorService for managing threads


    {
        System.out.println("ExecutorService initialized with " + Runtime.getRuntime().availableProcessors() + " threads.");
    }





}
