package com.taehoon.kwon.travelstory.Itinerary.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;
import java.util.List;

public class ItineraryMapAdapter extends RecyclerView.Adapter<ItineraryMapAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ItineraryLocation> itineraryLocations;

    public ItineraryMapAdapter(Context context, ArrayList<ItineraryLocation> itineraryLocations) {
        this.context = context;
        this.itineraryLocations = itineraryLocations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_itinerary, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItineraryLocation location = itineraryLocations.get(position);
        holder.locationText.setText(location.getName());
        List<String> url_list = location.getImages_url_list();
        if (url_list != null && url_list.size() != 0) {
            Glide.with(context).load(url_list.get(0)).centerCrop().into(holder.locationImage);
            holder.locationImagePlaceholdertext.setVisibility(View.GONE);
        }
        else {
            holder.locationImagePlaceholdertext.setVisibility(View.VISIBLE);
            holder.locationImagePlaceholdertext.setText("Image");
        }
    }

    @Override
    public int getItemCount() {
        return itineraryLocations.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView locationImage;
        TextView  locationText;
        TextView  locationImagePlaceholdertext;

        public ViewHolder(View itemView) {
            super(itemView);
            locationImage = itemView.findViewById(R.id.locationImage);
            locationText = itemView.findViewById(R.id.locationText);
            locationImagePlaceholdertext = itemView.findViewById(R.id.image_placeholder_text);
        }
    }
}
