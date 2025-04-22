package com.example.coupureapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoriqueActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SignalementAdapter adapter;
    private List<Signalement> signalementList;
    private TextView statsTextView;
    private Button btnExportPDF, btnExportCSV, btnRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        recyclerView = findViewById(R.id.recyclerView);
        statsTextView = findViewById(R.id.statsTextView);
        btnExportPDF = findViewById(R.id.btnExportPDF);
        btnExportCSV = findViewById(R.id.btnExportCSV);
        btnRetour = findViewById(R.id.btnRetour);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        signalementList = new ArrayList<>();
        adapter = new SignalementAdapter(signalementList, this);
        recyclerView.setAdapter(adapter);

        btnExportPDF.setOnClickListener(v -> exportPDF());
        btnExportCSV.setOnClickListener(v -> exportCSV());
        btnRetour.setOnClickListener(v -> {
            Intent intent = new Intent(HistoriqueActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        chargerSignalementsDepuisFirestore();
    }

    private void chargerSignalementsDepuisFirestore() {
        FirebaseFirestore.getInstance().collection("signalements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    signalementList.clear();
                    int totalHeures = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String description = doc.getString("description");
                        String date = doc.getString("date");
                        String localisation = doc.getString("localisation");
                        String imagePath = doc.getString("imageUrl");

                        if (description != null && date != null && localisation != null) {
                            Signalement signalement = new Signalement(description, date, localisation, imagePath);
                            signalementList.add(signalement);

                            if (estDansLeMoisActuel(date)) {
                                totalHeures += 2;
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    statsTextView.setText("⏱ Temps sans courant ce mois : " + totalHeures + " h");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Erreur de récupération : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private boolean estDansLeMoisActuel(String date) {
        try {
            String[] parts = date.split("-");
            int jour = Integer.parseInt(parts[0]);
            int mois = Integer.parseInt(parts[1]);
            int annee = Integer.parseInt(parts[2]);

            Calendar maintenant = Calendar.getInstance();
            return (mois == maintenant.get(Calendar.MONTH) + 1) &&
                    (annee == maintenant.get(Calendar.YEAR));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void exportPDF() {
        if (signalementList.isEmpty()) {
            Toast.makeText(this, "Aucun signalement à exporter", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();

        int pageNumber = 1;
        int y = 50;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        canvas.drawText("Historique des coupures :", 40, y, paint);
        y += 30;

        for (Signalement s : signalementList) {
            if (y > 800) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }

            canvas.drawText("• " + s.getDescription(), 40, y, paint); y += 20;
            canvas.drawText("  📅 " + s.getDate(), 60, y, paint); y += 20;
            canvas.drawText("  📍 " + s.getLocalisation(), 60, y, paint); y += 30;
        }

        document.finishPage(page);

        // ✅ Sauvegarder dans Download
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File pdfFile = new File(downloadsDir, "historique_coupures.pdf");

        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();
            Toast.makeText(this, "✅ PDF exporté dans : " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Erreur PDF : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCSV() {
        if (signalementList.isEmpty()) {
            Toast.makeText(this, "Aucun signalement à exporter", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Sauvegarder dans Download
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadsDir, "historique_coupures.csv");

        try {
            FileWriter writer = new FileWriter(csvFile);
            writer.append("Description,Date,Localisation\n");

            for (Signalement s : signalementList) {
                writer.append(s.getDescription()).append(",");
                writer.append(s.getDate()).append(",");
                writer.append(s.getLocalisation()).append("\n");
            }

            writer.flush();
            writer.close();
            Toast.makeText(this, "✅ CSV exporté dans : " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Erreur CSV : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

