package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/** Setup dialog for creating a private event with registration dates, capacity, and tags. */
public class FragmentPrivateEventSetup extends DialogFragment {

    private final EventRepository eventRepository = new EventRepository();
    private final List<String> selectedTags = new ArrayList<>();

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_event_setup, container, false);

        EditText regStartInput = view.findViewById(R.id.registrationStartDate_private_setup);
        EditText regEndInput = view.findViewById(R.id.registrationEndDate_private_setup);
        CheckBox geolocationCheck = view.findViewById(R.id.geolocation_private_setup);
        CheckBox waitlistCheck = view.findViewById(R.id.max_capacity_check_private_setup);
        EditText waitlistCapacityInput = view.findViewById(R.id.waitlist_capacity_private_setup);
        EditText attendeeCountInput = view.findViewById(R.id.attendee_count_private_setup);

        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        regStartInput.setText(today);

        waitlistCapacityInput.setEnabled(false);
        waitlistCheck.setOnCheckedChangeListener((btn, isChecked) ->
                waitlistCapacityInput.setEnabled(isChecked));

        view.findViewById(R.id.exit_button_new_private_event).setOnClickListener(v -> dismiss());

        // Edit Tags button
        view.findViewById(R.id.private_event_tags_button).setOnClickListener(v ->
                showTagPickerDialog());

        view.findViewById(R.id.private_event_photo_upload_button).setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        view.findViewById(R.id.private_event_create_button).setOnClickListener(v -> {
            String regStart = regStartInput.getText().toString().trim();
            String regEnd = regEndInput.getText().toString().trim();
            String attendeeCountStr = attendeeCountInput.getText().toString().trim();

            if (regStart.isEmpty() || regEnd.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in registration dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (attendeeCountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter the number of attendees", Toast.LENGTH_SHORT).show();
                return;
            }

            int attendeeCount = Integer.parseInt(attendeeCountStr);
            if (attendeeCount <= 0) {
                Toast.makeText(getContext(), "Number of attendees must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            @SuppressLint("HardwareIds")
            String organizerId = Settings.Secure.getString(
                    requireActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            Events event = new Events("New Event", regEnd, "Add a description...", "");
            event.setOrganizerId(organizerId);
            event.setRegistrationStart(regStart);
            event.setRegistrationEnd(regEnd);
            event.setGeolocationRequired(geolocationCheck.isChecked());
            event.setPrivate(true);
            event.setCapacity(attendeeCount);
            event.setTags(new ArrayList<>(selectedTags));

            if (waitlistCheck.isChecked()) {
                String capStr = waitlistCapacityInput.getText().toString().trim();
                if (!capStr.isEmpty()) {
                    event.setWaitlistLimit(Integer.parseInt(capStr));
                }
            }

            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                java.util.Date todayDate = cal.getTime();

                java.util.Date startDate = sdf.parse(regStart);
                if (startDate != null && startDate.before(todayDate)) {
                    Toast.makeText(getContext(), "Registration start date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }

                java.util.Date endDate = sdf.parse(regEnd);
                if (endDate != null && endDate.before(todayDate)) {
                    Toast.makeText(getContext(), "Registration end date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    Toast.makeText(getContext(), "Registration end date cannot be before start date", Toast.LENGTH_SHORT).show();
                    return;
                }

            } catch (java.text.ParseException e) {
                Toast.makeText(getContext(), "Invalid date format. Please use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }

            eventRepository.createEvent(event, new EventRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    dismiss();
                    Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("isPrivate", true);
                    if (selectedImageUri != null) {
                        intent.putExtra("imageUri", selectedImageUri.toString());
                    }
                    startActivity(intent);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to create event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }

    private void showTagPickerDialog() {
        View tagView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_event_tags, null);

        tagView.findViewById(R.id.exit_button_edit_event).setVisibility(View.GONE);
        tagView.findViewById(R.id.textView6).setVisibility(View.GONE);

        String[] tagNames = {"Entertainment", "Sports", "Cooking", "Outdoors", "Gaming", "Music", "Active", "Art"};
        int[] checkboxIds = {
                R.id.entertainment_check, R.id.sports_check, R.id.cooking_check2,
                R.id.outdoors_check, R.id.gaming_check, R.id.music_check,
                R.id.active_check, R.id.art_check
        };

        for (int i = 0; i < tagNames.length; i++) {
            CheckBox cb = tagView.findViewById(checkboxIds[i]);
            cb.setChecked(selectedTags.contains(tagNames[i]));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Event Tags")
                .setView(tagView)
                .setPositiveButton("Done", (dialog, which) -> {
                    selectedTags.clear();
                    for (int i = 0; i < tagNames.length; i++) {
                        CheckBox cb = tagView.findViewById(checkboxIds[i]);
                        if (cb.isChecked()) selectedTags.add(tagNames[i]);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}