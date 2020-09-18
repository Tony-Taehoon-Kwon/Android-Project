package com.taehoon.kwon.travelstory.Dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.Login.SigninActivity;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DashboardFragment extends Fragment {

    private ImageView accountImage;
    private TextView userName;
    private TextView userEmail;

    private FragmentManager fragmentManager;

    private GoogleSignInAccount googleSignInAccount;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private ArrayList<UserStory> myStories = new ArrayList<>();
    private ArrayList<UserStory> otherUsersStories = new ArrayList<>();
    private ArrayList<UserStory> allUsersStoriesExceptMe = new ArrayList<>();
    private RecyclerView myStoryRecyclerView;
    private StoryPreviewAdapter myStoryPreviewAdapter;
    private RecyclerView otherUsersStoryRecyclerView;
    private StoryGridPreviewAdapter otherUsersStoryPreviewAdapter;

    private ArrayList<Integer> random_indices = new ArrayList<>();
    private int completedTaskCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        View headerLayout = MainActivity.navigationView.getHeaderView(0);

        accountImage = headerLayout.findViewById(R.id.personPhoto);
        userName = headerLayout.findViewById(R.id.personName);
        userEmail = headerLayout.findViewById(R.id.personEmail);

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());

        if (googleSignInAccount != null) {
            String personName = googleSignInAccount.getDisplayName();
            String personEmail = googleSignInAccount.getEmail();
            Uri personPhoto = googleSignInAccount.getPhotoUrl();

            userName.setText(personName);
            userEmail.setText(personEmail);
            Glide.with(this).load(String.valueOf(personPhoto)).override(100, 100).into(accountImage);
        }
        if (firebaseAuth != null) {
            String personName = firebaseAuth.getCurrentUser().getDisplayName();
            String personEmail = firebaseAuth.getCurrentUser().getEmail();
            Uri personPhoto = firebaseAuth.getCurrentUser().getPhotoUrl();

            userName.setText(personName);
            userEmail.setText(personEmail);

            if (personPhoto != null)
                Glide.with(this).load(String.valueOf(personPhoto)).override(100, 100).into(accountImage);
            else
                Glide.with(this).load(R.drawable.ic_person_photo_placeholder).override(100, 100).into(accountImage);
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);

        myStoryRecyclerView = root.findViewById(R.id.storyPreviewRecyclerView);
        otherUsersStoryRecyclerView = root.findViewById(R.id.otherUsersStoryPreviewRecyclerView);

        myStoryPreviewAdapter = new StoryPreviewAdapter(getContext(), myStories);
        otherUsersStoryPreviewAdapter = new StoryGridPreviewAdapter(getContext(), otherUsersStories);

        myStoryRecyclerView.setAdapter(myStoryPreviewAdapter);
        otherUsersStoryRecyclerView.setAdapter(otherUsersStoryPreviewAdapter);

        myStoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        otherUsersStoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        fragmentManager = getActivity().getSupportFragmentManager();
        Log.d("Backstack Count: ", fragmentManager.getBackStackEntryCount() + " - Dashboard");

        getMyData();
        getOtherUsersData();

        return root;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (googleSignInAccount != null) {
            String personName = googleSignInAccount.getDisplayName();
            String personEmail = googleSignInAccount.getEmail();
            Uri personPhoto = googleSignInAccount.getPhotoUrl();

            userName.setText(personName);
            userEmail.setText(personEmail);
            Glide.with(this).load(String.valueOf(personPhoto)).override(100, 100).into(accountImage);
        }
        if (firebaseAuth != null) {
            String personName = firebaseAuth.getCurrentUser().getDisplayName();
            String personEmail = firebaseAuth.getCurrentUser().getEmail();
            Uri personPhoto = firebaseAuth.getCurrentUser().getPhotoUrl();

            userName.setText(personName);
            userEmail.setText(personEmail);

            if (personPhoto != null)
                Glide.with(this).load(String.valueOf(personPhoto)).override(100, 100).into(accountImage);
            else
                Glide.with(this).load(R.drawable.ic_person_photo_placeholder).override(100, 100).into(accountImage);
        }

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
    }

    private void getMyData() {

        CollectionReference storyCollections;

        if (googleSignInAccount != null)
            storyCollections = firebaseFirestore.collection("storybook").document(googleSignInAccount.getEmail()).collection("mystory");
        else if (firebaseAuth != null)
            storyCollections = firebaseFirestore.collection("storybook").document(firebaseAuth.getCurrentUser().getEmail()).collection("mystory");
        else
            return;

        storyCollections.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("getData", "onComplete method called");
                if (task.isSuccessful()) {
                    Log.d("getData", "onComplete method task successful");
                    QuerySnapshot collection = task.getResult();
                    List<DocumentSnapshot> documents = collection.getDocuments();
                    myStories = new ArrayList<>();

                    Log.d("getData", "document size : " + documents.size());

                    for (DocumentSnapshot document : documents) {
                        UserStory userStory = new UserStory();
                        userStory.setTitle((String)document.get("title"));
                        userStory.setTitle_image_url((String)document.get("title_image_url"));
                        Log.d("getData", "Title : " + (String)document.get("title"));
                        Log.d("getData", "Image Url : " + (String)document.get("title_image_url"));
                        myStories.add(userStory);
                    }

                    if (myStories.size() == 0) {
                        UserStory placeholder = new UserStory();
                        placeholder.setTitle("");
                        placeholder.setTitle_image_url(String.valueOf(R.drawable.img_story_preview_placeholder));
                        myStories.add(placeholder);
                    }

                    myStoryPreviewAdapter = new StoryPreviewAdapter(getContext(), myStories);
                    myStoryPreviewAdapter.notifyDataSetChanged();
                    myStoryRecyclerView.setAdapter(myStoryPreviewAdapter);
                    Log.d("getData", "onComplete method done");
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("getData", e.getMessage());
            }
        });
    }

    private void getOtherUsersData() {

        if (allUsersStoriesExceptMe.size() != 0)
            return;

        CollectionReference searchCollectionRef = firebaseFirestore.collection("storybook");

        searchCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot collection = task.getResult();
                    List<DocumentSnapshot> documents = collection.getDocuments();

                    for (DocumentSnapshot document : documents)
                    {
                        if (googleSignInAccount != null) {
                            if (document.getId().equals(googleSignInAccount.getEmail()))
                                continue;
                        }
                        else if (firebaseAuth != null) {
                            if (document.getId().equals(firebaseAuth.getCurrentUser().getEmail()))
                                continue;
                        }

                        searchCollectionRef.document(document.getId()).collection("mystory")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot collection = task.getResult();
                                            List<DocumentSnapshot> subDocuments = collection.getDocuments();

                                            for (DocumentSnapshot subDocument : subDocuments) {
                                                UserStory userStory = subDocument.toObject(UserStory.class);
                                                allUsersStoriesExceptMe.add(userStory);
                                            }
                                            ++completedTaskCount;

                                            if (completedTaskCount == documents.size() -1) // except my account
                                                pickOtherUsersData();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("getOtherUsersData", e.getMessage());
                                    }
                                });
                    }
                }
                else {
                    Log.d("findData", "onComplete method task failed");
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

    private void pickOtherUsersData() {

        if (allUsersStoriesExceptMe.size() <= 12) {
            otherUsersStories = allUsersStoriesExceptMe;
            otherUsersStoryPreviewAdapter = new StoryGridPreviewAdapter(getContext(), otherUsersStories);
            otherUsersStoryPreviewAdapter.notifyDataSetChanged();
            otherUsersStoryRecyclerView.setAdapter(otherUsersStoryPreviewAdapter);
            otherUsersStoryRecyclerView.getLayoutParams().height = 650 * (int)(allUsersStoriesExceptMe.size()/2+allUsersStoriesExceptMe.size()%2);
            return;
        }

        Random rand = new Random();
        random_indices.clear();
        otherUsersStories.clear();

        for (int i = 0; i < 12; ++i)
        {
            int random_index = rand.nextInt(allUsersStoriesExceptMe.size()); // 0 to size-1

            while (isDuplicatedIndex(random_index)) {
                random_index = rand.nextInt(allUsersStoriesExceptMe.size());
            }
            random_indices.add(random_index);
            otherUsersStories.add(allUsersStoriesExceptMe.get(random_index));
        }
        otherUsersStoryPreviewAdapter.notifyDataSetChanged();
        otherUsersStoryRecyclerView.setAdapter(otherUsersStoryPreviewAdapter);
        otherUsersStoryRecyclerView.getLayoutParams().height = 3900;
    }

    private boolean isDuplicatedIndex(int index) {
        for (int i = 0; i < random_indices.size(); ++i) {
            if (random_indices.get(i) == index)
                return true;
        }
        return false;
    }
}
