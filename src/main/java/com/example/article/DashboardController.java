package com.example.article;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class DashboardController {
    @FXML
    public Label sectionTitle;
    @FXML
    public ListView articleListView;
    @FXML
    public Button likeButton;
    @FXML
    public Button dislikeButton;
    @FXML
    public Button logoutButton;
    @FXML
    public Button viewArticlesButton;

    public void handleLogout(ActionEvent actionEvent) {
    }

    public void handleViewArticles(ActionEvent actionEvent) {
    }

    public void handleGetRecommendations(ActionEvent actionEvent) {
    }

    public void handleManageProfile(ActionEvent actionEvent) {
    }

    public void handleAdministerArticles(ActionEvent actionEvent) {
    }

    public void handleLikeArticle(ActionEvent actionEvent) {
    }

    public void handleDislikeArticle(ActionEvent actionEvent) {
    }
}
