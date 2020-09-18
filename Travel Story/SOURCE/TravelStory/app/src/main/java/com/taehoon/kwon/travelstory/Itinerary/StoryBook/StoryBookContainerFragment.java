package com.taehoon.kwon.travelstory.Itinerary.StoryBook;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.R;

public class StoryBookContainerFragment extends Fragment implements OnBackPressed {

    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_container, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new StoryBookMainFragment(), StoryBookMainFragment.FRAG_TAG)
                .commit();

        MainActivity.current_fragment_tag = StoryBookMainFragment.FRAG_TAG;

        return root;
    }

    @Override
    public void onBackPressed() {
        Log.d("FRAG TAG", "Frag tag / StoryBookContainer - onBackPressed called");
    }
}
