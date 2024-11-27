package com.example.article;

import java.util.List;

public class ArticleCategorizer {

    public static String categorizeArticle(List<String> tags, String title, String summary) {
        String text = String.join(" ", tags) + " " + title + " " + summary;
        text = text.toLowerCase();

        // Simple keyword matching logic
        if (text.contains("million") || text.contains("billion") || text.contains("global") || text.contains("company")) {
            return "Business and Economy";
        } else if (text.contains("techcrunch") || text.contains("data") || text.contains("platform")) {
            return "Technology";
        } else if (text.contains("coronavirus") || text.contains("covid19") || text.contains("health")) {
            return "Health and Pandemic";
        } else if (text.contains("india") || text.contains("china") || text.contains("domestic")) {
            return "Geopolitics and Regional Focus";
        } else if (text.contains("competition") || text.contains("scoreboard") || text.contains("twenty20")) {
            return "Sports and Competition";
        } else {
            return "Others";
        }
    }
}

