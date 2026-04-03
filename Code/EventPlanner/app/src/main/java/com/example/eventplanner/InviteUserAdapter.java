package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InviteUserAdapter extends RecyclerView.Adapter<InviteUserAdapter.UserViewHolder> {

    public interface OnInviteClickListener {
        void onInviteClicked(User user);
    }

    private final List<User> userList;
    private final OnInviteClickListener listener;

    public InviteUserAdapter(List<User> userList, OnInviteClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());

        holder.btnInvite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInviteClicked(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail;
        Button btnInvite;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_user_name);
            txtEmail = itemView.findViewById(R.id.txt_user_email);
            btnInvite = itemView.findViewById(R.id.btn_invite_user);
        }
    }
}
