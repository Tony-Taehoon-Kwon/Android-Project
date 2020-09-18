package com.taehoon.kwon.travelstory.Itinerary.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class AutocompleteLocationListAdapter extends ArrayAdapter<AutocompletePrediction> {

    private final Context context;
    private final ArrayList<AutocompletePrediction> autocompletePredictions;

    public AutocompleteLocationListAdapter(Context context, ArrayList<AutocompletePrediction> autocompletePredictions) {
        super(context, R.layout.layout_auto_complete_location, autocompletePredictions);
        this.context = context;
        this.autocompletePredictions = autocompletePredictions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_auto_complete_location, parent, false);
        TextView locationPrimaryText = v.findViewById(R.id.autocompletePrimaryText);
        TextView locationSecondaryText = v.findViewById(R.id.autocompleteSecondaryText);
        locationPrimaryText.setText(autocompletePredictions.get(position).getPrimaryText(null).toString());
        locationSecondaryText.setText(autocompletePredictions.get(position).getSecondaryText(null).toString());
        return v;
    }
}
