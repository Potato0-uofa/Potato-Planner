package com.example.eventplanner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InviteEntrantActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etSearch;
    private InviteUserAdapter adapter;
    private List<User> userList = new ArrayList<>();

    private UserRepository userRepository;
    private EventRepository eventRepository;

    private String eventId;

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

        adapter = new InviteUserAdapter(userList, user -> inviteUser(user));
        recyclerView.setAdapter(adapter);

        loadUsers();

        // 🔥 SEARCH LISTENER (correct place)
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

        eventRepository.fetchEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Events event) {

                if (event.getWaitingList() == null) {
                    event.setWaitingList(new WaitingList());
                }

                Entrant entrant = new Entrant(
                        user.getDeviceId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone()
                );

                event.getWaitingList().addEntrant(entrant);

                eventRepository.updateEvent(event, new EventRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(InviteEntrantActivity.this,
                                "User invited successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(InviteEntrantActivity.this,
                                "Failed to invite user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(InviteEntrantActivity.this,
                        "Failed to load event",
                        Toast.LENGTH_SHORT).show();
            }
        });
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