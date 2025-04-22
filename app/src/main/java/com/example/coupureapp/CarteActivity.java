package com.example.coupureapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CarteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firestore;
    private Button btnFiltrerDate, btnFiltrerZone, btnBackMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carte);

        firestore = FirebaseFirestore.getInstance();

        btnFiltrerDate = findViewById(R.id.btnFiltrerDate);
        btnFiltrerZone = findViewById(R.id.btnFiltrerZone);
        btnBackMenu = findViewById(R.id.btnbackmenu);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFiltrerDate.setOnClickListener(v -> {
            Toast.makeText(this, "Filtrage par date non implémenté", Toast.LENGTH_SHORT).show();
        });

        btnFiltrerZone.setOnClickListener(v -> {
            Toast.makeText(this, "Filtrage par zone non implémenté", Toast.LENGTH_SHORT).show();
        });

        // Retour à l'accueil (MainActivity)
        btnBackMenu.setOnClickListener(v -> {
            Intent intent = new Intent(CarteActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Fermer cette activité
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        chargerCoupures();
    }

    private void chargerCoupures() {
        firestore.collection("signalements")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String type = doc.getString("type"); // signalée, planifiée, résolue
                        String localisation = doc.getString("localisation");

                        if (localisation != null && localisation.contains(",")) {
                            String[] coords = localisation.split(",");
                            double lat = Double.parseDouble(coords[0].trim());
                            double lng = Double.parseDouble(coords[1].trim());

                            LatLng position = new LatLng(lat, lng);

                            float color = BitmapDescriptorFactory.HUE_RED;
                            if ("planifiée".equalsIgnoreCase(type)) {
                                color = BitmapDescriptorFactory.HUE_ORANGE;
                            } else if ("résolue".equalsIgnoreCase(type)) {
                                color = BitmapDescriptorFactory.HUE_GREEN;
                            }

                            mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title("Coupure " + type)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        }
                    }

                    if (!query.isEmpty()) {
                        QueryDocumentSnapshot first = query.iterator().next();
                        String localisation = first.getString("localisation");
                        if (localisation != null && localisation.contains(",")) {
                            String[] coords = localisation.split(",");
                            LatLng center = new LatLng(Double.parseDouble(coords[0].trim()), Double.parseDouble(coords[1].trim()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10f));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
