package com.taehoon.kwon.travelstory.Itinerary.ExploreStories;

import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExploreStoryDetailAdapter extends RecyclerView.Adapter<ExploreStoryDetailAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItineraryLocation> itineraryLocations;

    public ExploreStoryDetailAdapter(Context context, ArrayList<ItineraryLocation> itineraryLocations) {
        this.context = context;
        this.itineraryLocations = itineraryLocations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItineraryLocation itineraryLocation = itineraryLocations.get(position);
        holder.storyDetailDateText.setText(getDateTimeDurationInString(itineraryLocation.getTime_arrival(), itineraryLocation.getTime_departure()));
        holder.storyDetailTitleText.setText(itineraryLocation.getName());
        holder.storyDetailDescriptionText.setText(itineraryLocation.getDescription());
        holder.exploreStoryDetailImageAdapter = new ExploreStoryDetailImageAdapter(context, itineraryLocation.getImages_url_list());
        holder.storyDetailRecyclerView.setAdapter(holder.exploreStoryDetailImageAdapter);
        holder.storyDetailRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    }

    @Override
    public int getItemCount() { return itineraryLocations.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView storyDetailDateText;
        TextView storyDetailTitleText;
        TextView storyDetailDescriptionText;
        RecyclerView storyDetailRecyclerView;
        ExploreStoryDetailImageAdapter exploreStoryDetailImageAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyDetailDateText = itemView.findViewById(R.id.storyDetailDateText);
            storyDetailTitleText = itemView.findViewById(R.id.storyDetailTitleText);
            storyDetailDescriptionText = itemView.findViewById(R.id.storyDetailDescriptionText);
            storyDetailRecyclerView = itemView.findViewById(R.id.storyDetailRecyclerView);
        }
    }

    private String getDateTimeDurationInString (Date arrivalTime, Date departureTime) {

        String[] arrival_temp = arrivalTime.toString().split(" ", 6); // DayOfWeek Month DayOfMonth Time TimeZone Year
        String[] departure_temp = departureTime.toString().split(" ", 6); // DayOfWeek Month DayOfMonth Time TimeZone Year
        String[] arrival_temp2 = arrival_temp[3].split(":",3); // hh:mm::ss
        String[] departure_temp2 = departure_temp[3].split(":",3); // hh:mm::ss
        String arrivalTimeAmPm = getTimeInAmPmFormat(Integer.parseInt(arrival_temp2[0]), Integer.parseInt(arrival_temp2[1]));
        String departureTimeAmPm = getTimeInAmPmFormat(Integer.parseInt(departure_temp2[0]), Integer.parseInt(departure_temp2[1]));

        StringBuilder builder = new StringBuilder();
        builder.append(arrival_temp[1]);
        builder.append(" ");
        builder.append(arrival_temp[2]);
        builder.append(" ");
        builder.append(arrivalTimeAmPm);
        builder.append(" ~ ");
        builder.append(departure_temp[1]);
        builder.append(" ");
        builder.append(departure_temp[2]);
        builder.append(" ");
        builder.append(departureTimeAmPm);

        return builder.toString();
    }

    private String getTimeInAmPmFormat(int hourOfDay, int minute) {

        StringBuilder builder = new StringBuilder();
        String amPm = "";

        if (hourOfDay == 0) {
            hourOfDay = 12;
            amPm = " am";
        }
        else if (hourOfDay == 12) {
            amPm = " pm";
        }
        else if (hourOfDay >= 13) {
            hourOfDay -= 12;
            amPm = " pm";
        }
        else {
            amPm = " am";
        }
        builder.append(hourOfDay);
        builder.append(":");
        builder.append(minute);
        builder.append(amPm);
        return builder.toString();
    }
}
