package com.example.coupureapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignalementActivity extends AppCompatActivity {

    EditText descriptionEditText, latitudeEditText, longitudeEditText;
    Button locationButton, captureImageButton, sendButton, returnToMainButton;
    TextView locationTextView;
    ProgressBar progressBar;
    ImageView imagePreview;

    FusedLocationProviderClient fusedLocationClient;
    String localisation = "Inconnue";
    String imagePath = null;
    private Uri imageUri;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_LOCATION_PERMISSION = 1001;
    static final int REQUEST_CAMERA_PERMISSION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signalement);

        descriptionEditText = findViewById(R.id.descriptionEditText);
        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        locationButton = findViewById(R.id.locationButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        sendButton = findViewById(R.id.sendButton);
        returnToMainButton = findViewById(R.id.returnToMainButton);
        locationTextView = findViewById(R.id.locationTextView);
        progressBar = findViewById(R.id.progressBar);
        imagePreview = findViewById(R.id.imagePreview);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationButton.setOnClickListener(v -> obtenirLocalisationEtMettreAJour());

        captureImageButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                ouvrirCamera();
            } else {
                requestCameraPermission();
            }
        });

        sendButton.setOnClickListener(v -> {
            String description = descriptionEditText.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une description.", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            obtenirLocalisationEtEnregistrer(description);
        });

        returnToMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignalementActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean checkCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void ouvrirCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CoupureApp");
                if (!folder.exists()) folder.mkdirs();
                String fileName = "img_" + System.currentTimeMillis() + ".jpg";
                photoFile = new File(folder, fileName);
                imageUri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && imageUri != null) {
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(View.VISIBLE);
            imagePath = imageUri.getPath(); // déjà au bon format
        }
    }

    private String enregistrerImageLocalement(Bitmap bitmap) {
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CoupureApp");
        if (!folder.exists()) folder.mkdirs();

        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        File file = new File(folder, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void enregistrerSignalement(String description) {
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (imagePath != null) {
            Uri fileUri = Uri.fromFile(new File(imagePath));
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child("images/" + fileUri.getLastPathSegment());

            UploadTask uploadTask = storageRef.putFile(fileUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    enregistrerDansFirestore(description, localisation, date, imageUrl);
                });
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Erreur upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            enregistrerDansFirestore(description, localisation, date, null);
        }
    }

    private void enregistrerDansFirestore(String description, String localisation, String date, String imageUrl) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Signalement signalement = new Signalement(description, date, localisation, imageUrl);

        firestore.collection("signalements")
                .add(signalement)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Signalement enregistré", Toast.LENGTH_SHORT).show();
                    descriptionEditText.setText("");
                    latitudeEditText.setText("");
                    longitudeEditText.setText("");
                    locationTextView.setText("Localisation : inconnue");
                    imagePreview.setVisibility(View.GONE);
                    imagePath = null;
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur Firestore", Toast.LENGTH_SHORT).show();
                });
    }

    private void obtenirLocalisationEtMettreAJour() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        localisation = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
                        locationTextView.setText("Localisation: " + localisation);
                        latitudeEditText.setText(String.valueOf(location.getLatitude()));
                        longitudeEditText.setText(String.valueOf(location.getLongitude()));
                    } else {
                        Toast.makeText(this, "Localisation indisponible", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void obtenirLocalisationEtEnregistrer(String description) {
        String latStr = latitudeEditText.getText().toString().trim();
        String lngStr = longitudeEditText.getText().toString().trim();

        if (!latStr.isEmpty() && !lngStr.isEmpty()) {
            localisation = latStr + ", " + lngStr;
            enregistrerSignalement(description);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        localisation = location.getLatitude() + ", " + location.getLongitude();
                    }
                    enregistrerSignalement(description);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendButton.performClick();
        } else if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ouvrirCamera();
        } else {
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
        }
    }
}
