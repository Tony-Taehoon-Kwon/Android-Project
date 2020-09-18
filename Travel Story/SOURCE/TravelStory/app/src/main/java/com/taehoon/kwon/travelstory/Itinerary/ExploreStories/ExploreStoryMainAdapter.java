package com.taehoon.kwon.travelstory.Itinerary.ExploreStories;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class ExploreStoryMainAdapter extends RecyclerView.Adapter<ExploreStoryMainAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserStory> userStories;
    private FragmentManager fragmentManager;

    public ExploreStoryMainAdapter(Context context, ArrayList<UserStory> userStories, FragmentManager fragmentManager) {
        this.context = context;
        this.userStories = userStories;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserStory userStory = userStories.get(position);
        holder.titleText.setText(userStory.getTitle());
        Glide.with(context).load(String.valueOf(userStory.getTitle_image_url())).centerCrop().into(holder.titleImage);
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
            titleImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Bundle args = new Bundle();
                    args.putParcelable("selected_story", userStories.get(position));

                    Fragment fragment = new ExploreStoryDetailFragment();
                    fragment.setArguments(args);

                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, fragment, ExploreStoryDetailFragment.FRAG_TAG)
                            .hide(fragmentManager.findFragmentByTag(ExploreStoryMainFragment.FRAG_TAG))
                            .commit();

                    MainActivity.current_fragment_tag = ExploreStoryDetailFragment.FRAG_TAG;
                }
            });
        }
    }
}
