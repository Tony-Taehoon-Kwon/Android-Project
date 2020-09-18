package com.taehoon.kwon.travelstory.Dashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.R;

import java.net.URL;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BitmapTransformation;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class StoryPreviewAdapter extends RecyclerView.Adapter<StoryPreviewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserStory> userStories;

    public StoryPreviewAdapter(Context context, ArrayList<UserStory> userStories) {
        this.context = context;
        this.userStories = userStories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserStory userStory = userStories.get(position);
        holder.titleText.setText(userStory.getTitle());

        if (userStory.getTitle_image_url().equals(String.valueOf(R.drawable.img_story_preview_placeholder)))
            Glide.with(context).load(R.drawable.img_story_preview_placeholder).centerCrop().into(holder.titleImage);
        else
            Glide.with(context).load(String.valueOf(userStory.getTitle_image_url()))
                    //.apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(100, 0)))
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

            titleImage = itemView.findViewById(R.id.storyPreviewImage);
            titleText = itemView.findViewById(R.id.storyPreviewTitleText);
        }
    }
}
