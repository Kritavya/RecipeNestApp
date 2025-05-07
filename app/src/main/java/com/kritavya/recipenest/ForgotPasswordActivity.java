package com.kritavya.recipenest;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSubmit;
    private ProgressBar progressBar;
    
    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.et_email);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progressBar);
        
        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.GONE);
        }

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address");
                etEmail.requestFocus();
            } else {
                resetPassword(email);
            }
        });
    }
    
    private void resetPassword(String email) {
        btnSubmit.setEnabled(false);
        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        
        // Add timeout handler
        final Handler timeoutHandler = new Handler();
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Request timed out. Please check your internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        };
        
        // Set 15-second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 15000);
        
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Cancel timeout handler
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        
                        if (progressBar != null) {
                            progressBar.setVisibility(ProgressBar.GONE);
                        }
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Reset link sent to your email!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            btnSubmit.setEnabled(true);
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Cancel timeout handler
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    if (progressBar != null) {
                        progressBar.setVisibility(ProgressBar.GONE);
                    }
                    btnSubmit.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
