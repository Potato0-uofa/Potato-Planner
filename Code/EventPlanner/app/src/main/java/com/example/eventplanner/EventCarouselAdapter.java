package com.example.eventplanner;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewPager2 adapter that displays public events in a horizontal carousel.
 * Each card shows the event image, name, organizer name, category, date, and location.
 */
public class EventCarouselAdapter extends RecyclerView.Adapter<EventCarouselAdapter.CarouselViewHolder> {

    private final List<Events> events;
    private final Map<String, String> organizerNames = new HashMap<>();
    private final UserRepository userRepository = new UserRepository();

    public EventCarouselAdapter(List<Events> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Events event = events.get(position);

        holder.tvName.setText(event.getName() != null ? event.getName() : "Unnamed Event");
        holder.tvCategory.setText(event.getCategory() != null ? event.getCategory() : "EVENT");
        holder.tvDate.setText(event.getDate() != null ? event.getDate() : "");
        holder.tvLocation.setText(event.getLocation() != null ? event.getLocation() : "");

        // Hide location row entirely if no location
        boolean hasLocation = event.getLocation() != null && !event.getLocation().isEmpty();
        holder.locationRow.setVisibility(hasLocation ? View.VISIBLE : View.GONE);

        // Load event image with Glide
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .transform(new CenterCrop())
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(0);
            holder.ivImage.setBackgroundColor(0xFF3A3347);
        }

        // Load organizer name (cached after first fetch)
        String organizerId = event.getOrganizerId();
        if (organizerId != null) {
            if (organizerNames.containsKey(organizerId)) {
                holder.tvOrganizer.setText(organizerNames.get(organizerId));
            } else {
                holder.tvOrganizer.setText("Loading...");
                userRepository.getUserByDeviceId(organizerId, new UserRepository.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        String name = (user != null && user.getName() != null) ? user.getName() : "Unknown";
                        organizerNames.put(organizerId, name);
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            holder.tvOrganizer.setText(name);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        holder.tvOrganizer.setText("Unknown");
                    }
                });
            }
        } else {
            holder.tvOrganizer.setText("Unknown");
        }

        // Tap opens event detail view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDescriptionView.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getName());
            intent.putExtra("eventDescription", event.getDescription());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvOrganizer, tvCategory, tvDate, tvLocation;
        LinearLayout locationRow;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.carousel_event_image);
            tvName = itemView.findViewById(R.id.carousel_event_name);
            tvOrganizer = itemView.findViewById(R.id.carousel_event_organizer);
            tvCategory = itemView.findViewById(R.id.carousel_event_category);
            tvDate = itemView.findViewById(R.id.carousel_event_date);
            tvLocation = itemView.findViewById(R.id.carousel_event_location);
            locationRow = itemView.findViewById(R.id.carousel_location_row);
        }
    }
}
