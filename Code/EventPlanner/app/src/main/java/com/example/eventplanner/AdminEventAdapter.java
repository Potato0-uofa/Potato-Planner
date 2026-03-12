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

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    /**
     * Interface used to notify the activity when an admin
     * performs an action on an event.
     */
    public interface OnEventActionListener {
        void onDeleteClicked(Events event, int position);
    }

    private final List<Events> events;
    private final OnEventActionListener listener;

    public AdminEventAdapter(List<Events> events, OnEventActionListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtEventName, txtEventDate, txtEventLocation, txtEventStatus;
        Button btnRemoveEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txt_admin_event_name);
            txtEventDate = itemView.findViewById(R.id.txt_admin_event_date);
            txtEventLocation = itemView.findViewById(R.id.txt_admin_event_location);
            txtEventStatus = itemView.findViewById(R.id.txt_admin_event_status);
            btnRemoveEvent = itemView.findViewById(R.id.btn_admin_remove_event);
        }
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}