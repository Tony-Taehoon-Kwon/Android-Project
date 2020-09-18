package com.taehoon.kwon.travelstory.Itinerary.StoryBook;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.taehoon.kwon.travelstory.EventTrigger;
import com.taehoon.kwon.travelstory.Itinerary.Map.ItineraryMapFragment;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.MarkerList;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class StoryBookNewRecordFragment extends StoryBookContainerFragment implements OnBackPressed, DatePickerDialog.OnDateSetListener {

    public static final String FRAG_TAG = "story_book_new_record";
    private static int RESULT_LOAD_IMAGE = 2001;

    private GoogleSignInAccount googleSignInAccount;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private FragmentManager fragmentManager;

    private EditText storyTitle;
    private TextView titleImageErrorText;
    private ImageView titleImage;
    private TextView travelStartDate;
    private TextView travelEndDate;
    private TextView travelStartDateErrorText;
    private TextView travelEndDateErrorText;
    private Button addPlaceButton;
    private Button newStoryUploadButton;
    private ProgressBar newStoryUploadProgressBar;
    private RecyclerView placeListView;

    private AddPlaceAdapter placeListAdapter;
    private ArrayList<ItineraryLocation> itineraryLocations = new ArrayList<>();

    private Boolean isImageSelected = false;
    private Boolean isStartDateSelected = false;
    private Boolean isEndDateSelected = false;
    private Boolean isStartDatePicking;
    private Boolean isEditing = false;
    private Boolean isTitleDuplicated = false;

    private Uri selectedImage;
    private String presavedImageURL;
    private EventTrigger goToMapFrag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_story_book_new_record, container, false);

        Log.d("FRAG_TAG", "Before Frag tag : " + MainActivity.current_fragment_tag);
        MainActivity.current_fragment_tag = FRAG_TAG;
        Log.d("FRAG_TAG", "Current Frag tag : " + MainActivity.current_fragment_tag);

        fragmentManager = getActivity().getSupportFragmentManager();
        Log.d("Backstack Count: ", fragmentManager.getBackStackEntryCount() + " - StoryBookNewRecord");

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());

        goToMapFrag = new GoToMapEvent();

        storyTitle = root.findViewById(R.id.storyTitle);
        storyTitle.setOnEditorActionListener(storyTitleActionListener);
        titleImageErrorText = root.findViewById(R.id.titleImageErrorText);
        titleImage = root.findViewById(R.id.titleImage);
        titleImage.setOnClickListener(findImageClickHandler);
        travelStartDate = root.findViewById(R.id.travelStartDate);
        travelEndDate = root.findViewById(R.id.travelEndDate);
        travelStartDateErrorText = root.findViewById(R.id.travelStartDateErrorText);
        travelEndDateErrorText = root.findViewById(R.id.travelEndDateErrorText);
        placeListView = root.findViewById(R.id.placeListView);
        placeListAdapter = new AddPlaceAdapter(itineraryLocations, goToMapFrag);
        placeListView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        addPlaceButton = root.findViewById(R.id.addPlaceButton);
        addPlaceButton.setOnClickListener(onClickListener);
        newStoryUploadButton = root.findViewById(R.id.newStoryUploadButton);
        newStoryUploadButton.setOnClickListener(uploadClickHandler);
        newStoryUploadProgressBar = root.findViewById(R.id.newStoryUploadProgressBar);

        travelStartDate.setOnClickListener(startDateClickListener);
        travelEndDate.setOnClickListener(endDateClickListener);

        if (getArguments() != null) {
            UserStory selectedStory = getArguments().getParcelable("edit_user_story");
            storyTitle.setText(selectedStory.getTitle());
            Glide.with(getContext()).load(selectedStory.getTitle_image_url()).centerCrop().into(titleImage);
            presavedImageURL = selectedStory.getTitle_image_url();

            travelStartDate.setText(getDateInString(selectedStory.getDate_start()));
            travelEndDate.setText(getDateInString(selectedStory.getDate_end()));

            itineraryLocations = selectedStory.getItineraryLocationList();
            placeListAdapter.placeList = itineraryLocations;
            placeListAdapter.notifyDataSetChanged();
            placeListView.setAdapter(placeListAdapter);
            isEditing = true;
            isImageSelected = true;
            isStartDateSelected = true;
            isEndDateSelected = true;
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d("StoryBookNewRecord", "StoryBookNewRecord onStart called");
        Log.d("StoryBookNewRecord", "StoryBookNewRecord id : " + getId());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        Log.d("StoryBookNewRecord", "StoryBookNewRecord onHiddenChanged called : " + hidden);

        if (!hidden) {
            if (getArguments() != null) {
                Log.d("StoryBookNewRecord", "StoryBookNewRecord : getArguments!");
                ItineraryLocation lastSpot = getArguments().getParcelable("itineraryLocation");

                ArrayList<ItineraryLocation> itineraryLocationsTemp = getArguments().getParcelableArrayList("changed_itineraryLocationList");
                if (itineraryLocationsTemp != null) {
                    itineraryLocations = itineraryLocationsTemp;
                    placeListAdapter.placeList = itineraryLocations;
                    placeListAdapter.notifyDataSetChanged();
                    placeListView.setAdapter(placeListAdapter);
                }

                if (lastSpot != null) {
                    Log.d("StoryBookNewRecord", "StoryBookNewRecord lastSpot : " + lastSpot.getName());
                    if (itineraryLocations.size() == MarkerList.getInstance().markers.size() -1) // expected senario
                        itineraryLocations.add(lastSpot);
                    else { // when editing, bug happens that itineraryLocation size increased by one. Make size equal
                        itineraryLocations.get(itineraryLocations.size() -1).setName(lastSpot.getName());
                        itineraryLocations.get(itineraryLocations.size() -1).setDescription(lastSpot.getDescription());
                        itineraryLocations.get(itineraryLocations.size() -1).setGeo_point(lastSpot.getGeo_point());
                        itineraryLocations.get(itineraryLocations.size() -1).setDirection_points_from_previous_location(lastSpot.getDirection_points_from_previous_location());
                        itineraryLocations.get(itineraryLocations.size() -1).setImages_url_list(lastSpot.getImages_url_list());
                        itineraryLocations.get(itineraryLocations.size() -1).setIndex_of_selected_path_from_previous_location(lastSpot.getIndex_of_selected_path_from_previous_location());
                        itineraryLocations.get(itineraryLocations.size() -1).setTime_arrival(lastSpot.getTime_arrival());
                        itineraryLocations.get(itineraryLocations.size() -1).setTime_departure(lastSpot.getTime_departure());
                    }
                    placeListAdapter.notifyDataSetChanged();
                    placeListView.setAdapter(placeListAdapter);
                    addPlaceButton.setError(null);

                    if (itineraryLocations.size() != MarkerList.getInstance().markers.size())
                        Log.e("StoryBookNewRecord", "Edit mode itineraryLocation size bug happened");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to go back to My Story Book main page?\nYou may lose your progress.")
                .setCancelable(true)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Fragment storyBookMain = fragmentManager.findFragmentByTag(StoryBookMainFragment.FRAG_TAG);
                        Fragment itineraryMap = fragmentManager.findFragmentByTag(ItineraryMapFragment.FRAG_TAG);
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        if (storyBookMain == null)
                            transaction.add(R.id.fragment_container, new StoryBookMainFragment(), StoryBookMainFragment.FRAG_TAG);
                        else
                            transaction.show(storyBookMain);

                        if (itineraryMap != null)
                            transaction.remove(itineraryMap);

                        transaction.remove(fragmentManager.findFragmentByTag(FRAG_TAG))
                                .commit();

                        MainActivity.current_fragment_tag = StoryBookMainFragment.FRAG_TAG;
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void addData(Uri downloadUri) {

        if (downloadUri == null && (presavedImageURL == null || presavedImageURL.isEmpty())) {
            ToastMessage("Error occured while uploading image.\nPlease select title image again.");
            return;
        }

        DocumentReference accountRef;
        DocumentReference storybookRef;

        if (googleSignInAccount != null) {
            accountRef = firebaseFirestore
                    .collection("storybook")
                    .document(googleSignInAccount.getEmail());

            storybookRef = accountRef
                    .collection("mystory")
                    .document(storyTitle.getText().toString());
        }
        else if (firebaseAuth != null) {
            accountRef = firebaseFirestore
                    .collection("storybook")
                    .document(firebaseAuth.getCurrentUser().getEmail());

            storybookRef = accountRef
                    .collection("mystory")
                    .document(storyTitle.getText().toString());
        }
        else
            return;

        String startDateString = travelStartDate.getText().toString();
        String endDateString = travelEndDate.getText().toString();
        String[] monthDayYearStart = startDateString.split(" / ", 3);
        String[] monthDayYearEnd = endDateString.split(" / ", 3);

        Date startDate = new Date(Integer.parseInt(monthDayYearStart[2]) -1900,
                                   Integer.parseInt(monthDayYearStart[0]) -1,
                                   Integer.parseInt(monthDayYearStart[1]));

        Date endDate = new Date(Integer.parseInt(monthDayYearEnd[2]) -1900,
                                Integer.parseInt(monthDayYearEnd[0]) -1,
                                Integer.parseInt(monthDayYearEnd[1]));

        UserStory userStory = new UserStory();
        userStory.setTitle(storyTitle.getText().toString());
        if (downloadUri != null) userStory.setTitle_image_url(downloadUri.toString());
        else                     userStory.setTitle_image_url(presavedImageURL);
        userStory.setDate_start(startDate);
        userStory.setDate_end(endDate);
        userStory.setItineraryLocationList(itineraryLocations);

        Map<String, Object> created = new HashMap<>();
        created.put("Top Document Created First", true);
        accountRef.set(created); // this should be done, or you will find that subdocuments are alive, topdocument would be dead(italics font in firebase console)
        storybookRef.set(userStory)
                .addOnSuccessListener(addStoryDataSuccessListener);
    }

    private OnSuccessListener<Void> addStoryDataSuccessListener = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Log.d("tag", "save user id into database");

            Bundle args = new Bundle();
            args.putBoolean("story_added", true);

            Fragment fragment = fragmentManager.findFragmentByTag(StoryBookMainFragment.FRAG_TAG);
            Fragment itinerayMapFragment = fragmentManager.findFragmentByTag(ItineraryMapFragment.FRAG_TAG);
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (fragment == null) {
                fragment = new StoryBookMainFragment();
                fragment.setArguments(args);
                transaction.add(R.id.fragment_container, fragment, StoryBookMainFragment.FRAG_TAG);
            }
            else {
                fragment.setArguments(args);
                transaction.show(fragment);
            }
            transaction.remove(fragmentManager.findFragmentByTag(FRAG_TAG));

            if (itinerayMapFragment != null)
                transaction.remove(itinerayMapFragment);

            transaction.commit();

            MainActivity.current_fragment_tag = StoryBookMainFragment.FRAG_TAG;
        }
    };

    private class GoToMapEvent implements EventTrigger {

        @Override
        public void trigger() {
            String titleText = storyTitle.getText().toString().trim();

            if (titleText.isEmpty())
                storyTitle.setText("Temporary Title");

            Bundle args = new Bundle();
            args.putString("story_title", storyTitle.getText().toString());
            args.putBoolean("edit_mode", isEditing);

            if (isEditing)
                args.putParcelableArrayList("edit_itinerary_list", itineraryLocations);
            if (placeListAdapter.isPlaceRemoved) {
                args.putBoolean("place_removed", true);
                args.putParcelableArrayList("removed_itinerary_list", itineraryLocations);
                args.putInt("update_needed_marker_for_broken_path", placeListAdapter.removedPlaceIndex);
                placeListAdapter.isPlaceRemoved = false;
                placeListAdapter.removedPlaceIndex = -1;
            }

            Fragment fragment = fragmentManager.findFragmentByTag(ItineraryMapFragment.FRAG_TAG);

            if (fragment == null) {
                fragment = new ItineraryMapFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, ItineraryMapFragment.FRAG_TAG)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }
            else {
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .show(fragment)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }
            MainActivity.current_fragment_tag = ItineraryMapFragment.FRAG_TAG;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String titleText = storyTitle.getText().toString().trim();

            if (titleText.isEmpty()) {
                storyTitle.setError("Title Required");
                storyTitle.requestFocus();
                return;
            }

            Bundle args = new Bundle();
            args.putString("story_title", storyTitle.getText().toString());
            args.putBoolean("edit_mode", isEditing);

            Log.d("Remove Test", "Remove Test : StoryBookNewRecord Add place button clicked - isEditing = " + isEditing);
            Log.d("Remove Test", "Remove Test : StoryBookNewRecord Add place button clicked - isPlaceRemoved = " + placeListAdapter.isPlaceRemoved);

            if (isEditing)
                args.putParcelableArrayList("edit_itinerary_list", itineraryLocations);
            if (placeListAdapter.isPlaceRemoved) {
                args.putBoolean("place_removed", true);
                args.putParcelableArrayList("removed_itinerary_list", itineraryLocations);
                args.putInt("update_needed_marker_for_broken_path", placeListAdapter.removedPlaceIndex);
                placeListAdapter.isPlaceRemoved = false;
                placeListAdapter.removedPlaceIndex = -1;
            }

            Fragment fragment = fragmentManager.findFragmentByTag(ItineraryMapFragment.FRAG_TAG);

            if (fragment == null) {
                fragment = new ItineraryMapFragment();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, fragment, ItineraryMapFragment.FRAG_TAG)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }
            else {
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .show(fragment)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .commit();
            }
            MainActivity.current_fragment_tag = ItineraryMapFragment.FRAG_TAG;
        }
    };

    private View.OnClickListener findImageClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();
            titleImage.setImageURI(selectedImage);
            isImageSelected = true;
            titleImageErrorText.setError(null);
        }
    }

    private TextView.OnEditorActionListener storyTitleActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideSoftKeyboard();
                return true;
            }
            return false;
        }
    };

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(storyTitle.getWindowToken(), 0);
    }

    private View.OnClickListener uploadClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isContentMissing())
                return;

            checkSameTitleExistedAndUpload();
        }
    };

    private void uploadStory() {
        String path;
        if (googleSignInAccount != null)
            path = "storybook/" + googleSignInAccount.getEmail() + "/" + storyTitle.getText().toString() + "/title_image.png";
        else if (firebaseAuth != null)
            path = "storybook/" + firebaseAuth.getCurrentUser().getEmail() + "/" + storyTitle.getText().toString() + "/title_image.png";
        else
            return;

        final StorageReference storybookRef = firebaseStorage.getReference(path);

        if (selectedImage == null) {
            addData(null);
            return;
        }

        UploadTask uploadTask = storybookRef.putFile(selectedImage);
        newStoryUploadProgressBar.setVisibility(View.VISIBLE);
        newStoryUploadButton.setEnabled(false);

        uploadTask.addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.i("upload image", "Upload Task Complete!");
            }
        });

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
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    addData(downloadUri);
                }
                newStoryUploadProgressBar.setVisibility(View.GONE);
                newStoryUploadButton.setEnabled(true);
            }
        });
    }

    private Boolean isContentMissing() {

        String titleText = storyTitle.getText().toString().trim();

        if (titleText.isEmpty()) {
            storyTitle.setError("Title Required");
            storyTitle.requestFocus();
            return true;
        }

        if (!isImageSelected) {
            titleImageErrorText.setError("Title Image Required");
            titleImageErrorText.requestFocus();
            return true;
        }

        if (!isStartDateSelected) {
            travelStartDateErrorText.setError("Start Date Required");
            travelStartDateErrorText.requestFocus();
            return true;
        }

        if (!isEndDateSelected) {
            travelEndDateErrorText.setError("End Date Required");
            travelEndDateErrorText.requestFocus();
            return true;
        }

        if (itineraryLocations.size() == 0) {
            addPlaceButton.setError("At least one place Required");
            addPlaceButton.requestFocus();
            return true;
        }
        return false;
    }

    private void checkSameTitleExistedAndUpload() {

        isTitleDuplicated = false;

        CollectionReference searchCollectionRef;
        if (googleSignInAccount != null)
            searchCollectionRef = firebaseFirestore.collection("storybook").document(googleSignInAccount.getEmail()).collection("mystory");
        else if (firebaseAuth != null)
            searchCollectionRef = firebaseFirestore.collection("storybook").document(firebaseAuth.getCurrentUser().getEmail()).collection("mystory");
        else
            return;

        searchCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!isEditing) {
                        QuerySnapshot collection = task.getResult();
                        List<DocumentSnapshot> documents = collection.getDocuments();

                        String wantedTitle = storyTitle.getText().toString().trim().toLowerCase();

                        for (DocumentSnapshot document : documents) {

                            String databaseTitle = ((String) document.get("title")).trim().toLowerCase();

                            if (wantedTitle.equals(databaseTitle)) {
                                isTitleDuplicated = true;
                                storyTitle.setError("Same title name already exists!");
                                storyTitle.requestFocus();
                                break;
                            }
                        }
                    }
                    if (!isTitleDuplicated)
                        uploadStory();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("findData", e.getMessage());
            }
        });
    }

    private View.OnClickListener startDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isStartDatePicking = true;
            showDatePickerDialog();
        }
    };

    private View.OnClickListener endDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isStartDatePicking = false;
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
            travelStartDate.setText((month+1) + " / " + dayOfMonth + " / " + year);
            isStartDateSelected = true;
            travelStartDateErrorText.setError(null);
        }
        else
        {
            travelEndDate.setText((month+1) + " / " + dayOfMonth + " / " + year);
            isEndDateSelected = true;
            travelEndDateErrorText.setError(null);
        }
    }

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

    private void ToastMessage(String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
