package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private static final int SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Set splash screen logo from local drawable
        ImageView splashLogo = findViewById(R.id.splash_logo);
        splashLogo.setImageResource(R.drawable.local_logo); // Set local image

        // Delay of 3 seconds, then go to Onboarding screen
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToOnboarding, SPLASH_DELAY);
    }

    private void navigateToOnboarding() {
        startActivity(new Intent(SplashScreen.this, OnboardingActivity.class));
        finish();
    }
}
