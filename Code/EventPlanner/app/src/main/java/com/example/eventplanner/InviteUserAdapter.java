package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** RecyclerView adapter displaying users with an invite button for organizer use. */
public class InviteUserAdapter extends RecyclerView.Adapter<InviteUserAdapter.UserViewHolder> {

    /**
     * Listener interface for invite button clicks.
     */
    public interface OnInviteClickListener {
        /**
         * Called when the invite button is clicked for a user.
         *
         * @param user the user to invite
         */
        void onInviteClicked(User user);
    }

    /** List of users displayed by this adapter. */
    private final List<User> userList;

    /** Callback listener for invite actions. */
    private final OnInviteClickListener listener;

    /** Label text for the invite button. */
    private String buttonLabel = "Invite";

    /**
     * Constructs an adapter with the given user list and invite listener.
     *
     * @param userList list of users to display
     * @param listener callback for invite actions
     */
    public InviteUserAdapter(List<User> userList, OnInviteClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    /**
     * Inflates the invite user item layout and creates a new ViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds user data to the ViewHolder at the given position.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the item in the adapter
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());
        holder.btnInvite.setText(buttonLabel);

        holder.btnInvite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInviteClicked(user);
            }
        });
    }

    /**
     * Sets the label text displayed on the invite button.
     *
     * @param label the button label text
     */
    public void setButtonLabel(String label) {
        this.buttonLabel = label;
    }

    /**
     * Returns the total number of users in the list.
     *
     * @return the user count
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /** ViewHolder for a user row containing name, email, and an invite button. */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        /** TextViews for user name and email. */
        TextView txtName, txtEmail;
        /** Button to invite the user. */
        Button btnInvite;

        /**
         * Constructs the ViewHolder and binds the views.
         *
         * @param itemView the inflated item layout
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_user_name);
            txtEmail = itemView.findViewById(R.id.txt_user_email);
            btnInvite = itemView.findViewById(R.id.btn_invite_user);
        }
    }
}
