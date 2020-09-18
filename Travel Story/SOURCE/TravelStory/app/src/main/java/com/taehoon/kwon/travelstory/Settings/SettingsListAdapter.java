package com.taehoon.kwon.travelstory.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class SettingsListAdapter extends ArrayAdapter<SettingsContent> {

    private final Context context;
    private final ArrayList<SettingsContent> contents;

    public SettingsListAdapter(Context context, ArrayList<SettingsContent> contents) {
        super(context, R.layout.layout_settings, contents);
        this.context = context;
        this.contents = contents;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_settings, parent, false);
        TextView content = v.findViewById(R.id.settingContentText);
        ImageView content_icon = v.findViewById(R.id.settingContentIconImage);
        content.setText(contents.get(position).title);
        content_icon.setImageResource(contents.get(position).drawable_icon);
        return v;
    }
}
