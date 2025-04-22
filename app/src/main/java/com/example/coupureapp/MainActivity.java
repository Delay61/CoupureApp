package com.example.coupureapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtCoupureZone, txtNomUtilisateur, txtNumUtilisateur, txtCommuneUtilisateur;
    private ListView listAlertes;
    private Button logoutButton;
    private FirebaseFirestore db;

    private final String zoneUtilisateur = "Bujumbura"; // À rendre dynamique plus tard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiserUI();
        afficherCoupuresZone();
        chargerDernieresAlertes();
        chargerInfosUtilisateur();  // Charge les informations de l'utilisateur
        configurerNavigation();
    }

    private void initialiserUI() {
        txtCoupureZone = findViewById(R.id.txtCoupureZone);
        txtNomUtilisateur = findViewById(R.id.txtNomUtilisateur);
        txtNumUtilisateur = findViewById(R.id.txtNumUtilisateur);
        txtCommuneUtilisateur = findViewById(R.id.txtCommuneUtilisateur);
        listAlertes = findViewById(R.id.listAlertes);
        logoutButton = findViewById(R.id.logoutButton);
        db = FirebaseFirestore.getInstance();

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void chargerInfosUtilisateur() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nom = document.getString("nom");
                        String numero = document.getString("numero");
                        String commune = document.getString("commune");

                        txtNomUtilisateur.setText("Bienvenue Sur notre application" + nom);
                        txtNumUtilisateur.setText("_");
                        txtCommuneUtilisateur.setText("_");
                    }
                })
                .addOnFailureListener(e -> {
                    // Gérer l'erreur ici
                    txtNomUtilisateur.setText("Erreur de récupération des infos");
                    txtNumUtilisateur.setText("Erreur de récupération des infos");
                    txtCommuneUtilisateur.setText("Erreur de récupération des infos");
                });
    }

    private void configurerNavigation() {
        findViewById(R.id.btnSignalement).setOnClickListener(v ->
                startActivity(new Intent(this, SignalementActivity.class)));

        findViewById(R.id.btnCarte).setOnClickListener(v ->
                startActivity(new Intent(this, CarteActivity.class)));

        findViewById(R.id.btnHistorique).setOnClickListener(v ->
                startActivity(new Intent(this, HistoriqueActivity.class)));

//        findViewById(R.id.btnCalendrier).setOnClickListener(v ->
//                startActivity(new Intent(this, CalendrierActivity.class)));

//        findViewById(R.id.btnNotifications).setOnClickListener(v ->
//                startActivity(new Intent(this, NotificationsActivity.class)));

        findViewById(R.id.btnProfil).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilActivity.class)));

        findViewById(R.id.btnParametres).setOnClickListener(v ->
                startActivity(new Intent(this, ParametresActivity.class)));

        findViewById(R.id.btnAPropos).setOnClickListener(v ->
                startActivity(new Intent(this, AproposActivity.class)));
    }

    private void afficherCoupuresZone() {
        db.collection("coupures_planifiees")
                .whereEqualTo("zone", zoneUtilisateur)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String date = query.getDocuments().get(0).getString("date");
                        txtCoupureZone.setText("Prochaine coupure dans votre zone : " + date);
                    } else {
                        txtCoupureZone.setText("Aucune coupure prévue pour votre zone");
                    }
                })
                .addOnFailureListener(e -> txtCoupureZone.setText("Erreur lors du chargement des données."));
    }

    private void chargerDernieresAlertes() {
        db.collection("alertes")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(query -> {
                    List<String> alertes = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        alertes.add(doc.getString("message"));
                    }
                    listAlertes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alertes));
                })
                .addOnFailureListener(e -> {
                    List<String> erreur = new ArrayList<>();
                    erreur.add("Impossible de charger les alertes.");
                    listAlertes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, erreur));
                });
    }
}
