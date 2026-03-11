package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.OrganizerViewHolder> {

    public interface OnOrganizerActionListener {
        void onRemoveClicked(User organizer, int position);
    }

    private final List<User> organizers;
    private final OnOrganizerActionListener listener;

    public AdminOrganizerAdapter(List<User> organizers, OnOrganizerActionListener listener) {
        this.organizers = organizers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new OrganizerViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return organizers == null ? 0 : organizers.size();
    }

    static class OrganizerViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtPhone;
        Button btnRemoveOrganizer;

        public OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_admin_organizer_name);
            txtEmail = itemView.findViewById(R.id.txt_admin_organizer_email);
            txtPhone = itemView.findViewById(R.id.txt_admin_organizer_phone);
            btnRemoveOrganizer = itemView.findViewById(R.id.btn_admin_remove_organizer);
        }
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}