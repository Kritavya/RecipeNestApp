package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);


        Button btnGetStarted = findViewById(R.id.btn_get_started);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);


        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(this, OnboardingActivity2.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
