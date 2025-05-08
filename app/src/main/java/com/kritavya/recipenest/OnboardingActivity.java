package com.kritavya.recipenest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OnboardingActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Handle system UI for immersive experience
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Make status bar transparent
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            
            // Make navigation bar semi-transparent
            getWindow().setNavigationBarColor(Color.parseColor("#80000000"));
            
            // Set the UI flags for a more immersive experience
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Light status bar for better contrast against dark background
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
        }
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, proceed to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_onboarding);

        Button btnGetStarted = findViewById(R.id.btn_get_started);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);

        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, OnboardingPagerActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
