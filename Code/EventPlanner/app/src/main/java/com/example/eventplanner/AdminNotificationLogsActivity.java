package com.example.eventplanner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminNotificationAdapter adapter;
    private List<Notification> logList = new ArrayList<>();

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

    private void loadLogs() {

        // 🔥 get all notifications
        List<Notification> notifications = CustomLogs.getNotificationLogs();

        logList.clear();
        logList.addAll(notifications);

        adapter.notifyDataSetChanged();
    }
}