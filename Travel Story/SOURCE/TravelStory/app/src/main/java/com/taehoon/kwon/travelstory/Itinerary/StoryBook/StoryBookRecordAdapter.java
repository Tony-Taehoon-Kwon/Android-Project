package com.taehoon.kwon.travelstory.Itinerary.StoryBook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.taehoon.kwon.travelstory.Itinerary.model.UserStory;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.util.ArrayList;
import java.util.Date;

public class StoryBookRecordAdapter extends RecyclerView.Adapter<StoryBookRecordAdapter.ViewHolder> {

    private Context context;
    private ArrayList<UserStory> userStories;
    private FragmentManager fragmentManager;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private GoogleSignInAccount googleSignInAccount;

    public StoryBookRecordAdapter(Context context, ArrayList<UserStory> userStories, FragmentManager fragmentManager) {
        this.context = context;
        this.userStories = userStories;
        this.fragmentManager = fragmentManager;
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserStory userStory = userStories.get(position);
        holder.titleText.setText(userStory.getTitle());
        holder.date.setText(getDateDurationInString(userStory.getDate_start(), userStory.getDate_end()));
        Glide.with(context).load(String.valueOf(userStory.getTitle_image_url())).centerCrop().into(holder.titleImage);
    }

    @Override
    public int getItemCount() {
        return userStories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView titleImage;
        TextView titleText;
        TextView date;
        ImageButton actionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleImage = itemView.findViewById(R.id.storyItemImage);
            titleText = itemView.findViewById(R.id.storyItemTitleText);
            date = itemView.findViewById(R.id.storyItemDateText);
            actionButton = itemView.findViewById(R.id.storyMainActionButton);

            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String options[] = new String[3];
                    options[0] = "Edit";
                    options[1] = "Remove";
                    options[2] = "Cancel";

                    final AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setTitle("Choose action")
                            .setCancelable(true)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            Bundle args = new Bundle();
                                            args.putParcelable("edit_user_story", userStories.get(getAdapterPosition()));

                                            Fragment fragment = new StoryBookNewRecordFragment();
                                            fragment.setArguments(args);

                                            fragmentManager.beginTransaction()
                                                    .add(R.id.fragment_container, fragment, StoryBookNewRecordFragment.FRAG_TAG)
                                                    .hide(fragmentManager.findFragmentByTag(StoryBookMainFragment.FRAG_TAG))
                                                    .commit();
                                            MainActivity.current_fragment_tag = StoryBookNewRecordFragment.FRAG_TAG;
                                            break;
                                        case 1:
                                            dialog.cancel();
                                            callRemoveAlert();
                                            break;
                                        case 2: dialog.cancel();       break;
                                    }
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        private void callRemoveAlert() {
            final AlertDialog.Builder removeBuilder = new AlertDialog.Builder(itemView.getContext());
            removeBuilder.setMessage("Are you sure you want to remove " + titleText.getText().toString() + "?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            DocumentReference selectedDocument;

                            if (googleSignInAccount != null)
                                selectedDocument =  firebaseFirestore.collection("storybook")
                                        .document(googleSignInAccount.getEmail())
                                        .collection("mystory")
                                        .document(titleText.getText().toString());
                            else if (firebaseAuth != null)
                                selectedDocument = firebaseFirestore.collection("storybook")
                                        .document(firebaseAuth.getCurrentUser().getEmail())
                                        .collection("mystory")
                                        .document(titleText.getText().toString());
                            else
                                return;

                            selectedDocument.delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            int position = getAdapterPosition();
                                            userStories.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, userStories.size());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Story Remove", "Story Remove Error", e);
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog removeAlert = removeBuilder.create();
            removeAlert.show();
        }
    }

    private String getDateDurationInString (Date startDate, Date endDate) {

        String[] start_temp = startDate.toString().split(" ", 6); // DayOfWeek Month DayOfMonth Time TimeZone Year
        String[] end_temp = endDate.toString().split(" ", 6); // DayOfWeek Month DayOfMonth Time TimeZone Year

        String start_month = getMonthInIntValueStringFormat(start_temp[1]);
        String end_month = getMonthInIntValueStringFormat(end_temp[1]);

        StringBuilder builder = new StringBuilder();
        builder.append(start_month);
        builder.append(".");
        builder.append(start_temp[2]);
        builder.append(".");
        builder.append(start_temp[5]);
        builder.append(" ~ ");
        builder.append(end_month);
        builder.append(".");
        builder.append(end_temp[2]);
        builder.append(".");
        builder.append(end_temp[5]);

        return builder.toString();
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
