package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.LogViewHolder> {

    private final List<Notification> logList;

    public AdminNotificationAdapter(List<Notification> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        Notification log = logList.get(position);

        holder.txtSender.setText("From: " + log.getSenderName());
        holder.txtReceiver.setText("To: " + log.getReceiverName());
        holder.txtEvent.setText("Event: " + log.getEventName());
        holder.txtMessage.setText(log.getMessage());
    }

    @Override
    public int getItemCount() {
        return logList == null ? 0 : logList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView txtSender, txtReceiver, txtEvent, txtMessage;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSender = itemView.findViewById(R.id.txt_sender);
            txtReceiver = itemView.findViewById(R.id.txt_receiver);
            txtEvent = itemView.findViewById(R.id.txt_event);
            txtMessage = itemView.findViewById(R.id.txt_message);
        }
    }
}
