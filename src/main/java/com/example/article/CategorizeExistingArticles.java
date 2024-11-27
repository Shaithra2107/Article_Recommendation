package com.example.article;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class CategorizeExistingArticles {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";  // Update if necessary
    private static final String DATABASE_NAME = "News_Recommendation";
    private static final String COLLECTION_NAME = "News";

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> articleCollection;

    // Constructor to initialize MongoDB client and connection
    public CategorizeExistingArticles() {
        ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(DATABASE_NAME);
        articleCollection = database.getCollection(COLLECTION_NAME);
    }

    public void categorizeAndMoveArticles() {
        // Fetch all existing articles from the 'News' collection
        List<Document> articles = articleCollection.find().into(new ArrayList<>());

        for (Document article : articles) {
            // Validate fields to avoid null-pointer exceptions
            String keywords = article.getString("keywords");
            String title = article.getString("title");
            String summary = article.getString("summary");

            if (keywords == null || title == null || summary == null) {
                System.err.println("Skipping article with missing fields: " + article);
                continue;
            }

            // Use your categorization logic to get the category of the article
            String category = ArticleCategorizer.categorizeArticle(
                    List.of(keywords.split(",")),  // Tags as list
                    title,
                    summary
            );

            if (category == null || category.isEmpty()) {
                System.err.println("Skipping article with invalid category: " + article);
                continue;
            }

            // Ensure category name is formatted correctly for a collection
            String formattedCategory = category.replace(" ", "_");

            // Get or create the category collection (MongoDB will automatically create it if it doesn't exist)
            MongoCollection<Document> categoryCollection = database.getCollection(formattedCategory);

            // Replace the existing document if it already exists in the category collection
            categoryCollection.replaceOne(
                    new Document("_id", article.getObjectId("_id")),  // Match by _id
                    article,  // The document to replace
                    new com.mongodb.client.model.ReplaceOptions().upsert(true)  // Use upsert to insert if not found
            );
        }

        System.out.println("Articles have been categorized and moved.");
    }

    // Clean up resources
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public static void main(String[] args) {
        CategorizeExistingArticles categorizer = new CategorizeExistingArticles();
        try {
            categorizer.categorizeAndMoveArticles();
        } finally {
            categorizer.close();  // Ensure the MongoDB client is closed
        }
    }
}
