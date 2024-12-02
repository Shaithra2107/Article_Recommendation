package com.example.article.App;

public class Admin extends Person {
    private String adminLevel;

    // Constructor
    public Admin(String id, String name, String adminLevel) {
        super(id, name);
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    // Admin-Specific Behavior
    public void manageUsers() {
        System.out.println("Managing users...");
    }
}