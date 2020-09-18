package com.taehoon.kwon.travelstory.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.felipecsl.quickreturn.library.widget.QuickReturnAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.taehoon.kwon.travelstory.Login.SigninActivity;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;

public class SettingsFragment extends SettingsContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "settings_main";

    private FragmentManager fragmentManager;

    private ArrayList<SettingsContent> contents = new ArrayList<>();

    private ListView settingsListView;
    private SettingsListAdapter settingsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);

        initSettingsList();

        settingsListView = root.findViewById(R.id.settingsListView);
        settingsAdapter = new SettingsListAdapter(getContext(), contents);
        settingsListView.setAdapter(new QuickReturnAdapter(settingsAdapter));
        settingsListView.setOnItemClickListener(onClickListListener);

        return root;
    }

    private AdapterView.OnItemClickListener onClickListListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            switch (position) {
                case 1:
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, new SettingsCustomBackgroundFragment(), SettingsCustomBackgroundFragment.FRAG_TAG)
                            .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                            .commit();
                    MainActivity.current_fragment_tag = SettingsCustomBackgroundFragment.FRAG_TAG;
                    break;
                case 2:
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, new SettingsBugReportFragment(), SettingsBugReportFragment.FRAG_TAG)
                            .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                            .commit();
                    MainActivity.current_fragment_tag = SettingsBugReportFragment.FRAG_TAG;
                    break;
                case 3:
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, new SettingsTermsOfUseFragment(), SettingsTermsOfUseFragment.FRAG_TAG)
                            .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                            .commit();
                    MainActivity.current_fragment_tag = SettingsTermsOfUseFragment.FRAG_TAG;
                    break;
                case 4:
                    fragmentManager.beginTransaction()
                            .add(R.id.fragment_container, new SettingsCreditsFragment(), SettingsCreditsFragment.FRAG_TAG)
                            .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                            .commit();
                    MainActivity.current_fragment_tag = SettingsCreditsFragment.FRAG_TAG;
                    break;
                case 5:
                    signOut();
                    break;
            }
        }
    };

    private void signOut() {
        if (GoogleSignIn.getLastSignedInAccount(getContext()) != null) {
            SigninActivity.googleSignInClient.signOut()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent startIntent = new Intent(getContext(), SigninActivity.class);
                            startActivity(startIntent);
                        }
                    });
        }
        else if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
            Intent startIntent = new Intent(getContext(), SigninActivity.class);
            startActivity(startIntent);
        }
    }

    private void initSettingsList() {
        contents.add(new SettingsContent(R.drawable.ic_custom_background, "Change Background"));
        contents.add(new SettingsContent(R.drawable.ic_bug_report, "Bug Report"));
        contents.add(new SettingsContent(R.drawable.ic_terms_of_use, "Terms of Use"));
        contents.add(new SettingsContent(R.drawable.ic_credits, "Credits"));
        contents.add(new SettingsContent(R.drawable.ic_logout, "Logout"));
    }
}
