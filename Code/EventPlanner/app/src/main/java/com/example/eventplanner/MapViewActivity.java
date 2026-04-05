package com.example.eventplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventplanner.EntrantLocation;
import com.example.eventplanner.EventTypeFragment;
import com.example.eventplanner.HomePage;
import com.example.eventplanner.LocationRepository;
import com.example.eventplanner.NonAdminBrowseEvents;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Displays a Google Map showing red dot markers for every entrant's
 * last-known location on the waiting list for a specific event.
 *
 * Locations are read in real-time from:
 *   events/{eventId}/attendee_locations/{deviceId}
 *
 * Launch with:
 *   Intent intent = new Intent(this, MapViewActivity.class);
 *   intent.putExtra("eventId", eventId);
 *   startActivity(intent);
 */
public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;

    private LocationRepository locationRepository;
    private ListenerRegistration locationsListener;

    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        eventId = getIntent().getStringExtra("eventId");
        locationRepository = new LocationRepository();

        findViewById(R.id.exit_button_waitlist).setOnClickListener(v -> finish());

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); // calls onMapReady when ready
    }

    /**
     * Called when the GoogleMap is ready to use.
     * Starts the real-time location listener and plots markers.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (eventId == null || eventId.isEmpty()) {
            return;
        }

        startListeningToLocations();
    }


    /**
     * Attaches a real-time listener to the attendee_locations subcollection.
     * Every update clears the existing markers and redraws them so the map
     * always reflects the current state.
     */
    private void startListeningToLocations() {
        locationsListener = locationRepository.listenToLocations(eventId,
                new LocationRepository.LocationsCallback() {
                    @Override
                    public void onUpdate(List<EntrantLocation> locations) {
                        if (googleMap == null) return;

                        // Clear old markers before redrawing
                        googleMap.clear();

                        if (locations.isEmpty()) return;

                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                        for (EntrantLocation loc : locations) {
                            LatLng position = new LatLng(loc.getLat(), loc.getLng());

                            googleMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(loc.getDeviceId())
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            boundsBuilder.include(position);
                        }

                        try {
                            LatLngBounds bounds = boundsBuilder.build();
                            mapView.post(() -> {
                                try {
                                    googleMap.animateCamera(
                                            CameraUpdateFactory.newLatLngBounds(bounds, 150));
                                } catch (Exception ex) {
                                    if (!locations.isEmpty()) {
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                                new LatLng(locations.get(0).getLat(),
                                                        locations.get(0).getLng()), 12f));
                                    }
                                }
                            });
                        } catch (Exception e) {
                            if (!locations.isEmpty()) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(locations.get(0).getLat(),
                                                locations.get(0).getLng()), 12f));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Silently fail — map just stays empty
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationsListener != null) locationsListener.remove();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}