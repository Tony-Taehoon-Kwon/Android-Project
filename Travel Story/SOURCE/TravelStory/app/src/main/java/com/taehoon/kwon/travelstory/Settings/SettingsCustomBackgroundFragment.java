package com.taehoon.kwon.travelstory.Settings;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class SettingsCustomBackgroundFragment extends SettingsContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "settings_custom_background";
    private static int RESULT_LOAD_IMAGE = 9001;

    private FragmentManager fragmentManager;

    private ImageView customBackgroundPreview;
    private Button selectImageButton;
    private Button saveImageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings_custom_background, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        customBackgroundPreview = root.findViewById(R.id.customBackgroundPreview);
        selectImageButton = root.findViewById(R.id.selectImageButton);
        saveImageButton = root.findViewById(R.id.saveImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)customBackgroundPreview.getDrawable();
                saveToInternalStorage(bitmapDrawable.getBitmap());

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
        });
        loadImageFromStorage("/data/user/0/com.taehoon.kwon.travelstory/app_imageDir");
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            Glide.with(getContext()).load(selectedImage).centerCrop().into(customBackgroundPreview);
        }
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

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getActivity());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,"custom_background.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path)
    {
        try {
            File file = new File(path, "custom_background.jpg");
            if (file != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                Glide.with(getContext()).load(bitmap).into(customBackgroundPreview);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
