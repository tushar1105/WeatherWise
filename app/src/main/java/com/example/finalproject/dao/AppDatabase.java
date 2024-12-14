package com.example.finalproject.dao;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.finalproject.model.EmergencyEvent;

@Database(entities = {EmergencyEvent.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract EmergencyEventDao emergencyEventDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "emergency_event_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
