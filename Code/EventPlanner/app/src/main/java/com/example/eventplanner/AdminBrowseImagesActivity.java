package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private final List<String> eventIds = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_images);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recycler_admin_images);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminImageAdapter(imageUrls, (imageUrl, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Image")
                    .setMessage("Are you sure you want to remove this image?")
                    .setPositiveButton("Remove", (dialog, which) -> removeImage(imageUrl, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        loadImages();
    }

    private void loadImages() {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    imageUrls.clear();
                    eventIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Events event = doc.toObject(Events.class);
                        if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
                            imageUrls.add(event.getImageUrl());
                            eventIds.add(doc.getId());
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

    private void removeImage(String imageUrl, int position) {
        if (position < 0 || position >= eventIds.size()) return;

        String eventId = eventIds.get(position);

        // Remove imageUrl from the Firestore event document
        db.collection("events").document(eventId)
                .update("imageUrl", null)
                .addOnSuccessListener(aVoid -> {
                    // Also delete from Firebase Storage
                    try {
                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                        imageRef.delete().addOnFailureListener(e -> {
                            // Storage file may already be gone — not critical
                        });
                    } catch (IllegalArgumentException e) {
                        // URL wasn't a valid Storage reference — skip
                    }

                    imageUrls.remove(position);
                    eventIds.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to remove image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
