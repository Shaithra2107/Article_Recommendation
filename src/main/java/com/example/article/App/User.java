package com.example.article.App;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class User extends Person {
    private static String loggedInUserId;

    // New fields to store user preferences
    private Map<String, Map<String, Integer>> ratingsByCategory; // category -> (articleId -> rating)
    private Set<String> preferredCategories; // High-rated categories

    // Constructor
    public User(String id, String name) {
        super(id, name);
        loggedInUserId = id;
        this.ratingsByCategory = new HashMap<>();
        this.preferredCategories = new HashSet<>();
    }

    // Static methods for logged-in user management
    public static String getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(String loggedInUserId) {
        com.example.article.App.User.loggedInUserId = loggedInUserId;
    }

    // Overridden method
    @Override
    public String getRole() {
        return "User";
    }

    // Add a rating for an article
    public void updateRating(String articleId, String category, int rating) {
        ratingsByCategory
                .computeIfAbsent(category, k -> new HashMap<>())
                .put(articleId, rating);

        // Update preferred categories if rating is high
        if (rating >= 4) {
            preferredCategories.add(category);
        } else if (rating <= 2) {
            preferredCategories.remove(category);
        }
    }

    // Get ratings for a specific category
    public Map<String, Integer> getRatingsForCategory(String category) {
        return ratingsByCategory.getOrDefault(category, new HashMap<>());
    }

    // Get all preferred categories
    public Set<String> getPreferredCategories() {
        return preferredCategories;
    }

    // Determine if the user is new
    public boolean isNewUser() {
        return ratingsByCategory.isEmpty();
    }
}