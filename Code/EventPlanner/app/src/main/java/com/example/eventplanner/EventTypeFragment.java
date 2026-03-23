package com.example.eventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class EventTypeFragment extends DialogFragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_type, container, false);

        // Exit button dismisses the dialog
        view.findViewById(R.id.exit_button_new_event_fragment).setOnClickListener(v -> dismiss());

        // Handle Public/Private event buttons here
        view.findViewById(R.id.public_event_button).setOnClickListener(v -> {
            // TODO: open public event creation
            dismiss();
        });

        view.findViewById(R.id.private_event_button).setOnClickListener(v -> {
            // TODO: open private event creation
            dismiss();
        });

        return view;
    }
}
