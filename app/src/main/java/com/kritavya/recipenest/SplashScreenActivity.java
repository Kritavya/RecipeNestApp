package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {
    
    private static final int SPLASH_DURATION = 1500; // 1.5 seconds
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Use a handler to delay the check slightly to allow Firebase to initialize
        // and to show the splash screen for a minimum amount of time
        new Handler().postDelayed(this::checkUserLoginStatus, SPLASH_DURATION);
    }
    
    private void checkUserLoginStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        Intent intent;
        if (currentUser != null) {
            // User is logged in, go to MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User is not logged in, go to OnboardingActivity
            intent = new Intent(this, OnboardingActivity.class);
        }
        
        // Start the appropriate activity
        startActivity(intent);
        
        // Finish this activity to prevent going back to splash
        finish();
    }
} 