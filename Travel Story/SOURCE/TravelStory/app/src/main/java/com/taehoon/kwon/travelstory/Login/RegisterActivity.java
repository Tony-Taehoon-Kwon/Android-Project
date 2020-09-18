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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.taehoon.kwon.travelstory.MainActivity;
import com.taehoon.kwon.travelstory.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private Button registerButton;

    // Firebase
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = (EditText)findViewById(R.id.registerEmailEditText);
        passwordEditText = (EditText)findViewById(R.id.registerPWEditText);
        passwordConfirmEditText = (EditText)findViewById(R.id.registerConfirmPWEditText);
        registerButton   = (Button)findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String passwordConfirm = passwordConfirmEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    emailEditText.setError("Email Required");
                    emailEditText.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError("Valid Email Required");
                    emailEditText.requestFocus();
                    return;
                }

                if (password.isEmpty() || password.length() < 6) {
                    passwordEditText.setError("6 char password required");
                    passwordEditText.requestFocus();
                    return;
                }

                if (!passwordConfirm.equals(password)) {
                    passwordConfirmEditText.setError("Your password and confirmation password do not match");
                    passwordConfirmEditText.requestFocus();
                    return;
                }

                signUpWithEmail(email, password);
            }
        });
    }

    private void signUpWithEmail(final String email, final String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signup", "createUserWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            signInWithEmail(email, password);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signup", "createUserWithEmail:failure", task.getException());
                            ToastMessage("Signup failed.");
                        }
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

    private void ToastMessage(String message)
    {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        overridePendingTransition(0, 0);
    }
}
