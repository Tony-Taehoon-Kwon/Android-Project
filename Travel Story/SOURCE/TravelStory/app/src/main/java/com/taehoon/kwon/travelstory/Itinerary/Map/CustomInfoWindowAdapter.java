package com.taehoon.kwon.travelstory.Itinerary.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.taehoon.kwon.travelstory.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View infoWindow;
    private Context infoContext;

    public CustomInfoWindowAdapter(Context context) {
        infoContext = context;
        infoWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void renderWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        String snippet = marker.getSnippet();
        TextView titleView = view.findViewById(R.id.infoTitle);
        TextView snippetView = view.findViewById(R.id.infoSnippet);

        if (!title.equals(""))
            titleView.setText(title);

        if (!snippet.equals(""))
            snippetView.setText(snippet);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, infoWindow);
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, infoWindow);
        return infoWindow;
    }
}
