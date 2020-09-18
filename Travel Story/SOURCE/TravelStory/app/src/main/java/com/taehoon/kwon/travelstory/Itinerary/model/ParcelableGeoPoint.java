package com.taehoon.kwon.travelstory.Itinerary.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

public class ParcelableGeoPoint implements Parcelable {
    private double latitude;
    private double longitude;

    public ParcelableGeoPoint() {}

    public ParcelableGeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ParcelableGeoPoint(GeoPoint geoPoint) {
        this.latitude = geoPoint.getLatitude();
        this.longitude = geoPoint.getLongitude();
    }

    protected ParcelableGeoPoint(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<ParcelableGeoPoint> CREATOR = new Creator<ParcelableGeoPoint>() {
        @Override
        public ParcelableGeoPoint createFromParcel(Parcel in) {
            return new ParcelableGeoPoint(in);
        }

        @Override
        public ParcelableGeoPoint[] newArray(int size) {
            return new ParcelableGeoPoint[size];
        }
    };

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
