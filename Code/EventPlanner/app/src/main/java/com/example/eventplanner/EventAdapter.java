package com.example.eventplanner;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** RecyclerView adapter for event cards, routing to organizer or entrant views based on role. */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /** List of events displayed by this adapter. */
    private final List<Events> events;

    /**
     * Constructs an adapter with the given event list.
     *
     * @param events list of events to display
     */
    public EventAdapter(List<Events> events) {
        this.events = events;
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
                .inflate(R.layout.item_event_browse, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the ViewHolder and sets up the click listener
     * to route to the organizer or entrant view based on the user's role.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Events event = events.get(position);
        holder.tvName.setText(event.getName() != null ? event.getName() : "Unnamed Event");
        holder.tvCategory.setText(event.getCategory() != null ? event.getCategory() : "No Category");
        holder.tvStatus.setText(event.getStatus() != null ? event.getStatus() : "Open");

        holder.itemView.setOnClickListener(v -> {
            String deviceId = android.provider.Settings.Secure.getString(
                    v.getContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            boolean isOrganizer = deviceId.equals(event.getOrganizerId());
            boolean isCoOrganizer = event.getCoOrganizerIds() != null &&
                    event.getCoOrganizerIds().contains(deviceId);

            if (isOrganizer) {
                Intent intent = new Intent(v.getContext(), CreateEventActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("isPrivate", event.isPrivate());
                v.getContext().startActivity(intent);

            } else if (isCoOrganizer) {
                Intent intent = new Intent(v.getContext(), EventDescriptionView.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("eventName", event.getName());
                intent.putExtra("eventDescription", event.getDescription());
                v.getContext().startActivity(intent);

            } else {
                Intent intent = new Intent(v.getContext(), EventDescriptionView.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("eventName", event.getName());
                intent.putExtra("eventDescription", event.getDescription());
                v.getContext().startActivity(intent);
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
        return events.size();
    }

    /** ViewHolder for an event card containing name, category, and status. */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        /** TextViews for event name, category, and status. */
        TextView tvName, tvCategory, tvStatus;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param itemView the inflated item layout
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvCategory = itemView.findViewById(R.id.tv_event_category);
            tvStatus = itemView.findViewById(R.id.tv_event_status);
        }
    }
}