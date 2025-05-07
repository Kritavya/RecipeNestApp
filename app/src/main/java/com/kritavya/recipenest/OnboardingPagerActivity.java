package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.kritavya.recipenest.adapters.OnboardingAdapter;
import com.kritavya.recipenest.models.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingPagerActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private OnboardingAdapter adapter;
    private Button btnNext, btnSkip;
    private List<OnboardingItem> onboardingItems;
    private ImageView[] dots;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_pager);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);

        // Setup onboarding items
        setupOnboardingItems();

        // Setup adapter
        adapter = new OnboardingAdapter(this, onboardingItems, viewPager);
        viewPager.setAdapter(adapter);

        // Setup dots
        setupDots(0);

        // Setup listeners
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                currentPosition = position;

                // Change button text on last page
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Get Started");
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText("Next");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPosition < onboardingItems.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1);
            } else {
                // On last page, proceed to signup
                startActivity(new Intent(OnboardingPagerActivity.this, SignupActivity.class));
                finish();
            }
        });

        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingPagerActivity.this, SignupActivity.class));
            finish();
        });
    }

    private void setupOnboardingItems() {
        onboardingItems = new ArrayList<>();

        // First slide - Browse thousands of recipe (original onboarding2)
        onboardingItems.add(new OnboardingItem(
                "Browse thousands of recipe",
                "Explore recipes from around the world",
                R.drawable.onboarding_discover // Placeholder, not used
        ));

        // Second slide - Personalized feed (original onboarding3)
        onboardingItems.add(new OnboardingItem(
                "Personalized your feed",
                "Get recommendations based on your preferences",
                R.drawable.onboarding_cook // Placeholder, not used
        ));

        // Third slide - Save your favorite recipe
        onboardingItems.add(new OnboardingItem(
                "Save your favorite recipe",
                "Keep track of recipes you love and access them anytime",
                R.drawable.onboarding_save
        ));

        // Fourth slide - Share with friends (original onboarding4)
        onboardingItems.add(new OnboardingItem(
                "Share with friends!",
                "Share your cooking experiences on social media",
                R.drawable.onboarding_save // Placeholder, not used
        ));
    }

    private void setupDots(int position) {
        // Clear existing dots
        dotsLayout.removeAllViews();
        dots = new ImageView[onboardingItems.size()];

        // Create new dots
        for (int i = 0; i < onboardingItems.size(); i++) {
            dots[i] = new ImageView(this);
            if (i == position) {
                dots[i].setImageResource(R.drawable.indicator_active);
            } else {
                dots[i].setImageResource(R.drawable.indicator_inactive);
            }

            // Set layout params - make dots larger
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    32, 32
            );
            params.setMargins(16, 0, 16, 0);
            dots[i].setLayoutParams(params);

            // Add to layout
            dotsLayout.addView(dots[i], params);
            
            // Add click listener for navigation
            final int clickPosition = i;
            dots[i].setOnClickListener(v -> viewPager.setCurrentItem(clickPosition));
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPosition > 0) {
            viewPager.setCurrentItem(currentPosition - 1);
        } else {
            super.onBackPressed();
        }
    }
} 