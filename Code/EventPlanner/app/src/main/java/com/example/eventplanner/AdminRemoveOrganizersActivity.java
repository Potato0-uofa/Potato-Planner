package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminRemoveOrganizersActivity extends AppCompatActivity
        implements AdminOrganizerAdapter.OnOrganizerActionListener {

    private RecyclerView recyclerView;
    private AdminOrganizerAdapter adapter;
    private final List<User> organizerList = new ArrayList<>();
    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_remove_organizers);

        recyclerView = findViewById(R.id.recycler_admin_organizers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminOrganizerAdapter(organizerList, this);
        recyclerView.setAdapter(adapter);

        loadOrganizers();
    }

    private void loadOrganizers() {
        userRepository.fetchAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                organizerList.clear();

                // Current repo does not store roles.
                // For now, this screen displays users as removable organizers.
                organizerList.addAll(users);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminRemoveOrganizersActivity.this,
                        "Failed to load organizers: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRemoveClicked(User organizer, int position) {
        if (organizer == null || organizer.getDeviceId() == null || organizer.getDeviceId().trim().isEmpty()) {
            Toast.makeText(this, "This organizer cannot be removed.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove Organizer")
                .setMessage("Are you sure you want to remove " + safeText(organizer.getName()) + "?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    userRepository.deleteUser(organizer.getDeviceId(), new UserRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            organizerList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(AdminRemoveOrganizersActivity.this,
                                    "Organizer removed successfully.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminRemoveOrganizersActivity.this,
                                    "Failed to remove organizer: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String safeText(String value) {
        return (value == null || value.trim().isEmpty()) ? "this organizer" : value;
    }
}