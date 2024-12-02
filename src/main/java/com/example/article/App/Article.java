package com.example.article.App;


import java.util.HashSet;
import java.util.Set;

public class Article {
    private String category;
    private String articleId;
    private String title;
    private String date;
    private String description;
    private String url;
    private int rating;
    private Set<Person> persons; // M:M relationship with Person

    // Constructor
    public Article(String articleId, String title, String date, String description, String url) {
        this.articleId = articleId;
        this.title = title;
        this.date = date;
        this.description = description;
        this.url = url;
        this.rating = 0;  // Default rating value
    }
    public Article(String articleId, String title, String date, String description, String url, int rating) {
        this.articleId = articleId;
        this.title = title;
        this.date = date;
        this.description = description;
        this.url = url;
        this.rating = rating;
    }
    // Constructors
    public Article(String articleId, String title, String date, String description, String url, int rating, Set<Person> persons) {
        this.articleId = articleId;
        this.title = title;
        this.date = date;
        this.description = description;
        this.url = url;
        this.rating = 0; // Default rating
        this.persons = new HashSet<>();
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

    public Set<Person> getPersons() {
        return persons;
    }

    // Add a Person to the Article's set
    public void addPerson(Person person) {
        this.persons.add(person);
    }
    @Override
    public String toString() {
        return "Article{" +
                "articleId='" + articleId + '\'' +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", rating=" + rating +
                '}';
    }
}