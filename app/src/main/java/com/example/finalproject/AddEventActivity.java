package com.example.finalproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.dao.AppDatabase;
import com.example.finalproject.model.EmergencyEvent;

public class AddEventActivity extends AppCompatActivity {

    private EditText typeInput, locationInput, dateInput, statusInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        typeInput = findViewById(R.id.event_type_input);
        locationInput = findViewById(R.id.event_location_input);
        dateInput = findViewById(R.id.event_date_input);
        statusInput = findViewById(R.id.event_status_input);

        Button saveEventButton = findViewById(R.id.save_event_button);
        saveEventButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String type = typeInput.getText().toString();
        String location = locationInput.getText().toString();
        String date = dateInput.getText().toString();
        String status = statusInput.getText().toString();

        EmergencyEvent event = new EmergencyEvent();
        event.setType(type);
        event.setLocation(location);
        event.setDate(date);
        event.setStatus(status);

        AppDatabase db = AppDatabase.getInstance(this);
        db.emergencyEventDao().insert(event);

        Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
