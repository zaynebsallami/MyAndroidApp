package com.example.p1.models;

public class Expense {
    private int id;
    private String title;
    private String category;
    private double amount;
    private String date;
    private String description; // Make sure this field exists

    // Constructeur vide
    public Expense() {}

    // Constructeur complet sans id
    public Expense(String title, String category, double amount, String date, String description) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Constructeur complet avec id
    public Expense(int id, String title, String category, double amount, String date, String description) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Make sure this getter exists
    public String getDescription() {
        return description;
    }

    // Make sure this setter exists
    public void setDescription(String description) {
        this.description = description;
    }
}