package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows administrators to browse uploaded images
 * associated with events.
 * Implements user story:
 * - US 03.06.01: Browse uploaded images
 */
public class AdminBrowseImagesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;
    private final List<String> imageUrls = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_images);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_admin_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminImageAdapter(imageUrls, (imageUrl, position) -> {
            Toast.makeText(this, "Remove image not implemented yet", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);

        loadImages();
    }

    private void loadImages() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    imageUrls.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Events event = doc.toObject(Events.class);
                        if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
                            imageUrls.add(event.getImageUrl());
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (imageUrls.isEmpty()) {
                        Toast.makeText(this, "No uploaded images found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load images: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}