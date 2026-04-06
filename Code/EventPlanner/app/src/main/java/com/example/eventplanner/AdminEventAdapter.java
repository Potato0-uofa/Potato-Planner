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
 * RecyclerView adapter used by administrators to display events
 * and perform moderation actions such as removing events.
 */

public class  AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    /**
     * Interface used to notify the activity when an admin
     * performs an action on an event.
     */
    public interface OnEventActionListener {
        void onDeleteClicked(Events event, int position);
    }

    /** List of events displayed by this adapter. */
    private final List<Events> events;

    /** Callback listener for event moderation actions. */
    private final OnEventActionListener listener;

    /**
     * Constructs an adapter with the given event list and action listener.
     *
     * @param events   list of events to display
     * @param listener callback for delete actions
     */
    public AdminEventAdapter(List<Events> events, OnEventActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Inflates the event item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new EventViewHolder
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder at the given position.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Events event = events.get(position);

        holder.txtEventName.setText(safeText(event.getName(), "Untitled Event"));
        holder.txtEventDate.setText("Date: " + safeText(event.getDate(), "N/A"));
        holder.txtEventLocation.setText("Location: " + safeText(event.getLocation(), "N/A"));
        holder.txtEventStatus.setText("Status: " + safeText(event.getStatus(), "N/A"));

        holder.btnRemoveEvent.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onDeleteClicked(event, holder.getAdapterPosition());
            }
        });
    }

    /**
     * Returns the total number of events in the list.
     *
     * @return the event count
     */
    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    /** ViewHolder for an admin event row containing name, date, location, status, and a remove button. */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        /** TextViews displaying event name, date, location, and status. */
        TextView txtEventName, txtEventDate, txtEventLocation, txtEventStatus;
        /** Button to remove the event. */
        Button btnRemoveEvent;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param itemView the inflated item layout
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txt_admin_event_name);
            txtEventDate = itemView.findViewById(R.id.txt_admin_event_date);
            txtEventLocation = itemView.findViewById(R.id.txt_admin_event_location);
            txtEventStatus = itemView.findViewById(R.id.txt_admin_event_status);
            btnRemoveEvent = itemView.findViewById(R.id.btn_admin_remove_event);
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