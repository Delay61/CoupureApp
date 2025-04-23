package com.example.coupureapp;

public class Signalement {
    private String id;
    private String description;
    private String date;
    private String localisation;
    private String imagePath;
    private String type; // Ajout du champ type

    // 🔧 Constructeur vide requis par Firestore
    public Signalement() {
    }

    //Constructeur avec tous les paramètres
    public Signalement(String description, String date, String localisation, String imagePath, String type) {
        this.description = description;
        this.date = date;
        this.localisation = localisation;
        this.imagePath = imagePath;
        this.type = type;
    }

    // Getters
    public String getId() {
        return id;
    }

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

    public String getType() {
        return type;
    }

    //Setters
    public void setId(String id) {
        this.id = id;
    }

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

    public void setType(String type) {
        this.type = type;
    }
}
