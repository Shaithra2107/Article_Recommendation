package com.example.article.App;

import java.util.ArrayList;

public abstract class Person {
    private String id;
    private String name;
    private ArrayList<Article> articles=new ArrayList<>();

    public Person(String id,String name,ArrayList<Article> articles) {
        this.id=id;
        this.name=name;
        this.articles=articles;
    }


    // Constructor
    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Abstract Method for Role-Specific Behavior
    public abstract String getRole();
}