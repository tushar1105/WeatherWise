package com.example.finalproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.dao.AppDatabase;
import com.example.finalproject.model.EmergencyEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText eventTypeInput, eventDescriptionInput, cityNameInput;
    private Button addEventButton, searchCityButton, cancelButton;
    private MapView mapView;
    private GoogleMap googleMap;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventTypeInput = findViewById(R.id.event_type_input);
        eventDescriptionInput = findViewById(R.id.event_description_input);
        cityNameInput = findViewById(R.id.city_name_input);
        addEventButton = findViewById(R.id.add_event_button);
        searchCityButton = findViewById(R.id.search_city_button);
        cancelButton = findViewById(R.id.btn_cancel);
        mapView = findViewById(R.id.map_view);

        executorService = Executors.newSingleThreadExecutor();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Handle adding event
        addEventButton.setOnClickListener(v -> addEmergencyEvent());

        // Handle city search
        searchCityButton.setOnClickListener(v -> {
            String cityName = cityNameInput.getText().toString().trim();
            if (!cityName.isEmpty()) {
                searchCity(cityName);
            } else {
                Toast.makeText(this, "Please enter a city name.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up "Cancel" button click listener
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddEventActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void searchCity(String cityName) {
        Geocoder geocoder = new Geocoder(this);
        executorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng cityLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                    runOnUiThread(() -> {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(cityLatLng).title(cityName));
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder().target(cityLatLng).zoom(10).build()));
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(AddEventActivity.this, "City not found.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(AddEventActivity.this, "Error finding city: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void addEmergencyEvent() {
        String eventType = eventTypeInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String cityName = cityNameInput.getText().toString().trim();

        if (eventType.isEmpty() || eventDescription.isEmpty() || cityName.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng cityLatLng = googleMap.getCameraPosition().target; // Get the map's current target position.

        EmergencyEvent event = new EmergencyEvent(eventType, eventDescription, cityLatLng.latitude, cityLatLng.longitude);
        AppDatabase db = AppDatabase.getInstance(this);

        executorService.execute(() -> {
            db.emergencyEventDao().insert(event);
            runOnUiThread(() -> {
                Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
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
        mapView.onDestroy();
        executorService.shutdown();
    }
}
