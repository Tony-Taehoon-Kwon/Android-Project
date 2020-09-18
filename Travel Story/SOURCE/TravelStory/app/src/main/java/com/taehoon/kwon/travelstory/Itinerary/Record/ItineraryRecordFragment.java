package com.taehoon.kwon.travelstory.Itinerary.Record;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.taehoon.kwon.travelstory.Itinerary.Map.ItineraryMapFragment;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookContainerFragment;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookNewRecordFragment;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.MarkerList;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class ItineraryRecordFragment extends StoryBookContainerFragment implements OnBackPressed, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private FragmentManager fragmentManager;

    public static final String FRAG_TAG = "story_book_itinerary_record";
    private static int RESULT_LOAD_IMAGE = 2001;

    private GoogleSignInAccount googleSignInAccount;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private ClipData imageList;
    private int imageCurrentIndex = 0;
    private int completedTaskCount = 0;

    private ImageSwitcher imageContainer;
    private ImageButton previousSwitchButton, nextSwitchButton;
    private EditText destinationTitle;
    private EditText destinationDescription;
    private Button cancelButton;
    private Button confirmButton;
    private ProgressBar uploadProgressBar;
    private TextView arrivalTime;
    private TextView departureTime;
    private TextView arrivalTimeErrorText;
    private TextView departureTimeErrorText;

    private Boolean isStartDatePicking;
    private Boolean isArrivalTimeSelected = false;
    private Boolean isDepartureTimeSelected = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_itinerary_record, container, false);

        Log.d("FRAG_TAG", "Before Frag tag : " + MainActivity.current_fragment_tag);
        MainActivity.current_fragment_tag = FRAG_TAG;
        Log.d("FRAG_TAG", "Current Frag tag : " + MainActivity.current_fragment_tag);

        fragmentManager = getActivity().getSupportFragmentManager();
        Log.d("Backstack Count: ", fragmentManager.getBackStackEntryCount() + " - ItineraryRecord");

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());

        destinationTitle = root.findViewById(R.id.destinationTitle);
        destinationDescription = root.findViewById(R.id.destinationDescription);
        previousSwitchButton = root.findViewById(R.id.previousSwitchButton);
        nextSwitchButton = root.findViewById(R.id.nextSwitchButton);
        imageContainer = root.findViewById(R.id.itineraryImage);
        cancelButton = root.findViewById(R.id.cancelButton);
        confirmButton = root.findViewById(R.id.confirmPlaceButton);
        uploadProgressBar = root.findViewById(R.id.uploadProgressBar);
        arrivalTime = root.findViewById(R.id.arrivalTime);
        departureTime = root.findViewById(R.id.departureTime);
        arrivalTimeErrorText = root.findViewById(R.id.arrivalTimeErrorText);
        departureTimeErrorText = root.findViewById(R.id.departureTimeErrorText);

        arrivalTime.setOnClickListener(arrivalTimeClickListener);
        departureTime.setOnClickListener(departureTimeClickListener);
        imageContainer.setOnClickListener(findImageClickHandler);
        cancelButton.setOnClickListener(cancelButtonClickHandler);
        confirmButton.setOnClickListener(confirmButtonClickHandler);

        imageContainer.setFactory(imageFactory);
        imageContainer.setImageResource(R.drawable.ic_image);
        previousSwitchButton.setOnClickListener(previousSwitchButtonClickListener);
        nextSwitchButton.setOnClickListener(nextSwitchButtonClickListener);

        return root;
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to go back to Map page?\nYou may lose your progress.")
                .setCancelable(true)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Fragment fragment = fragmentManager.findFragmentByTag(ItineraryMapFragment.FRAG_TAG);

                        Bundle args = new Bundle();
                        args.putBoolean("record_cancelled", true);

                        if (fragment == null) {
                            fragment = new ItineraryMapFragment();
                            fragment.setArguments(args);
                            fragmentManager.beginTransaction()
                                    .add(R.id.fragment_container, fragment, ItineraryMapFragment.FRAG_TAG)
                                    .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                    .commit();
                        }
                        else {
                            fragment.setArguments(args);
                            fragmentManager.beginTransaction()
                                    .show(fragment)
                                    .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                    .commit();
                        }
                        MainActivity.current_fragment_tag = ItineraryMapFragment.FRAG_TAG;
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private ViewSwitcher.ViewFactory imageFactory = new ViewSwitcher.ViewFactory() {
        @Override
        public View makeView() {
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
            return imageView;
        }
    };

    private View.OnClickListener previousSwitchButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (imageList == null)             return;
            if (imageList.getItemCount() <= 1)  return;

            imageContainer.setInAnimation(getContext(), R.anim.anim_switch_from_left);
            imageContainer.setOutAnimation(getContext(), R.anim.anim_switch_to_right);
            --imageCurrentIndex;
            if (imageCurrentIndex < 0)
                imageCurrentIndex = imageList.getItemCount() - 1;
            imageContainer.setImageURI(imageList.getItemAt(imageCurrentIndex).getUri());
        }
    };

    private View.OnClickListener nextSwitchButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (imageList == null)             return;
            if (imageList.getItemCount() <= 1)  return;

            imageContainer.setInAnimation(getContext(), R.anim.anim_switch_from_right);
            imageContainer.setOutAnimation(getContext(), R.anim.anim_switch_to_left);
            ++imageCurrentIndex;
            if (imageCurrentIndex >= imageList.getItemCount())
                imageCurrentIndex = 0;
            imageContainer.setImageURI(imageList.getItemAt(imageCurrentIndex).getUri());
        }
    };

    private View.OnClickListener findImageClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                imageContainer.setImageURI(clipData.getItemAt(0).getUri());
                imageList = clipData;
            }
        }
    }

    private View.OnClickListener confirmButtonClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isContentMissing())
                return;

            fileUpload();
        }
    };

    private void fileUpload() {
        completedTaskCount = 0;
        List<String> urlStringList = new ArrayList<>();

        for (int i = 0; i < imageList.getItemCount(); ++i)
        {
            String path;

            if (googleSignInAccount != null)
                path = "storybook/" + googleSignInAccount.getEmail() + "/"
                    + MarkerList.getInstance().storyTitle + "/place_" + MarkerList.getInstance().markers.size()
                        + "/picture_" + (i+1) + "_" + UUID.randomUUID().toString() +".png";
            else if (firebaseAuth != null)
                path = "storybook/" + firebaseAuth.getCurrentUser().getEmail() + "/"
                        + MarkerList.getInstance().storyTitle + "/place_" + MarkerList.getInstance().markers.size()
                        + "/picture_" + (i+1) + "_" + UUID.randomUUID().toString() +".png";
            else
                return;

            final StorageReference storybookRef = firebaseStorage.getReference(path);

            UploadTask uploadTask = storybookRef.putFile(imageList.getItemAt(i).getUri());
            uploadProgressBar.setVisibility(View.VISIBLE);
            confirmButton.setEnabled(false);

            /* need to download uri because before downloading uri, there is no way to get the url connected to firebase storage
            *  Trial and failure : imageList.getItemAt(completedTaskCount++).getUri().toString()
            *                       this returns url for the temporary image, which is selected with gallery access
            *                       It might work before application terminate, but if application restarted, then link was expired already
            * Proper url looks like https://firebasestorage.googleapis.com/...
            * temporary(bad) url looks like content://com.google.android.apps.photos.contentprovider/-1/1/content%3A%2F%2Fmedia%2Fexternal%2Fimages%2Fmedia%2F324/ORIGINAL/NONE/image%2Fpng/423248953 */
            Task<Uri> getDownloadUriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storybookRef.getDownloadUrl();
                }
            });

            getDownloadUriTask.addOnCompleteListener(getActivity(), new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();
                        urlStringList.add(downloadUri.toString());

                        ++completedTaskCount;

                        if (completedTaskCount == imageList.getItemCount()) {
                            Fragment fragment = fragmentManager.findFragmentByTag(StoryBookNewRecordFragment.FRAG_TAG);

                            MarkerList.RegisteredMarker lastMarker = MarkerList.getInstance().markers.get(MarkerList.getInstance().markers.size() - 1);
                            lastMarker.registered = true;

                            String arrivalTimeString = arrivalTime.getText().toString();
                            String departureTimeString = departureTime.getText().toString();
                            int[] yearMonthDateHourMinuteArrival = getYearMonthDateHourMinInInteger(arrivalTimeString);
                            int[] yearMonthDateHourMinuteDeparture = getYearMonthDateHourMinInInteger(departureTimeString);

                            Date arrivalTimeDate = new Date(yearMonthDateHourMinuteArrival[0] - 1900,
                                    yearMonthDateHourMinuteArrival[1] - 1,
                                    yearMonthDateHourMinuteArrival[2],
                                    yearMonthDateHourMinuteArrival[3],
                                    yearMonthDateHourMinuteArrival[4]);

                            Date departureTimeDate = new Date(yearMonthDateHourMinuteDeparture[0] - 1900,
                                    yearMonthDateHourMinuteDeparture[1] - 1,
                                    yearMonthDateHourMinuteDeparture[2],
                                    yearMonthDateHourMinuteDeparture[3],
                                    yearMonthDateHourMinuteDeparture[4]);

                            ItineraryLocation lastSpot = new ItineraryLocation();
                            lastSpot.setName(destinationTitle.getText().toString());
                            lastSpot.setDescription(destinationDescription.getText().toString());
                            lastSpot.setImages_url_list(urlStringList);
                            lastSpot.setGeo_point(new GeoPoint(lastMarker.marker.getPosition().latitude, lastMarker.marker.getPosition().longitude));
                            lastSpot.setTime_arrival(arrivalTimeDate);
                            lastSpot.setTime_departure(departureTimeDate);
                            lastSpot.setDirection_points_from_previous_location(lastMarker.direction_points_from_previous_location);
                            lastSpot.setIndex_of_selected_path_from_previous_location(lastMarker.index_of_selected_path_from_previous_location);

                            Bundle args = new Bundle();
                            args.putParcelable("itineraryLocation", lastSpot);

                            if (getArguments() != null) {
                                ArrayList<ItineraryLocation> itineraryLocations = getArguments().getParcelableArrayList("changed_itineraryLocationList");
                                if (itineraryLocations != null)
                                    args.putParcelableArrayList("changed_itineraryLocationList", itineraryLocations);
                            }

                            if (fragment == null) {
                                fragment = new StoryBookNewRecordFragment();
                                fragment.setArguments(args);
                                fragmentManager.beginTransaction()
                                        .add(R.id.fragment_container, fragment, StoryBookNewRecordFragment.FRAG_TAG)
                                        .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                        .commit();
                            } else {
                                fragment.setArguments(args);
                                fragmentManager.beginTransaction()
                                        .show(fragment)
                                        .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                        .commit();
                            }
                            MainActivity.current_fragment_tag = StoryBookNewRecordFragment.FRAG_TAG;
                        }
                    }
                    uploadProgressBar.setVisibility(View.GONE);
                    confirmButton.setEnabled(true);
                }
            });
        }
    }

    private View.OnClickListener cancelButtonClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Are you sure you want to go back to Map page?\nYou may lose your progress.")
                    .setCancelable(true)
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            Fragment fragment = fragmentManager.findFragmentByTag(ItineraryMapFragment.FRAG_TAG);

                            Bundle args = new Bundle();
                            args.putBoolean("record_cancelled", true);

                            if (fragment == null) {
                                fragment = new ItineraryMapFragment();
                                fragment.setArguments(args);
                                fragmentManager.beginTransaction()
                                        .add(R.id.fragment_container, fragment, ItineraryMapFragment.FRAG_TAG)
                                        .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                        .commit();
                            }
                            else {
                                fragment.setArguments(args);
                                fragmentManager.beginTransaction()
                                        .show(fragment)
                                        .remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                        .commit();
                            }
                            MainActivity.current_fragment_tag = ItineraryMapFragment.FRAG_TAG;
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    };

    private Boolean isContentMissing() {

        if (imageList == null) {
            ToastMessage("Image Required");
            return true;
        }

        String destinationTitleText = destinationTitle.getText().toString().trim();
        String descriptionText = destinationDescription.getText().toString().trim();

        if (destinationTitleText.isEmpty()) {
            destinationTitle.setError("Destination title Required");
            destinationTitle.requestFocus();
            return true;
        }

        if (descriptionText.isEmpty()) {
            destinationDescription.setError("Description Required");
            destinationDescription.requestFocus();
            return true;
        }

        if (!isArrivalTimeSelected) {
            arrivalTimeErrorText.setError("Arrival Time Required");
            arrivalTimeErrorText.requestFocus();
            return true;
        }

        if (!isDepartureTimeSelected) {
            departureTimeErrorText.setError("Departure Time Required");
            departureTimeErrorText.requestFocus();
            return true;
        }
        return false;
    }

    private View.OnClickListener arrivalTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isStartDatePicking = true;
            isArrivalTimeSelected = false;
            showDatePickerDialog();
        }
    };

    private View.OnClickListener departureTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isStartDatePicking = false;
            isDepartureTimeSelected = false;
            showDatePickerDialog();
        }
    };

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(), this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (isStartDatePicking)
        {
            arrivalTime.setText((month+1) + " / " + dayOfMonth + " / " + year);
        }
        else
        {
            departureTime.setText((month+1) + " / " + dayOfMonth + " / " + year);
        }
        showTimePickerDialog();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (isStartDatePicking) {
            arrivalTime.setText(arrivalTime.getText() + "\n" + getTimeInAmPmFormat(hourOfDay, minute));
            arrivalTimeErrorText.setError(null);
            isArrivalTimeSelected = true;
        }
        else {
            departureTime.setText(departureTime.getText() + "\n" + getTimeInAmPmFormat(hourOfDay, minute));
            departureTimeErrorText.setError(null);
            isDepartureTimeSelected = true;
        }
    }

    private String getTimeInAmPmFormat(int hourOfDay, int minute) {

        StringBuilder builder = new StringBuilder();
        String amPm = "";

        if (hourOfDay == 0) {
            hourOfDay = 12;
            amPm = " am";
        }
        else if (hourOfDay == 12) {
            amPm = " pm";
        }
        else if (hourOfDay >= 13) {
            hourOfDay -= 12;
            amPm = " pm";
        }
        else {
            amPm = " am";
        }
        builder.append(hourOfDay);
        builder.append(" : ");
        builder.append(minute);
        builder.append(amPm);
        return builder.toString();
    }

    private int[] getYearMonthDateHourMinInInteger(String date) {
        String[] monthDayYear = date.split(" / ", 3);
        String[] YearTime = monthDayYear[2].split("\n", 2);

        int year = Integer.parseInt(YearTime[0]);
        int month = Integer.parseInt(monthDayYear[0]);
        int day = Integer.parseInt(monthDayYear[1]);
        int[] hourMin = getTimeOfHourAndMinuteInInteger(YearTime[1]);
        int[] result = { year, month, day, hourMin[0], hourMin[1] };

        return result;
    }

    private int[] getTimeOfHourAndMinuteInInteger(String time) {
        String[] temp = time.split(" : ", 2);
        String[] temp2 = temp[1].split(" ", 2);
        int hour;
        int minute = Integer.parseInt(temp2[0]);

        if (temp2[1].equals("am") && temp[0].equals("12")) // if 12:00 am
            hour = 0;
        else if (temp2[1].equals("am") || temp[0].equals("12")) // if 1:00 am ~ 11:00 am or 12:00 pm
            hour = Integer.parseInt(temp[0]);
        else                                               // if 1:00 pm ~ 11:00 pm
            hour = Integer.parseInt(temp[0]) + 12;

        int[] result = { hour, minute };
        return result;
    }

    private void ToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
