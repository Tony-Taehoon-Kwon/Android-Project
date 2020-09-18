package com.taehoon.kwon.travelstory.Itinerary.model;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MarkerList implements Serializable {

    //region Singleton Code
    private static volatile MarkerList _instance;

    private MarkerList() {
        // prevent from the reflection api
        if (_instance != null)
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
    }

    public static MarkerList getInstance() {
        if (_instance == null) {
            synchronized (MarkerList.class) {
                if (_instance == null)
                    _instance = new MarkerList();
            }
        }
        return _instance;
    }

    // make singleton from serialize and deserialize operation
    protected MarkerList readResolve() {
        return getInstance();
    }

    //endregion Singleton

    public List<RegisteredMarker> markers;
    public String storyTitle;

    public void CreateNewMarkerList(String storyTitle) {
        markers = new ArrayList<>();
        this.storyTitle = storyTitle;
    }

    public static class RegisteredMarker {
        public Marker marker = null;
        public Boolean registered = false;
        public List<ParcelableGeoPoint> direction_points_from_previous_location = new ArrayList<>();
        public int index_of_selected_path_from_previous_location = -1;

        public RegisteredMarker(Marker marker, Boolean registered) {
            this.marker = marker;
            this.registered = registered;
        }
    }
}
