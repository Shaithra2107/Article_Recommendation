package com.example.article;

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
import javafx.util.Callback;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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
    private TableColumn<Article, Void> actionsColumn; // The column containing the "Rate" button

    private MongoCollection<Document> newsCollection;

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

        String category = DashboardController.getCategory();
        if (category != null) {
            System.out.println("Category loaded: " + category);
            categoryLabel.setText(category);
            setCategoryAndLoadArticles(category);
        } else {
            System.out.println("Category is null!");
        }
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

    private void setCategoryAndLoadArticles(String category) {
        try {
            ObservableList<Article> articles = FXCollections.observableArrayList();
            MongoCollection<Document> categoryCollection = getMongoCollection(category.replace(" ", "_"));
            MongoCursor<Document> cursor = categoryCollection.find().iterator();

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String articleId = doc.getString("articleID");
                String title = doc.getString("title");
                String date = doc.getString("date");
                String description = doc.getString("summary");
                String url = doc.getString("link");

                if (articleId == null || title == null || date == null) {
                    System.out.println("Missing article data: " + doc.toJson());
                    continue;
                }

                articles.add(new Article(articleId, title, date, description, url));
            }

            newsTable.setItems(articles);
        } catch (Exception e) {
            System.err.println("Error loading articles: " + e.getMessage());
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
        HostServices hostServices = HelloApplication.getHostServicesInstance();
        hostServices.showDocument(url);
    }


    // Handle the back button to return to the Admin Dashboard
    public void handleBack(ActionEvent actionEvent) {
        try {
            Parent adminRoot = FXMLLoader.load(getClass().getResource("DashBoard.fxml"));
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

            // Open the URL in the default web browser
            if (url != null) {
                HostServices hostServices = HelloApplication.getHostServicesInstance();
                hostServices.showDocument(url);
            }
        }
    }
}
