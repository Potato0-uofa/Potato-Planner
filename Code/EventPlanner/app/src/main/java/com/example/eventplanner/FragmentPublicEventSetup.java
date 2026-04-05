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

/** Setup dialog for creating a public event with registration dates, capacity, and tags. */
public class FragmentPublicEventSetup extends DialogFragment {

    private final EventRepository eventRepository = new EventRepository();

    // Holds the tags the user selected via the Edit Tags dialog
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
        View view = inflater.inflate(R.layout.fragment_public_event_setup, container, false);

        EditText regStartInput = view.findViewById(R.id.registrationStartDate_public_setup);
        EditText regEndInput = view.findViewById(R.id.registrationEndDate_public_setup);
        CheckBox geolocationCheck = view.findViewById(R.id.geolocation_public_setup);
        CheckBox waitlistCheck = view.findViewById(R.id.max_capacity_check_public_setup);
        EditText waitlistCapacityInput = view.findViewById(R.id.waitlist_capacity_public_setup);
        EditText attendeeCountInput = view.findViewById(R.id.attendee_count_public_setup);

        // Auto-fill registration start date with today's date
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        regStartInput.setText(today);

        // Only enable capacity input when checkbox is checked
        waitlistCapacityInput.setEnabled(false);
        waitlistCheck.setOnCheckedChangeListener((btn, isChecked) ->
                waitlistCapacityInput.setEnabled(isChecked));

        view.findViewById(R.id.exit_button_new_public_event).setOnClickListener(v -> dismiss());

        // Edit Tags button — shows tag picker dialog, stores result in selectedTags
        view.findViewById(R.id.public_event_tags_button).setOnClickListener(v ->
                showTagPickerDialog());

        view.findViewById(R.id.public_event_photo_upload_button).setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        view.findViewById(R.id.public_event_create_button).setOnClickListener(v -> {
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
            event.setPrivate(false);
            event.setCapacity(attendeeCount);
            event.setTags(new ArrayList<>(selectedTags));

            if (waitlistCheck.isChecked()) {
                String capStr = waitlistCapacityInput.getText().toString().trim();
                if (!capStr.isEmpty()) {
                    event.setWaitlistLimit(Integer.parseInt(capStr));
                }
            }

            // Validate registration dates
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
                    intent.putExtra("isPrivate", false);
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

    /**
     * Shows the tag picker dialog using fragment_event_tags.xml.
     * Selections are saved to selectedTags and persist if the user
     * opens the dialog again (checkboxes will be pre-checked).
     */
    private void showTagPickerDialog() {
        View tagView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_event_tags, null);

        // Hide the title and exit button — not needed inside a dialog
        tagView.findViewById(R.id.exit_button_edit_event).setVisibility(View.GONE);
        tagView.findViewById(R.id.textView6).setVisibility(View.GONE);

        // Map tag names to their checkbox IDs
        String[] tagNames = {"Entertainment", "Sports", "Cooking", "Outdoors", "Gaming", "Music", "Active", "Art"};
        int[] checkboxIds = {
                R.id.entertainment_check, R.id.sports_check, R.id.cooking_check2,
                R.id.outdoors_check, R.id.gaming_check, R.id.music_check,
                R.id.active_check, R.id.art_check
        };

        // Pre-check any tags already selected
        for (int i = 0; i < tagNames.length; i++) {
            CheckBox cb = tagView.findViewById(checkboxIds[i]);
            cb.setChecked(selectedTags.contains(tagNames[i]));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Event Tags")
                .setView(tagView)
                .setPositiveButton("Done", (dialog, which) -> {
                    // Save selections back to selectedTags
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