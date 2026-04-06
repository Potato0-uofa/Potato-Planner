package com.example.eventplanner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Admin screen that displays all notification logs across the system. */
public class AdminNotificationLogsActivity extends AppCompatActivity {

    /** RecyclerView for displaying notification log entries. */
    private RecyclerView recyclerView;

    /** Adapter binding notification data to the RecyclerView. */
    private AdminNotificationAdapter adapter;

    /** In-memory list of notification logs displayed on screen. */
    private List<Notification> logList = new ArrayList<>();

    /**
     * Initializes the activity, sets up the RecyclerView, and loads notification logs.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notification_logs);

        recyclerView = findViewById(R.id.recycler_logs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminNotificationAdapter(logList);
        recyclerView.setAdapter(adapter);

        loadLogs();
    }

    /**
     * Loads all notification log entries from the custom log store and populates the list.
     */
    private void loadLogs() {

        // 🔥 get all notifications
        CustomLogs logs = new CustomLogs() {};
        List<Notification> notifications = logs.getNotificationLogs();

        logList.clear();
        logList.addAll(notifications);

        adapter.notifyDataSetChanged();
    }
}
