package com.example.coupureapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URL;
import java.util.List;

public class SignalementAdapter extends RecyclerView.Adapter<SignalementAdapter.ViewHolder> {

    private final List<Signalement> signalements;
    private final Context context;

    public SignalementAdapter(List<Signalement> signalements, Context context) {
        this.signalements = signalements;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description, date, localisation, type;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            description = view.findViewById(R.id.textDescription);
            date = view.findViewById(R.id.textDate);
            localisation = view.findViewById(R.id.textLocation);
            type = view.findViewById(R.id.textType);  // Ajout du champ type
            image = view.findViewById(R.id.imagePreview);
        }
    }

    @NonNull
    @Override
    public SignalementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Initialisation de la vue de l'élément RecyclerView
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_signalement, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Récupération de l'objet Signalement à la position donnée
        Signalement s = signalements.get(position);

        // Mise à jour des TextViews avec les informations de l'objet Signalement
        holder.description.setText(s.getDescription());
        holder.date.setText(s.getDate());
        holder.localisation.setText("📍 Localisation : " + s.getLocalisation());
        holder.type.setText("Type : " + s.getType());  // Ajout du type dans l'affichage

        // Chargement de l'image si un chemin est défini
        if (s.getImagePath() != null && !s.getImagePath().isEmpty()) {
            // Chargement de l'image dans un thread pour éviter le blocage de l'UI
            new Thread(() -> {
                try {
                    URL url = new URL(s.getImagePath());
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    holder.image.post(() -> {
                        holder.image.setImageBitmap(bitmap);
                        holder.image.setVisibility(View.VISIBLE);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.image.post(() -> holder.image.setVisibility(View.GONE));
                }
            }).start();
        } else {
            // Si aucun chemin d'image, cacher l'ImageView
            holder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return signalements.size();
    }
}
