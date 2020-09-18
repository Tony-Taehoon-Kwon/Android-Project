package com.taehoon.kwon.travelstory.Itinerary.ExploreStories;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookRecordAdapter;
import com.taehoon.kwon.travelstory.Itinerary.model.ItineraryLocation;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.R;
import com.taehoon.kwon.travelstory.OnBackPressed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExploreStoryMainFragment extends ExploreStoryContainerFragment implements OnBackPressed {

    public static final String FRAG_TAG = "explore_story_main";

    private FragmentManager fragmentManager;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private EditText searchText;
    private ArrayList<UserStory> userStories = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExploreStoryMainAdapter exploreStoryMainAdapter;

    private List<DocumentSnapshot> documents = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore_stories, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);

        searchText = root.findViewById(R.id.searchStoryEditText);
        searchText.setOnEditorActionListener(searchEditorActionListener);

        recyclerView = root.findViewById(R.id.otherUserStoryRecyclerView);
        exploreStoryMainAdapter = new ExploreStoryMainAdapter(getContext(), userStories, fragmentManager);
        recyclerView.setAdapter(exploreStoryMainAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        return root;
    }

    private TextView.OnEditorActionListener searchEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { // (actionId, event) => (3, null), (0, ACTION_DOWN)

            if (actionId != EditorInfo.IME_ACTION_SEARCH)
                return false;

            hideSoftKeyboard();
            findData();
            return true;
        }
    };

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
    }

    private void findData() {

        CollectionReference searchCollectionRef = firebaseFirestore.collection("storybook");

        Log.d("findData", "Called!");

        searchCollectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d("findData", "onComplete method called");
                if (task.isSuccessful()) {
                    Log.d("findData", "onComplete method task successful");

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("findData : Documents", document.getId() + " => " + document.getData());
                    }

                    QuerySnapshot collection = task.getResult();
                    documents = collection.getDocuments();
                    userStories = new ArrayList<>();

                    for (DocumentSnapshot document : documents) {
                        searchCollectionRef.document(document.getId()).collection("mystory")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot collection = task.getResult();
                                            List<DocumentSnapshot> subDocuments = collection.getDocuments();

                                            for (DocumentSnapshot subDocument : subDocuments) {

                                                String title = (String)subDocument.get("title");

                                                if (title.toLowerCase().contains(searchText.getText().toString().toLowerCase())) {
                                                    UserStory userStory = subDocument.toObject(UserStory.class);
                                                    userStories.add(userStory);
                                                }
                                            }
                                            exploreStoryMainAdapter = new ExploreStoryMainAdapter(getContext(), userStories, fragmentManager);
                                            exploreStoryMainAdapter.notifyDataSetChanged();
                                            recyclerView.setAdapter(exploreStoryMainAdapter);
                                            Log.d("findData", "onComplete method done");
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
}
