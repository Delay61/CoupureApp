package com.example.coupureapp;

public class User {
    private String id, nom, prenom, telephone, zone, email;

    // Constructeurs, getters et setters

    public User(String id, String nom, String prenom, String telephone, String zone, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.zone = zone;
        this.email = email;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getZone() {
        return zone;
    }

    public String getEmail() {
        return email;
    }
}
