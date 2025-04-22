package com.example.coupureapp;

public class Signalement {
    private String description;
    private String date;
    private String localisation;
    private String imagePath;

    // 🔧 Constructeur vide requis par Firestore
    public Signalement() {
        // Obligatoire pour Firestore
    }

    // ✅ Constructeur avec paramètres
    public Signalement(String description, String date, String localisation, String imagePath) {
        this.description = description;
        this.date = date;
        this.localisation = localisation;
        this.imagePath = imagePath;
    }

    // ✅ Getters
    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getLocalisation() {
        return localisation;
    }

    public String getImagePath() {
        return imagePath;
    }

    // ✅ Setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
