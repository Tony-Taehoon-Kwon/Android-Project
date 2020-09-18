package com.taehoon.kwon.travelstory.Itinerary.ExploreStories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;
import com.taehoon.kwon.travelstory.OnBackPressed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExploreStoryDetailFragment extends ExploreStoryContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "explore_story_detail";

    private FragmentManager fragmentManager;

    private ImageView exploreStoriesTitleImage;
    private TextView exploreStoriesTitleText;
    private TextView exploreStoriesStartDate;
    private TextView exploreStoriesEndDate;
    private ImageView openMapButton;

    private RecyclerView recyclerView;
    private ExploreStoryDetailAdapter exploreStoryDetailAdapter;
    private ArrayList<ItineraryLocation> itineraryLocationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore_stories_detail, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        exploreStoriesTitleImage = root.findViewById(R.id.exploreStoriesTitleImage);
        exploreStoriesTitleText = root.findViewById(R.id.exploreStoriesTitleText);
        exploreStoriesStartDate = root.findViewById(R.id.exploreStoriesStartDate);
        exploreStoriesEndDate = root.findViewById(R.id.exploreStoriesEndDate);
        openMapButton = root.findViewById(R.id.ic_open_map_button);
        recyclerView = root.findViewById(R.id.exploreStoriesPlaceListView);
        exploreStoryDetailAdapter = new ExploreStoryDetailAdapter(getContext(), itineraryLocationList);
        recyclerView.setAdapter(exploreStoryDetailAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        if (getArguments() != null) {
            UserStory selectedStory = getArguments().getParcelable("selected_story");
            Glide.with(getActivity()).load(String.valueOf(selectedStory.getTitle_image_url())).centerCrop().into(exploreStoriesTitleImage);
            exploreStoriesTitleText.setText(selectedStory.getTitle());
            exploreStoriesStartDate.setText(getDateInString(selectedStory.getDate_start()));
            exploreStoriesEndDate.setText(getDateInString(selectedStory.getDate_end()));

            itineraryLocationList = selectedStory.getItineraryLocationList();
            exploreStoryDetailAdapter = new ExploreStoryDetailAdapter(getContext(), itineraryLocationList);
            exploreStoryDetailAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(exploreStoryDetailAdapter);
        }

        openMapButton.setOnClickListener(onClickOpenMapButton);
        return root;
    }

    @Override
    public void onBackPressed() {
        Fragment exploreStoryMain = fragmentManager.findFragmentByTag(ExploreStoryMainFragment.FRAG_TAG);
        Fragment openMap = fragmentManager.findFragmentByTag(OpenMapFragment.FRAG_TAG);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (exploreStoryMain == null)
            transaction.add(R.id.fragment_container, new ExploreStoryMainFragment(), ExploreStoryMainFragment.FRAG_TAG);
        else
            transaction.show(exploreStoryMain);

        if (openMap != null)
            transaction.remove(openMap);

        transaction.remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                .commit();

        MainActivity.current_fragment_tag = ExploreStoryMainFragment.FRAG_TAG;
    }

    private View.OnClickListener onClickOpenMapButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = fragmentManager.findFragmentByTag(OpenMapFragment.FRAG_TAG);

            if (fragment == null) {
                fragment = new OpenMapFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, OpenMapFragment.FRAG_TAG)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }
            else {
                fragmentManager.beginTransaction()
                        .show(fragment)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }

            Bundle args = new Bundle();
            args.putParcelableArrayList("open_itinerary_list", itineraryLocationList);

            fragment.setArguments(args);
            MainActivity.current_fragment_tag = OpenMapFragment.FRAG_TAG;
        }
    };

    private String getDateInString (Date date) {

        String[] temp = date.toString().split(" ", 6); // DayOfWeek Month DayOfMonth Time TimeZone Year
        String month = getMonthInIntValueStringFormat(temp[1]);

        StringBuilder builder = new StringBuilder();
        builder.append(month);
        builder.append(" / ");
        builder.append(temp[2]);
        builder.append(" / ");
        builder.append(temp[5]);

        return builder.toString();
    }

    private String getMonthInIntValueStringFormat(String month) {
        if (month.equals("Jan"))       return "1";
        else if (month.equals("Feb")) return "2";
        else if (month.equals("Mar")) return "3";
        else if (month.equals("Apr")) return "4";
        else if (month.equals("May")) return "5";
        else if (month.equals("Jun")) return "6";
        else if (month.equals("Jul")) return "7";
        else if (month.equals("Aug")) return "8";
        else if (month.equals("Sep")) return "9";
        else if (month.equals("Oct")) return "10";
        else if (month.equals("Nov")) return "11";
        else if (month.equals("Dec")) return "12";
        else                            return "0"; // error month
    }
}
