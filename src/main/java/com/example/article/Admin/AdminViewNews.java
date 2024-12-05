package com.example.article.Admin;

import com.example.article.App.Article;
import com.example.article.HelloApplication;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class AdminViewNews implements Initializable {
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
        String category = AdminDashboard.getCategory();
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
            tasks.add(() -> fetchArticlesByCategory(category));

            // Execute tasks concurrently
            List<Future<List<Article>>> results = executorService.invokeAll(tasks);

            // Collect results from all threads
            for (Future<List<Article>> future : results) {
                articles.addAll(future.get());
            }

            // Update TableView with the loaded articles
            newsTable.setItems(articles);

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


                if (articleId != null && title != null && date != null) {
                    articles.add(new Article(articleId, title, date, description, url)); // Pass the rating to the Article constructor
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching articles for category " + category + ": " + e.getMessage());
        }
        return articles;
    }


    private MongoCollection<Document> getMongoCollection(String collectionName) {
        try {
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
            MongoClient mongoClient = MongoClients.create("mongodb+srv://shaithra20232694:123shaithra@cluster0.cwjpj.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
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

    // Handle the back button to return to the Admin Dashboard
    public void handleBackToAdmin(ActionEvent actionEvent) {
        try {
            Parent adminRoot = FXMLLoader.load(getClass().getResource("/com/example/article/AdminView.fxml"));
            Scene adminScene = new Scene(adminRoot);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(adminScene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
