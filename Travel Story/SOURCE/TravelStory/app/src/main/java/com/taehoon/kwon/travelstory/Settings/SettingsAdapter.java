package com.taehoon.kwon.travelstory.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<SettingsContent> contents;

    public SettingsAdapter(Context context, ArrayList<SettingsContent> contents) {
        this.context = context;
        this.contents = contents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_settings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.content.setText(contents.get(position).title);
        holder.content_icon.setImageResource(contents.get(position).drawable_icon);
    }

    @Override
    public int getItemCount() { return contents.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView content;
        ImageView content_icon;
        LinearLayout content_container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            content = itemView.findViewById(R.id.settingContentText);
            content_icon = itemView.findViewById(R.id.settingContentIconImage);
            content_container = itemView.findViewById(R.id.settingContentContainer);
        }
    }
}
