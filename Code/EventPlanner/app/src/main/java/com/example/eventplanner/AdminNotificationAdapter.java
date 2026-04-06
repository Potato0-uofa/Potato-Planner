package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** RecyclerView adapter for displaying notification log entries in the admin view. */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.LogViewHolder> {

    /** List of notification log entries displayed by this adapter. */
    private final List<Notification> logList;

    /**
     * Constructs an adapter with the given notification log list.
     *
     * @param logList list of notifications to display
     */
    public AdminNotificationAdapter(List<Notification> logList) {
        this.logList = logList;
    }

    /**
     * Inflates the notification log item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new LogViewHolder
     */
    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new LogViewHolder(view);
    }

    /**
     * Binds notification log data to the ViewHolder at the given position.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Notification log = logList.get(position);

        holder.txtSender.setText("From: " + log.getSenderName());
        holder.txtReceiver.setText("To: " + log.getReceiverName());
        holder.txtEvent.setText("Event: " + log.getEventName());
        holder.txtMessage.setText(log.getMessage());
    }

    /**
     * Returns the total number of notification log entries.
     *
     * @return the log entry count
     */
    @Override
    public int getItemCount() {
        return logList == null ? 0 : logList.size();
    }

    /** ViewHolder for a notification log row containing sender, receiver, event, and message. */
    static class LogViewHolder extends RecyclerView.ViewHolder {

        /** TextViews for sender, receiver, event name, and message content. */
        TextView txtSender, txtReceiver, txtEvent, txtMessage;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param itemView the inflated item layout
         */
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txt_sender);
            txtReceiver = itemView.findViewById(R.id.txt_receiver);
            txtEvent = itemView.findViewById(R.id.txt_event);
            txtMessage = itemView.findViewById(R.id.txt_message);
        }
    }
}
