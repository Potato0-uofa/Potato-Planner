package com.example.eventplanner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Activity allowing organizers to search for users and send event invitations. */
public class InviteEntrantActivity extends AppCompatActivity {

    public static final String EXTRA_INVITE_TYPE = "inviteType";
    public static final String INVITE_TYPE_WAITLIST = "waitlist";
    public static final String INVITE_TYPE_COORGANIZER = "coorganizer";

    private RecyclerView recyclerView;
    private EditText etSearch;
    private InviteUserAdapter adapter;
    private List<User> userList = new ArrayList<>();

    private UserRepository userRepository;
    private EventRepository eventRepository;

    private String eventId;
    private String inviteType = INVITE_TYPE_WAITLIST;
    private boolean isPrivateEvent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_entrant);

        etSearch = findViewById(R.id.et_search_user);

        recyclerView = findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRepository = new UserRepository();
        eventRepository = new EventRepository();

        eventId = getIntent().getStringExtra("eventId");
        String rawInviteType = getIntent().getStringExtra(EXTRA_INVITE_TYPE);
        if (rawInviteType != null && !rawInviteType.trim().isEmpty()) {
            inviteType = rawInviteType;
        }

        if (eventId != null && !eventId.trim().isEmpty()) {
            eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
                @Override
                public void onSuccess(Events event) {
                    isPrivateEvent = event != null && event.isPrivate();
                }

                @Override
                public void onFailure(Exception e) {
                    isPrivateEvent = false;
                }
            });
        }

        adapter = new InviteUserAdapter(userList, user -> inviteUser(user));
        recyclerView.setAdapter(adapter);

        loadUsers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        userRepository.fetchAllUsers(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteEntrantActivity.this,
                        "Failed to load users",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inviteUser(User user) {
        if (user == null || user.getDeviceId() == null || user.getDeviceId().trim().isEmpty()) {
            Toast.makeText(InviteEntrantActivity.this,
                    "User does not exist",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(InviteEntrantActivity.this,
                    "Missing event ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RegistrationRepository registrationRepository = new RegistrationRepository();
        RegistrationRepository.SimpleCallback inviteCallback = new RegistrationRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (INVITE_TYPE_COORGANIZER.equals(inviteType)) {
                    Toast.makeText(InviteEntrantActivity.this,
                            "Co-organizer invitation sent",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isPrivateEvent) {
                    Toast.makeText(InviteEntrantActivity.this,
                            "Private waitlist invitation sent",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Public event invitation: track as pending entrant as well.
                Map<String, Object> data = new HashMap<>();
                data.put("pendingEntrants", FieldValue.arrayUnion(user.getDeviceId()));
                FirebaseFirestore.getInstance()
                        .collection("events")
                        .document(eventId)
                        .update(data)
                        .addOnSuccessListener(unused -> Toast.makeText(
                                InviteEntrantActivity.this,
                                "User invited successfully",
                                Toast.LENGTH_SHORT
                        ).show())
                        .addOnFailureListener(e -> Toast.makeText(
                                InviteEntrantActivity.this,
                                "Invited but failed to update event",
                                Toast.LENGTH_SHORT
                        ).show());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteEntrantActivity.this,
                        "Failed to invite user",
                        Toast.LENGTH_SHORT).show();
            }
        };

        if (INVITE_TYPE_COORGANIZER.equals(inviteType)) {
            registrationRepository.inviteUserToCoOrganizer(eventId, user.getDeviceId(), inviteCallback);
        } else if (isPrivateEvent) {
            registrationRepository.inviteUserToPrivateWaitlist(eventId, user.getDeviceId(), inviteCallback);
        } else {
            registrationRepository.inviteUserToEvent(eventId, user.getDeviceId(), inviteCallback);
        }
    }

    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();

        for (User user : userList) {
            if ((user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getPhone() != null && user.getPhone().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(user);
            }
        }

        adapter = new InviteUserAdapter(filteredList, user -> inviteUser(user));
        recyclerView.setAdapter(adapter);
    }
}
