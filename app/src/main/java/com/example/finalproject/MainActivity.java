package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.adapter.EmergencyEventAdapter;
import com.example.finalproject.dao.AppDatabase;
import com.example.finalproject.model.EmergencyEvent;
import com.example.finalproject.externalApi.WeatherApiService;
import com.example.finalproject.externalApi.WeatherResponse;
import com.example.finalproject.retrofit.RetrofitClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Setup MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            googleMap = map;
            loadMarkers(); // Load markers when map is ready
        });

        // Set up "Add Event" button click listener
        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            startActivity(intent);
        });

        // Load events from the database
        loadEventsFromDatabase();

        // Fetch weather data
        fetchWeatherData("London"); // Replace with dynamic location if needed
    }

    /**
     * Loads markers for emergency events onto the map.
     */
    private void loadMarkers() {
        if (googleMap != null) {
            googleMap.clear(); // Clear existing markers

            for (EmergencyEvent event : events) {
                LatLng location = new LatLng(43.4643, 80.5204);
                googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(event.getType()));
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

    /**
     * Fetches weather data from the OpenWeatherMap API.
     *
     * @param cityName Name of the city for which to fetch weather data
     */
    private void fetchWeatherData(String cityName) {
        WeatherApiService weatherApiService = RetrofitClient.getInstance().create(WeatherApiService.class);
        String apiKey = "e492236b9a6a9b51adf68579c84b552a"; // Replace with your actual API key

        Call<WeatherResponse> call = weatherApiService.getWeather(cityName, apiKey,"metric");
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
                    weatherInfo.setText("Failed to fetch weather data.");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherInfo.setText("Error: " + t.getMessage());
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
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
