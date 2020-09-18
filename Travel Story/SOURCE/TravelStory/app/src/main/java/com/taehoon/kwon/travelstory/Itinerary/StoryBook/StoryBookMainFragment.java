package com.taehoon.kwon.travelstory.Itinerary.StoryBook;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.OnBackPressed;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;
import java.util.List;

public class StoryBookMainFragment extends StoryBookContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "story_book_main";

    private FragmentManager fragmentManager;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private GoogleSignInAccount googleSignInAccount;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private ArrayList<UserStory> userStories = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private StoryBookRecordAdapter storyBookRecordAdapter;

    private FloatingActionButton addStoryButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_story_book_main, container, false);

        Log.d("FRAG_TAG", "Before Frag tag : " + MainActivity.current_fragment_tag);
        MainActivity.current_fragment_tag = FRAG_TAG;
        Log.d("FRAG_TAG", "Current Frag tag : " + MainActivity.current_fragment_tag);

        fragmentManager = getActivity().getSupportFragmentManager();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        recyclerView = root.findViewById(R.id.storyRecyclerView);
        storyBookRecordAdapter = new StoryBookRecordAdapter(getContext(), userStories, fragmentManager);
        recyclerView.setAdapter(storyBookRecordAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        getData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        addStoryButton = root.findViewById(R.id.addStoryButton);
        addStoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, new StoryBookNewRecordFragment(), StoryBookNewRecordFragment.FRAG_TAG)
                        .hide(fragmentManager.findFragmentByTag(FRAG_TAG))
                        .setCustomAnimations(R.anim.anim_switch_from_left, R.anim.anim_switch_to_right)
                        .commit();

                Log.d("FRAG_TAG", "Current Frag tag : " + MainActivity.current_fragment_tag);
                MainActivity.current_fragment_tag = StoryBookNewRecordFragment.FRAG_TAG;
                Log.d("FRAG_TAG", "After Frag tag : " + MainActivity.current_fragment_tag);
            }
        });

        return root;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            if (getArguments() != null) {
                boolean storyAdded = getArguments().getBoolean("story_added", false);
                if (storyAdded)
                    getData();
            }
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void getData() {

        swipeRefreshLayout.setRefreshing(true);

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
                    userStories = new ArrayList<>();

                    Log.d("getData", "document size : " + documents.size());

                    for (DocumentSnapshot document : documents) {
                        UserStory userStory = document.toObject(UserStory.class);
                        Log.d("getData", "Title : " + (String)document.get("title"));
                        Log.d("getData", "Image Url : " + (String)document.get("title_image_url"));
                        userStories.add(userStory);
                    }
                    storyBookRecordAdapter = new StoryBookRecordAdapter(getContext(), userStories, fragmentManager);
                    storyBookRecordAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(storyBookRecordAdapter);
                    Log.d("getData", "onComplete method done");
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("getData", e.getMessage());
            }
        });
    }
}
