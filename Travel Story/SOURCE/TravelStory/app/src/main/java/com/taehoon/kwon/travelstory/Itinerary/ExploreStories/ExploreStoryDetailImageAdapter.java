package com.taehoon.kwon.travelstory.Itinerary.ExploreStories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.taehoon.kwon.travelstory.R;

import java.util.List;

public class ExploreStoryDetailImageAdapter extends RecyclerView.Adapter<ExploreStoryDetailImageAdapter.ViewHolder> {

    private Context context;
    private List<String> url_list;

    public ExploreStoryDetailImageAdapter(Context context, List<String> url_list) {
        this.context = context;
        this.url_list = url_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story_detail_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(url_list.get(position)).centerCrop().into(holder.storyDetailImage);
    }

    @Override
    public int getItemCount() { return url_list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView storyDetailImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyDetailImage = itemView.findViewById(R.id.storyDetailImage);
        }
    }
}
