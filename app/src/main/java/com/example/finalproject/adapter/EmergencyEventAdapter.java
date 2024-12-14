package com.example.finalproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.model.EmergencyEvent;

import java.util.List;

public class EmergencyEventAdapter extends RecyclerView.Adapter<EmergencyEventAdapter.ViewHolder> {

    private List<EmergencyEvent> events;

    public EmergencyEventAdapter(List<EmergencyEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyEvent event = events.get(position);

        // Set the event details
        holder.typeTextView.setText("Type: " + event.getEventType());
        holder.descriptionTextView.setText("Description: "+event.getEventDescription());
    }


    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView, locationTextView, descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.event_type);
            descriptionTextView = itemView.findViewById(R.id.event_description);
        }
    }
}
