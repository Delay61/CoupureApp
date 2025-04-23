package com.example.coupureapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AproposActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apropos);

        TextView txtContact = findViewById(R.id.txtContact);
        TextView txtBug = findViewById(R.id.txtBug);
        Button btnRetourAccueil = findViewById(R.id.btnRetourAccueil);

        // Lien email vers le développeur
        txtContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:tonemail@exemple.com")); // Mets ici ton adresse mail
            intent.putExtra(Intent.EXTRA_SUBJECT, "Contact via l'application CoupureApp");
            startActivity(Intent.createChooser(intent, "Envoyer un e-mail"));
        });

        // Lien vers un formulaire Google Forms ou autre
        txtBug.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://forms.gle/tonFormulaire")); // Mets ici ton vrai lien
            startActivity(intent);
        });

        // Retour à MainActivity
        btnRetourAccueil.setOnClickListener(v -> {
            Intent intent = new Intent(AproposActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Optionnel : pour fermer l'activité actuelle
        });
    }
}
