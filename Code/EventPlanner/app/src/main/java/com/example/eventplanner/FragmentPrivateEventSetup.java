package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class FragmentPrivateEventSetup extends DialogFragment {

    private final EventRepository eventRepository = new EventRepository();

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

        // Auto-fill registration start date with today's date
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
        regStartInput.setText(today);

        // Only enable capacity input when checkbox is checked
        waitlistCapacityInput.setEnabled(false);
        waitlistCheck.setOnCheckedChangeListener((btn, isChecked) ->
                waitlistCapacityInput.setEnabled(isChecked));

        view.findViewById(R.id.exit_button_new_private_event).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.private_event_create_button).setOnClickListener(v -> {
            String regStart = regStartInput.getText().toString().trim();
            String regEnd = regEndInput.getText().toString().trim();
            String attendeeCountStr = attendeeCountInput.getText().toString().trim();


            // User must enter registration dates (end registration date, start registration date
            // is pre-filled, but user can choose to change if desired)
            if (regStart.isEmpty() || regEnd.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in registration dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // User must enter the number of attendees for the event
            if (attendeeCountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter the number of attendees", Toast.LENGTH_SHORT).show();
                return;
            }

            // Checks to see that the number of attendees is greater than 0
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

            Events event = new Events(
                    "New Event",
                    regEnd,
                    "Add a description...",
                    ""
            );
            event.setOrganizerId(organizerId);
            event.setRegistrationStart(regStart);
            event.setRegistrationEnd(regEnd);
            event.setGeolocationRequired(geolocationCheck.isChecked());
            event.setPrivate(true);
            event.setCapacity(attendeeCount);


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

                // Checks to see if registration start date is in the past
                java.util.Date startDate = sdf.parse(regStart);
                if (startDate != null && startDate.before(todayDate)) {
                    Toast.makeText(getContext(), "Registration start date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Checks to see if registration end date is in the past
                java.util.Date endDate = sdf.parse(regEnd);
                if (endDate != null && endDate.before(todayDate)) {
                    Toast.makeText(getContext(), "Registration end date cannot be in the past", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Checks to see if the registration end date is before the start date
                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    Toast.makeText(getContext(), "Registration end date cannot be before start date", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Checks to see if the date format inputted by the user is correct
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