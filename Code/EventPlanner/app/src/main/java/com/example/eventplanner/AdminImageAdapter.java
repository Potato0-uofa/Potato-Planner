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

    /**
     * Listener interface for image removal actions triggered by the admin.
     */
    public interface OnImageActionListener {
        /**
         * Called when the admin clicks the remove button for an image.
         *
         * @param imageUrl the URL of the image to remove
         * @param position the adapter position of the image
         */
        void onRemoveImage(String imageUrl, int position);
    }

    /** List of image URLs displayed by this adapter. */
    private final List<String> images;

    /** Callback listener for image removal actions. */
    private final OnImageActionListener listener;

    /**
     * Constructs an adapter with the given image URL list and action listener.
     *
     * @param images   list of image URLs to display
     * @param listener callback for removal actions
     */
    public AdminImageAdapter(List<String> images, OnImageActionListener listener) {
        this.images = images;
        this.listener = listener;
    }

    /**
     * Inflates the image item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new ImageViewHolder
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds image data to the ViewHolder at the given position using Glide.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
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

    /**
     * Returns the total number of images in the list.
     *
     * @return the image count
     */
    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    /** ViewHolder for an admin image row containing the image preview and a remove button. */
    static class ImageViewHolder extends RecyclerView.ViewHolder {

        /** ImageView displaying the event image. */
        ImageView imgEvent;
        /** Button to remove the image. */
        Button btnRemoveImage;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param itemView the inflated item layout
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEvent = itemView.findViewById(R.id.img_admin_event);
            btnRemoveImage = itemView.findViewById(R.id.btn_remove_image);
        }
    }
}