package com.taehoon.kwon.travelstory.Itinerary.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.ui.auth.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserStory implements Parcelable {

    private String title;
    private String title_image_url;
    private Date date_start;
    private Date date_end;
    private ArrayList<ItineraryLocation> itineraryLocationList;

    public UserStory() {}

    protected UserStory(Parcel in) {
        title = in.readString();
        title_image_url = in.readString();
        itineraryLocationList = in.createTypedArrayList(ItineraryLocation.CREATOR);
    }

    public static final Creator<UserStory> CREATOR = new Creator<UserStory>() {
        @Override
        public UserStory createFromParcel(Parcel in) {
            return new UserStory(in);
        }

        @Override
        public UserStory[] newArray(int size) {
            return new UserStory[size];
        }
    };

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getTitle_image_url() { return title_image_url; }

    public void setTitle_image_url(String title_image_url) { this.title_image_url = title_image_url; }

    public Date getDate_start() { return date_start; }

    public void setDate_start(Date date_start) { this.date_start = date_start; }

    public Date getDate_end() { return date_end; }

    public void setDate_end(Date date_end) { this.date_end = date_end; }

    public ArrayList<ItineraryLocation> getItineraryLocationList() { return itineraryLocationList; }

    public void setItineraryLocationList(ArrayList<ItineraryLocation> itineraryLocationList) { this.itineraryLocationList = itineraryLocationList; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(title_image_url);
        dest.writeList(itineraryLocationList);
    }
}
