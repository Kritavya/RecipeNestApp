package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    private static final int SPLASH_DELAY = 1000;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        ImageView splashLogo = findViewById(R.id.splash_logo);
        splashLogo.setImageResource(R.drawable.local_logo);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndNavigate, SPLASH_DELAY);
    }

    private void checkUserAndNavigate() {
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null) {
            // User is already logged in, go directly to MainActivity
            navigateToMain();
        } else {
            // User is not logged in, show onboarding first
            navigateToOnboarding();
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        finish();
    }

    private void navigateToOnboarding() {
        startActivity(new Intent(SplashScreen.this, OnboardingPagerActivity.class));
        finish();
    }
}
