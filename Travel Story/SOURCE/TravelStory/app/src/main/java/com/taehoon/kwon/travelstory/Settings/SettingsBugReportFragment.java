package com.taehoon.kwon.travelstory.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.R;

public class SettingsBugReportFragment extends SettingsContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "settings_bug_report";

    private FragmentManager fragmentManager;

    private EditText bugReportToEditText;
    private EditText bugReportSubjectEditText;
    private EditText bugReportMessageEditText;
    private Button sendButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings_bug_report, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        bugReportToEditText = root.findViewById(R.id.bugReportToEditText);
        bugReportSubjectEditText = root.findViewById(R.id.bugReportSubjectEditText);
        bugReportMessageEditText = root.findViewById(R.id.bugReportMessageEditText);
        sendButton = root.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String to = bugReportToEditText.getText().toString();
                String subject = bugReportSubjectEditText.getText().toString();
                String message = bugReportMessageEditText.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                email.putExtra(Intent.EXTRA_TEXT, message);

                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client : "));
            }
        });

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
