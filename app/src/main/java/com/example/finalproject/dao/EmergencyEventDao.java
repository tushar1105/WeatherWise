package com.example.finalproject.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.finalproject.model.EmergencyEvent;

import java.util.List;

@Dao
public interface EmergencyEventDao {

    @Insert
    void insert(EmergencyEvent event);

    @Query("SELECT * FROM emergency_events")
    LiveData<List<EmergencyEvent>> getAllEvents();
}
