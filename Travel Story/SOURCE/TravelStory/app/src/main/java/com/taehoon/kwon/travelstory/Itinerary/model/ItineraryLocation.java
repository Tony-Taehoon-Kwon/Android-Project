package com.taehoon.kwon.travelstory.Itinerary.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.List;

public class ItineraryLocation implements Parcelable {

    private String name;
    private String description;
    private GeoPoint geo_point;
    private List<ParcelableGeoPoint> direction_points_from_previous_location;
    private List<String> images_url_list;
    private int index_of_selected_path_from_previous_location;
    private Date time_arrival;
    private Date time_departure;

    public ItineraryLocation() {}

    protected ItineraryLocation(Parcel in) {
        name = in.readString();
        description = in.readString();
        images_url_list = in.readArrayList(String.class.getClassLoader());
        direction_points_from_previous_location = in.readArrayList(ParcelableGeoPoint.class.getClassLoader());
    }

    public static final Creator<ItineraryLocation> CREATOR = new Creator<ItineraryLocation>() {
        @Override
        public ItineraryLocation createFromParcel(Parcel in) {
            return new ItineraryLocation(in);
        }

        @Override
        public ItineraryLocation[] newArray(int size) {
            return new ItineraryLocation[size];
        }
    };

    public GeoPoint getGeo_point() { return geo_point; }

    public void setGeo_point(GeoPoint geo_point) { this.geo_point = geo_point; }

    public List<ParcelableGeoPoint> getDirection_points_from_previous_location() { return direction_points_from_previous_location; }

    public void setDirection_points_from_previous_location(List<ParcelableGeoPoint> direction_points_from_previous_location) { this.direction_points_from_previous_location = direction_points_from_previous_location; }

    public int getIndex_of_selected_path_from_previous_location() { return index_of_selected_path_from_previous_location; }

    public void setIndex_of_selected_path_from_previous_location(int index_of_selected_path_from_previous_location) { this.index_of_selected_path_from_previous_location = index_of_selected_path_from_previous_location; }

    public List<String> getImages_url_list() { return images_url_list; }

    public void setImages_url_list(List<String> images_url_list) { this.images_url_list = images_url_list; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Date getTime_arrival() { return time_arrival; }

    public void setTime_arrival(Date time_arrival) { this.time_arrival = time_arrival; }

    public Date getTime_departure() { return time_departure; }

    public void setTime_departure(Date time_departure) { this.time_departure = time_departure; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeList(images_url_list);
        dest.writeList(direction_points_from_previous_location);
    }
}
