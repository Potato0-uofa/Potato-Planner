package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
/**
 * RecyclerView adapter used to display organizers to administrators
 * and allow them to remove organizers from the system.
 */
public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    /**
     * Listener interface for organizer removal actions triggered by the admin.
     */
    public interface OnOrganizerActionListener {
        /**
         * Called when the admin clicks the remove button for an organizer.
         *
         * @param organizer the organizer to remove
         * @param position  the adapter position of the organizer
         */
        void onRemoveClicked(User organizer, int position);
    }

    /** List of organizers displayed by this adapter. */
    private final List<User> organizers;

    /** Callback listener for organizer removal actions. */
    private final OnOrganizerActionListener listener;

    /**
     * Constructs an adapter with the given organizer list and action listener.
     *
     * @param organizers list of organizers to display
     * @param listener   callback for removal actions
     */
    public AdminOrganizerAdapter(List<User> organizers, OnOrganizerActionListener listener) {
        this.organizers = organizers;
        this.listener = listener;
    }

    /**
     * Inflates the organizer item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new OrganizerViewHolder
     */
    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }

    /**
     * Binds organizer data to the ViewHolder at the given position.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        User organizer = organizers.get(position);

        holder.txtName.setText(safeText(organizer.getName(), "Unknown Organizer"));
        holder.txtEmail.setText("Email: " + safeText(organizer.getEmail(), "N/A"));
        holder.txtPhone.setText("Phone: " + safeText(organizer.getPhone(), "N/A"));

        holder.btnRemoveOrganizer.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onRemoveClicked(organizer, holder.getAdapterPosition());
            }
        });
    }

    /**
     * Returns the total number of organizers in the list.
     *
     * @return the organizer count
     */
    @Override
    public int getItemCount() {
        return organizers == null ? 0 : organizers.size();
    }

    /** ViewHolder for an organizer row containing name, email, phone, and a remove button. */
    static class OrganizerViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the organizer's name. */
        TextView txtName, txtEmail, txtPhone;
        /** Button to remove the organizer. */
        Button btnRemoveOrganizer;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param itemView the inflated item layout
         */
        public OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_admin_organizer_name);
            txtEmail = itemView.findViewById(R.id.txt_admin_organizer_email);
            txtPhone = itemView.findViewById(R.id.txt_admin_organizer_phone);
            btnRemoveOrganizer = itemView.findViewById(R.id.btn_admin_remove_organizer);
        }
    }

    /**
     * Returns the value if non-null and non-empty, otherwise returns the fallback.
     *
     * @param value    the string to check
     * @param fallback the fallback string if value is empty
     * @return the safe display text
     */
    private String safeText(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}