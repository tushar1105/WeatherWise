package com.example.finalproject.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emergency_events")
public class EmergencyEvent {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String eventType;
    private String eventDescription;
    private double latitude;
    private double longitude;

    // Constructor
    public EmergencyEvent(String eventType, String eventDescription, double latitude, double longitude) {
        this.eventType = eventType;
        this.eventDescription = eventDescription;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
