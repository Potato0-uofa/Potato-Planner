package com.example.eventplanner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * RecyclerView adapter used to display images uploaded for events.
 * Allows administrators to review and remove images if necessary.
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    public interface OnImageActionListener {
        void onRemoveImage(String imageUrl, int position);
    }

    private final List<String> images;
    private final OnImageActionListener listener;

    public AdminImageAdapter(List<String> images, OnImageActionListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = images.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .into(holder.imgEvent);

        holder.btnRemoveImage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveImage(imageUrl, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }
    static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEvent;
        Button btnRemoveImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEvent = itemView.findViewById(R.id.img_admin_event);
            btnRemoveImage = itemView.findViewById(R.id.btn_remove_image);
        }
    }
}