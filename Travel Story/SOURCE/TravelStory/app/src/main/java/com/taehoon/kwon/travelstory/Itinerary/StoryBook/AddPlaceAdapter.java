package com.taehoon.kwon.travelstory.Itinerary.StoryBook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taehoon.kwon.travelstory.EventTrigger;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class AddPlaceAdapter extends RecyclerView.Adapter<AddPlaceAdapter.ViewHolder> {

    public ArrayList<ItineraryLocation> placeList;
    public boolean isPlaceRemoved;
    public int removedPlaceIndex;
    private EventTrigger eventTrigger;

    public AddPlaceAdapter(ArrayList<ItineraryLocation> placeList, EventTrigger eventTrigger) {
        this.placeList = placeList;
        this.eventTrigger = eventTrigger;
        isPlaceRemoved = false;
        removedPlaceIndex = -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_add_place_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String place = placeList.get(position).getName();
        holder.removePlaceButton.setText(place);
    }

    @Override
    public int getItemCount() { return placeList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        Button removePlaceButton;

        public ViewHolder(View itemView) {
            super(itemView);
            removePlaceButton = itemView.findViewById(R.id.removePlaceButton);
            removePlaceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setMessage("Are you sure you want to remove " + removePlaceButton.getText() + "?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    int position = getAdapterPosition();
                                    setRemovedIndexForBrokenPath(position);
                                    placeList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, placeList.size());
                                    isPlaceRemoved = true;
                                    Log.d("Remove Test", "Remove Test : Adapter - isPlaceRemoved = " + isPlaceRemoved);

                                    if (removedPlaceIndex != -1)
                                        eventTrigger.trigger();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        private void setRemovedIndexForBrokenPath(int position) {
            if (position == placeList.size() -1  // last index
             || position == 0) // first index
                return;

            removedPlaceIndex = position;
        }
    }
}
