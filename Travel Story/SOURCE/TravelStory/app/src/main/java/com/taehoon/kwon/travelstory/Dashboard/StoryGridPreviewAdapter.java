package com.taehoon.kwon.travelstory.Dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class StoryGridPreviewAdapter extends RecyclerView.Adapter<StoryGridPreviewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserStory> userStories;

    public StoryGridPreviewAdapter(Context context, ArrayList<UserStory> userStories) {
        this.context = context;
        this.userStories = userStories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story_grid_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserStory userStory = userStories.get(position);
        holder.titleText.setText(userStory.getTitle());
        Glide.with(context).load(String.valueOf(userStory.getTitle_image_url()))
                .centerCrop()
                .into(holder.titleImage);
    }

    @Override
    public int getItemCount() {
        return userStories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView titleImage;
        TextView titleText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleImage = itemView.findViewById(R.id.storyGridPreviewImage);
            titleText = itemView.findViewById(R.id.storyGridPreviewTitleText);
        }
    }
}
