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

    private final List<Events> events;

    public EventAdapter(List<Events> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_browse, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Events event = events.get(position);
        holder.tvName.setText(event.getName() != null ? event.getName() : "Unnamed Event");
        holder.tvCategory.setText(event.getCategory() != null ? event.getCategory() : "No Category");
        holder.tvStatus.setText(event.getStatus() != null ? event.getStatus() : "Open");

        holder.itemView.setOnClickListener(v -> {
            // Get the current device ID
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

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvStatus;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvCategory = itemView.findViewById(R.id.tv_event_category);
            tvStatus = itemView.findViewById(R.id.tv_event_status);
        }
    }
}