package com.example.coupureapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText nomEditText, prenomEditText, telephoneEditText, zoneEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation des vues
        nomEditText = findViewById(R.id.nomEditText);
        prenomEditText = findViewById(R.id.prenomEditText);
        telephoneEditText = findViewById(R.id.telephoneEditText);
        zoneEditText = findViewById(R.id.zoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        btnLogin = findViewById(R.id.btnlogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bouton pour s'enregistrer
        registerButton.setOnClickListener(v -> enregistrerUtilisateur());

        // Bouton pour aller vers l'écran de connexion
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void enregistrerUtilisateur() {
        // Récupération des données saisies
        String nom = nomEditText.getText().toString().trim();
        String prenom = prenomEditText.getText().toString().trim();
        String tel = telephoneEditText.getText().toString().trim();
        String zone = zoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String mdp = passwordEditText.getText().toString();
        String confirmMdp = confirmPasswordEditText.getText().toString();

        // Vérification des champs
        if (TextUtils.isEmpty(nom) || TextUtils.isEmpty(prenom) || TextUtils.isEmpty(tel) ||
                TextUtils.isEmpty(zone) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mdp) || TextUtils.isEmpty(confirmMdp)) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérification des mots de passe
        if (!mdp.equals(confirmMdp)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Création de l'utilisateur avec Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, mdp).addOnCompleteListener(task -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();

                // Création du profil utilisateur dans Firestore
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("nom", nom);
                userMap.put("prenom", prenom);
                userMap.put("telephone", tel);
                userMap.put("zone", zone);
                userMap.put("email", email);

                db.collection("utilisateurs").document(userId).set(userMap)
                        .addOnSuccessListener(unused -> {
                            // Enregistrement dans la base de données SQLite
                            UserDatabaseHelper dbHelper = new UserDatabaseHelper(RegisterActivity.this);
                            SQLiteDatabase sqLiteDb = dbHelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put(UserDatabaseHelper.COLUMN_ID, userId);
                            values.put(UserDatabaseHelper.COLUMN_NOM, nom);
                            values.put(UserDatabaseHelper.COLUMN_PRENOM, prenom);
                            values.put(UserDatabaseHelper.COLUMN_TEL, tel);
                            values.put(UserDatabaseHelper.COLUMN_ZONE, zone);
                            values.put(UserDatabaseHelper.COLUMN_EMAIL, email);
                            sqLiteDb.insert(UserDatabaseHelper.TABLE_NAME, null, values);

                            Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Erreur Authentification : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
