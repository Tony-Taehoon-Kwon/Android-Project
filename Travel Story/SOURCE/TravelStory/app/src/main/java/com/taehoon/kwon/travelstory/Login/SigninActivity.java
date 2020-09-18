package com.taehoon.kwon.travelstory.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.maps.errors.ApiException;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SigninActivity extends AppCompatActivity {

    private ImageView wallPaper;
    private Button signInWithEmailBtn;
    private Button signInWithGoogleBtn;
    public static GoogleSignInClient googleSignInClient;
    private final int RC_SIGN_IN = 1001;

    // Firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        wallPaper = findViewById(R.id.wallPaper);
        signInWithGoogleBtn = findViewById(R.id.signinWithGoogle);
        signInWithEmailBtn  = findViewById(R.id.signinWithEmail);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        loadImageFromStorage("/data/user/0/com.taehoon.kwon.travelstory/app_imageDir");

        signInWithGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        signInWithEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), SigninWithEmailActivity.class);
                startActivity(startIntent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void updateUI(FirebaseUser user) {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (googleSignInAccount != null) {
            String personName = googleSignInAccount.getDisplayName();
            String personGivenName = googleSignInAccount.getGivenName();
            String personFamilyName = googleSignInAccount.getFamilyName();
            String personEmail = googleSignInAccount.getEmail();
            String personId = googleSignInAccount.getId();
            Uri personPhoto = googleSignInAccount.getPhotoUrl();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onPause() {
        super.onPause();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount googleSignInAccount = completedTask.getResult(ApiException.class);
            firebaseGoogleAuth(googleSignInAccount);
        }
        catch (ApiException e) {
            ToastMessage("Login Failed");
            firebaseGoogleAuth(null);
        }
    }

    private void firebaseGoogleAuth(GoogleSignInAccount googleSignInAccount) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Successfully signed in
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                    Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(startIntent);
                }
                else {
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    ToastMessage("Login failed. Email or password does not match");
                    updateUI(null);
                }
            }
        });
    }

    private void ToastMessage(String message) {
        Toast.makeText(SigninActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadImageFromStorage(String path)
    {
        try {
            File file = new File(path, "custom_background.jpg");
            if (file != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                Glide.with(this).load(bitmap).into(wallPaper);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
