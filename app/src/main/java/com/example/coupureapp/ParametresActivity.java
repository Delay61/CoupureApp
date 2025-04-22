package com.example.coupureapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

public class ParametresActivity extends AppCompatActivity {

    private Spinner spinnerLangue;
    private Switch switchNotifications, switchTheme;
    private EditText editZone;
    private Button btnSauvegarder, btnRetourMainActivity;

    private final String[] langues = {"Français", "Kirundi", "Anglais"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Appliquer le thème sombre clair selon préférences
        SharedPreferences prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("theme_sombre", false);
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        // Références des vues
        spinnerLangue = findViewById(R.id.spinnerLangue);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchTheme = findViewById(R.id.switchTheme);
        editZone = findViewById(R.id.editZone);
        btnSauvegarder = findViewById(R.id.btnSauvegarder);
        btnRetourMainActivity = findViewById(R.id.btnRetourMainActivity);

        // Initialiser Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langues);
        spinnerLangue.setAdapter(adapter);

        // Charger les préférences sauvegardées
        chargerPreferences();

        // Sauvegarder les préférences
        btnSauvegarder.setOnClickListener(v -> sauvegarderPreferences());

        // Retour à MainActivity
        btnRetourMainActivity.setOnClickListener(v -> {
            Intent intent = new Intent(ParametresActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Optionnel : pour fermer l'activité ParametresActivity
        });
    }

    private void chargerPreferences() {
        SharedPreferences prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        String langue = prefs.getString("langue", "Français");
        String zone = prefs.getString("zone", "");
        boolean notificationsActives = prefs.getBoolean("notifications", true);
        boolean themeSombre = prefs.getBoolean("theme_sombre", false);

        spinnerLangue.setSelection(getIndexLangue(langue));
        editZone.setText(zone);
        switchNotifications.setChecked(notificationsActives);
        switchTheme.setChecked(themeSombre);
    }

    private void sauvegarderPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences("Prefs", MODE_PRIVATE).edit();

        String langue = spinnerLangue.getSelectedItem().toString();
        String zone = editZone.getText().toString();
        boolean notifications = switchNotifications.isChecked();
        boolean themeSombre = switchTheme.isChecked();

        editor.putString("langue", langue);
        editor.putString("zone", zone);
        editor.putBoolean("notifications", notifications);
        editor.putBoolean("theme_sombre", themeSombre);
        editor.apply();

        // Appliquer le thème immédiatement
        AppCompatDelegate.setDefaultNightMode(themeSombre ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        Toast.makeText(this, "Préférences sauvegardées", Toast.LENGTH_SHORT).show();
    }

    private int getIndexLangue(String langue) {
        for (int i = 0; i < langues.length; i++) {
            if (langues[i].equalsIgnoreCase(langue)) return i;
        }
        return 0;
    }
}
