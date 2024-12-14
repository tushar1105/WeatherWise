package com.example.finalproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.adapter.EmergencyEventAdapter;
import com.example.finalproject.dao.AppDatabase;
import com.example.finalproject.externalApi.WeatherApiService;
import com.example.finalproject.externalApi.WeatherResponse;
import com.example.finalproject.model.EmergencyEvent;
import com.example.finalproject.retrofit.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerView;
    private EmergencyEventAdapter adapter;
    private MapView mapView;
    private TextView weatherInfo;

    // Google Map instance
    private GoogleMap googleMap;

    // List to hold emergency events
    private List<EmergencyEvent> events;

    // Location Client
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        mapView = findViewById(R.id.map_view);
        recyclerView = findViewById(R.id.recycler_view);
        Button addEventButton = findViewById(R.id.add_event_button);
        weatherInfo = findViewById(R.id.weather_info);

        // Initialize events list and adapter
        events = new ArrayList<>();
        adapter = new EmergencyEventAdapter(events);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize Location Client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            googleMap = map;
            loadMarkers(); // Load markers when map is ready

            // Set a marker click listener
            googleMap.setOnMarkerClickListener(marker -> {
                marker.showInfoWindow(); // Show the bubble with description
                return true;
            });
        });

        // Set up "Add Event" button click listener
        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        // Load events from the database
        loadEventsFromDatabase();

        // Fetch current location and weather data
        fetchCurrentLocationWeather();
    }

    /**
     * Fetches the current device location and displays weather information for that location.
     */
    // Initialize LocationRequest with the new API
    LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setWaitForAccurateLocation(true)
            .build();

    // Create LocationCallback to receive location updates
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult != null) {
                // Get the most recent location
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Use the location (latitude, longitude) as needed
                    fetchWeatherData(location);
                }
            }
        }
    };

    private void fetchCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null && locationResult.getLocations() != null) {
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                fetchWeatherData(location);
                            } else {
                                // Use default location (Waterloo) if location is null
                                //weatherInfo.setText("Unable to fetch location. Defaulting to Waterloo.");
                                fetchWeatherData(null);
                            }
                        }
                    }
                },
                Looper.getMainLooper());
    }

    /**
     * Fetches weather data for the given location.
     *
     * @param location Current device location
     */
    private void fetchWeatherData(Location location) {
        // Default location for Waterloo, ON if location is not available
        double defaultLatitude = 43.4621;  // Latitude for Waterloo, ON
        double defaultLongitude = -80.5400;  // Longitude for Waterloo, ON

        // If location is null, use the default (Waterloo)
        if (location == null) {
            location = new Location("default");
            location.setLatitude(defaultLatitude);
            location.setLongitude(defaultLongitude);
        }

        // Use the location (latitude, longitude) for weather data
        WeatherApiService weatherApiService = RetrofitClient.getInstance().create(WeatherApiService.class);
        String apiKey = "e492236b9a6a9b51adf68579c84b552a";

        Call<WeatherResponse> call = weatherApiService.getWeather(location.getLatitude(),location.getLongitude(),apiKey,"metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();
                    String weatherText = "City: " + weatherResponse.getCityName() +
                            "\nTemperature: " + weatherResponse.getMain().getTemp() + "\u00B0C" +
                            "\nWeather: " + weatherResponse.getWeather().get(0).getDescription();
                    weatherInfo.setText(weatherText);
                } else {
                    // Log detailed response and error code
                    Log.e("WeatherApi", "Response error: " + response.code() + " " + response.message());
                    weatherInfo.setText("Failed to fetch weather data.");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Log the error message for debugging
                Log.e("WeatherApi", "API call failure: " + t.getMessage());

                // Show default weather info (Waterloo) in case of error
                String defaultWeatherText = "City: Waterloo, ON\nTemperature: 10°C\nWeather: Clear sky";
                weatherInfo.setText(defaultWeatherText);
            }
        });
    }


    /**
     * Loads markers for emergency events onto the map.
     */
    private void loadMarkers() {
        if (googleMap != null) {
            googleMap.clear(); // Clear existing markers

            for (EmergencyEvent event : events) {
                LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(event.getEventType())
                        .snippet(event.getEventDescription())); // Show description in bubble
            }
        }
    }

    /**
     * Fetches all emergency events from the database and updates the UI.
     */
    private void loadEventsFromDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);

        // Observe the database for changes
        db.emergencyEventDao().getAllEvents().observe(this, eventList -> {
            events.clear();
            events.addAll(eventList);
            adapter.notifyDataSetChanged();

            // Update map markers
            loadMarkers();
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocationWeather();
        } else {
            weatherInfo.setText("Permission denied.");
            fetchWeatherData(null); // Use default location if permission is denied
        }
    }
}
