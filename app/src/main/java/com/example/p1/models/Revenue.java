package com.example.p1.models;

public class Revenue {
    private int id;
    private int userId;
    private String title;
    private String source;
    private double amount;
    private String date;
    private String description;

    // Constructeur vide
    public Revenue() {}

    // Constructeur sans id
    public Revenue(int userId, String title, String source, double amount, String date, String description) {
        this.userId = userId;
        this.title = title;
        this.source = source;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Constructeur complet
    public Revenue(int id, int userId, String title, String source, double amount, String date, String description) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.source = source;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}