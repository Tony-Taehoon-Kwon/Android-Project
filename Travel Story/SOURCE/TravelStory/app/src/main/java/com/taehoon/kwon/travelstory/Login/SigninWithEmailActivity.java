package com.taehoon.kwon.travelstory.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

public class SigninWithEmailActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText emailSignInEmailEditText;
    private EditText emailSignInPWEditText;
    private TextView registerTextView;

    // Firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_with_email);

        registerButton = findViewById(R.id.registerButton);
        emailSignInEmailEditText = findViewById(R.id.emailSignInEmailEditText);
        emailSignInPWEditText = findViewById(R.id.emailSignInPWEditText);
        registerTextView = findViewById(R.id.registerTextView);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailSignInEmailEditText.getText().toString().trim();
                String password = emailSignInPWEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    emailSignInEmailEditText.setError("Email Required");
                    emailSignInEmailEditText.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailSignInEmailEditText.setError("Valid Email Required");
                    emailSignInEmailEditText.requestFocus();
                    return;
                }

                if (password.isEmpty() || password.length() < 6) {
                    emailSignInPWEditText.setError("6 char password required");
                    emailSignInPWEditText.requestFocus();
                    return;
                }

                signInWithEmail(email, password);
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(startIntent);
            }
        });
    }

    private void signInWithEmail(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signin", "signInWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(startIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signin", "signInWithEmail:failure", task.getException());
                            ToastMessage("Login failed. Email or password does not match");
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void ToastMessage(String message) {
        Toast.makeText(SigninWithEmailActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
