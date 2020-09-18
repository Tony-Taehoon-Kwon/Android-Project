package com.taehoon.kwon.travelstory.Settings;

import android.os.Bundle;
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

public class SettingsTermsOfUseFragment extends SettingsContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "settings_terms_of_use";

    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings_terms_of_use, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        return root;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragment.FRAG_TAG);

        if (fragment == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, new SettingsFragment(), SettingsFragment.FRAG_TAG)
                    .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                    .commit();
        }
        else {
            fragmentManager.beginTransaction()
                    .show(fragment)
                    .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                    .commit();
        }
        MainActivity.current_fragment_tag = SettingsFragment.FRAG_TAG;
    }
}
