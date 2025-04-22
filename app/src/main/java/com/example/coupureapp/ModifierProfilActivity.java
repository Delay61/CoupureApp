package com.example.coupureapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ModifierProfilActivity extends AppCompatActivity {

    private EditText editNom, editPrenom, editTelephone, editZone;
    private Button btnEnregistrer;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_profil);

        // Liaison des vues
        editNom = findViewById(R.id.editNom);
        editPrenom = findViewById(R.id.editPrenom);
        editTelephone = findViewById(R.id.editTelephone);
        editZone = findViewById(R.id.editZone);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bouton pour enregistrer les modifications
        btnEnregistrer.setOnClickListener(v -> enregistrerModifications());
    }

    private void enregistrerModifications() {
        String nom = editNom.getText().toString();
        String prenom = editPrenom.getText().toString();
        String telephone = editTelephone.getText().toString();
        String zone = editZone.getText().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("prenom", prenom);
        updates.put("telephone", telephone);
        updates.put("zone", zone);

        // ✅ Utilise .set() avec SetOptions.merge() pour créer ou mettre à jour
        db.collection("utilisateurs").document(userId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // Retour au profil
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
