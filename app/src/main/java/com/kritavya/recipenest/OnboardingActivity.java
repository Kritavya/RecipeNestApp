package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OnboardingActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
