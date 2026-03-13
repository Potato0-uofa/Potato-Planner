package com.example.eventplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    private final List<Entrant> entrants;

    public WaitlistAdapter(List<Entrant> entrants) {
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        holder.text1.setText(entrant.getName() != null ? entrant.getName() : "Anonymous");
        holder.text2.setText(entrant.getEmail() != null ? entrant.getEmail() : "No Email");
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView text1;
        public final TextView text2;

        public ViewHolder(View view) {
            super(view);
            text1 = view.findViewById(android.R.id.text1);
            text2 = view.findViewById(android.R.id.text2);
        }
    }
}