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
 * Adapter for pending entrants list with a cancel button per row.
 * Used by organizers to cancel entrants who did not sign up (US 02.06.04).
 */
public class PendingEntrantAdapter extends RecyclerView.Adapter<PendingEntrantAdapter.ViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClicked(Entrant entrant, int position);
    }

    private final List<Entrant> entrants;
    private final OnCancelClickListener cancelListener;

    public PendingEntrantAdapter(List<Entrant> entrants, OnCancelClickListener cancelListener) {
        this.entrants = entrants;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_entrant, parent, false);
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

        holder.btnCancel.setOnClickListener(v ->
                cancelListener.onCancelClicked(entrant, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvName;
        public final TextView tvId;
        public final Button btnCancel;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_entrant_name);
            tvId = view.findViewById(R.id.tv_entrant_id);
            btnCancel = view.findViewById(R.id.btn_cancel_entrant);
        }
    }
}
