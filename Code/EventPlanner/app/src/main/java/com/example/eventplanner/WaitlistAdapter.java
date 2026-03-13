package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    private final List<Entrant> entrants;

    public WaitlistAdapter(List<Entrant> entrants) {
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        
        String name = entrant.getName();
        if (name == null || name.trim().isEmpty()) {
            name = "User: " + entrant.getDeviceId();
        }
        
        String email = entrant.getEmail();
        if (email == null || email.trim().isEmpty()) {
            email = "ID: " + entrant.getDeviceId();
        }

        holder.tvName.setText(name);
        holder.tvId.setText(email);
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvName;
        public final TextView tvId;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_entrant_name);
            tvId = view.findViewById(R.id.tv_entrant_id);
        }
    }
}