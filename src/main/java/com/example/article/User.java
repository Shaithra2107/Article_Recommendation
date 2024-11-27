package com.example.article;

public class User {
    private static String loggedInUserId;

    public static String getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(String loggedInUserId) {
        User.loggedInUserId = loggedInUserId;
    }
}

