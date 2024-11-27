package com.example.article;


public class Article {
    private String category;
    private String articleId;
    private String title;
    private String date;
    private String description;
    private String url;
    private int rating;

    // Constructor
    public Article(String articleId, String title, String date, String description, String url) {
        this.articleId = articleId;
        this.title = title;
        this.date = date;
        this.description = description;
        this.url = url;
        this.rating = 0;  // Default rating value
    }

    // Getters and Setters
    public String getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

