package com.example.coupureapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.Map;

public class ProfilActivity extends AppCompatActivity {

    private TextView textUID, textNom, textPrenom, textZone, textTelephone, textEmail;
    private Button btnModifier, btnSupprimer, btnAccueil, btnChangerMdp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        // Liaison des vues
        textUID = findViewById(R.id.textUID);
        textNom = findViewById(R.id.textNom);
        textPrenom = findViewById(R.id.textPrenom);
        textZone = findViewById(R.id.textZone);
        textTelephone = findViewById(R.id.textTelephone);
        textEmail = findViewById(R.id.textEmail);

        btnModifier = findViewById(R.id.btnModifier);
        btnSupprimer = findViewById(R.id.btnSupprimer);
        btnAccueil = findViewById(R.id.btnAccueil);
        btnChangerMdp = findViewById(R.id.btnChangerMdp);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            userId = user.getUid();
            textUID.setText("ID utilisateur : " + userId);

            // Chargement des données depuis Firestore
            db.collection("utilisateurs").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = documentSnapshot.getData();
                            if (data != null) {
                                textNom.setText("Nom : " + safeValue(data.get("nom")));
                                textPrenom.setText("Prénom : " + safeValue(data.get("prenom")));
                                textZone.setText("Zone : " + safeValue(data.get("zone")));
                                textTelephone.setText("Téléphone : " + safeValue(data.get("telephone")));
                                textEmail.setText("Email : " + safeValue(data.get("email")));
                            }
                        } else {
                            Toast.makeText(this, "Aucune donnée utilisateur trouvée", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur chargement profil : " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        //Accueil
        btnAccueil.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, MainActivity.class));
            finish();
        });

        // Réinitialisation du mot de passe
        btnChangerMdp.setOnClickListener(v -> {
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Email de réinitialisation envoyé", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Email introuvable pour cet utilisateur", Toast.LENGTH_SHORT).show();
            }
        });

        //Supprimer compte
        btnSupprimer.setOnClickListener(v -> {
            db.collection("utilisateurs").document(userId).delete()
                    .addOnSuccessListener(unused -> {
                        user.delete().addOnSuccessListener(unused1 -> {
                            Toast.makeText(this, "Compte supprimé avec succès", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfilActivity.this, LoginActivity.class));
                            finish();
                        }).addOnFailureListener(e -> Toast.makeText(this, "Erreur suppression compte : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Erreur suppression Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        //Modifier les infos
        btnModifier.setOnClickListener(v -> {
            startActivity(new Intent(ProfilActivity.this, ModifierProfilActivity.class));
        });
    }

    //Empêche les null ou objets non String
    private String safeValue(Object val) {
        return val != null ? val.toString() : "N/A";
    }
}
