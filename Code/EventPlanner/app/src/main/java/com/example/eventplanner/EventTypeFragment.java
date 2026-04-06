package com.example.eventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/** Dialog fragment that lets the user choose between creating a public or private event. */
public class EventTypeFragment extends DialogFragment {

    /**
     * Inflates the event type selection layout and sets up click listeners
     * for the public and private event buttons.
     *
     * @param inflater           the LayoutInflater to inflate views
     * @param container          the parent ViewGroup
     * @param savedInstanceState previously saved fragment state, if any
     * @return the inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_type, container, false);

        view.findViewById(R.id.exit_button_new_event_fragment).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.public_event_button).setOnClickListener(v -> {
            dismiss();
            FragmentPublicEventSetup fragment = new FragmentPublicEventSetup();
            fragment.show(requireActivity().getSupportFragmentManager(), "PublicEventSetup");
        });

        view.findViewById(R.id.private_event_button).setOnClickListener(v -> {
            dismiss();
            FragmentPrivateEventSetup fragment = new FragmentPrivateEventSetup();
            fragment.show(requireActivity().getSupportFragmentManager(), "PrivateEventSetup");
        });

        return view;
    }
}