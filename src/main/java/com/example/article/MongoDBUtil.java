package com.example.article;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtil {

    private static final String URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "News_Recommendation";

    public static MongoDatabase getDatabase() {
        MongoClient mongoClient = MongoClients.create(URI);
        return mongoClient.getDatabase(DATABASE_NAME);
    }
}
