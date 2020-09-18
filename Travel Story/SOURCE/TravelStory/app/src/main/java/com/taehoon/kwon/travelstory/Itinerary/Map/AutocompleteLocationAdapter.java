package com.taehoon.kwon.travelstory.Itinerary.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class AutocompleteLocationAdapter extends RecyclerView.Adapter<AutocompleteLocationAdapter.ViewHolder> {

    private ArrayList<AutocompletePrediction> locationList;

    public AutocompleteLocationAdapter(ArrayList<AutocompletePrediction> locationList) {
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_auto_complete_location, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AutocompletePrediction location = locationList.get(position);

        String locationPrimaryText = location.getPrimaryText(null).toString();
        String locationSecondaryText = location.getSecondaryText(null).toString();

        if (location != null) {
            holder.locationPrimaryText.setText(locationPrimaryText);
            holder.locationSecondaryText.setText(locationSecondaryText);
        }
    }

    @Override
    public int getItemCount() { return locationList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView locationPrimaryText;
        TextView locationSecondaryText;

        public ViewHolder(View itemView) {
            super(itemView);
            locationPrimaryText = itemView.findViewById(R.id.autocompletePrimaryText);
            locationSecondaryText = itemView.findViewById(R.id.autocompleteSecondaryText);
        }
    }
}
