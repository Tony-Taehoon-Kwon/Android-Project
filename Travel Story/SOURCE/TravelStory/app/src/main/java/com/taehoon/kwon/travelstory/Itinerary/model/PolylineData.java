package com.taehoon.kwon.travelstory.Itinerary.model;

import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.model.DirectionsLeg;

import java.util.ArrayList;

public class PolylineData {

    public ArrayList<Polyline> polylines;
    public ArrayList<DirectionsLeg> legs;
    public int selectedPathIndex;
    public ArrayList<ArrayList<ParcelableGeoPoint>> paths_in_GeoPoint;

    public PolylineData(ArrayList<Polyline> polylines, ArrayList<DirectionsLeg> legs, int selectedPathIndex, ArrayList<ArrayList<ParcelableGeoPoint>> paths_in_GeoPoint) {
        this.polylines = polylines;
        this.legs = legs;
        this.selectedPathIndex = selectedPathIndex;
        this.paths_in_GeoPoint = paths_in_GeoPoint;
    }
}
