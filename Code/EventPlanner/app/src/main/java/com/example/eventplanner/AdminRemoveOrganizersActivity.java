package com.example.eventplanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows administrators to view organizers and remove
 * those who violate application policies.
 * Implements user story:
 * - US 03.07.01: Remove organizers and US 03.05.01
 */
public class AdminRemoveOrganizersActivity extends AppCompatActivity
        implements AdminOrganizerAdapter.OnOrganizerActionListener {

    /** RecyclerView used to display the list of organizers. */
    private RecyclerView recyclerView;

    /** Adapter that binds organizer data to the RecyclerView. */
    private AdminOrganizerAdapter adapter;

    /** In-memory list of organizers currently shown on screen. */
    private final List<User> organizerList = new ArrayList<>();

    /** Repository used to fetch and remove user records. */
    private final UserRepository userRepository = new UserRepository();

    /**
     * Initializes the activity, sets up the organizer list, and loads organizer data.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
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

    /**
     * Loads all users from the repository and displays them as removable organizers.
     * <p>
     * The current repository does not store organizer roles separately, so all users
     * are shown in this screen.
     */
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

    /**
     * Handles removal of an organizer after the admin presses the remove action.
     * A confirmation dialog is displayed before the user record is deleted.
     *
     * @param organizer organizer selected for removal
     * @param position position of the organizer in the displayed list
     */
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

    /**
     * Returns a safe display string for organizer names.
     *
     * @param value organizer name that may be null or blank
     * @return the original name if present; otherwise {@code "this organizer"}
     */
    private String safeText(String value) {
        return (value == null || value.trim().isEmpty()) ? "this organizer" : value;
    }
}