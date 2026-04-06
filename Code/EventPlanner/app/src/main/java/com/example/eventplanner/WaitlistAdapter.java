package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** RecyclerView adapter for displaying entrant names and emails in a waitlist. */
public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    /** List of entrants displayed in the waitlist. */
    private final List<Entrant> entrants;

    /**
     * Constructs an adapter with the given entrant list.
     *
     * @param entrants list of entrants to display
     */
    public WaitlistAdapter(List<Entrant> entrants) {
        this.entrants = entrants;
    }

    /**
     * Inflates the waitlist entrant item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waitlist_entrant, parent, false);
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
    }

    /**
     * Returns the total number of entrants in the list.
     *
     * @return the entrant count
     */
    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /** ViewHolder for a waitlist entrant row containing name and ID/email. */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the entrant's name. */
        public final TextView tvName;
        /** TextView displaying the entrant's email or device ID. */
        public final TextView tvId;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param view the inflated item layout
         */
        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_entrant_name);
            tvId = view.findViewById(R.id.tv_entrant_id);
        }
    }
}