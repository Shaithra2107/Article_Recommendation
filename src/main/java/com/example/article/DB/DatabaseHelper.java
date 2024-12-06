package com.example.article.DB;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DatabaseHelper {
    private static final String DATABASE_NAME = "News_Recommendation";
    private final MongoClient mongoClient;


    //Making connection with the mongo db
    public DatabaseHelper(String connectionString) {
        try {
            // MongoClientSettings setup
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .serverApi(serverApi)
                    .build();

            // Create MongoClient with settings
            mongoClient = MongoClients.create(settings);
        } catch (Exception e) {
            System.err.println("Error initializing MongoClient: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize MongoClient.", e);
        }
    }

    //accessing the collection
    public MongoCollection<Document> getCollection(String collectionName) {
        return mongoClient.getDatabase(DATABASE_NAME).getCollection(collectionName);
    }

    //closing the databse
    public void close() {
        mongoClient.close();
    }
}
