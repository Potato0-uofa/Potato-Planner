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

    /**
     * Listener interface for cancel actions on pending entrants.
     */
    public interface OnCancelClickListener {
        /**
         * Called when the organizer clicks cancel on a pending entrant.
         *
         * @param entrant  the entrant to cancel
         * @param position the adapter position of the entrant
         */
        void onCancelClicked(Entrant entrant, int position);
    }

    /** List of pending entrants displayed by this adapter. */
    private final List<Entrant> entrants;

    /** Callback listener for cancel actions. */
    private final OnCancelClickListener cancelListener;

    /**
     * Constructs an adapter with the given entrant list and cancel listener.
     *
     * @param entrants       list of pending entrants to display
     * @param cancelListener callback for cancel actions
     */
    public PendingEntrantAdapter(List<Entrant> entrants, OnCancelClickListener cancelListener) {
        this.entrants = entrants;
        this.cancelListener = cancelListener;
    }

    /**
     * Inflates the pending entrant item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_entrant, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds entrant data to the ViewHolder at the given position.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
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

    /**
     * Returns the total number of pending entrants.
     *
     * @return the entrant count
     */
    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /** ViewHolder for a pending entrant row containing name, ID, and a cancel button. */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the entrant's name. */
        public final TextView tvName;
        /** TextView displaying the entrant's email or ID. */
        public final TextView tvId;
        /** Button to cancel the pending entrant. */
        public final Button btnCancel;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param view the inflated item layout
         */
        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_entrant_name);
            tvId = view.findViewById(R.id.tv_entrant_id);
            btnCancel = view.findViewById(R.id.btn_cancel_entrant);
        }
    }
}
