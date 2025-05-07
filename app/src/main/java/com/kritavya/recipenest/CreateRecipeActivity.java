package com.kritavya.recipenest;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.kritavya.recipenest.adapters.CreateRecipeAdapter;

public class CreateRecipeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout navHome;
    private ImageView[] stepDots;
    private CreateRecipeAdapter adapter;
    private int[] layouts = new int[]{
            R.layout.recipe_step1_basic_info,
            R.layout.recipe_step2_ingredients,
            R.layout.recipe_step3_instructions,
            R.layout.recipe_step4_additional_info,
            R.layout.recipe_step5_review
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure window to handle system insets
        configureSystemUi();
        
        setContentView(R.layout.activity_create_recipe);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        navHome = findViewById(R.id.nav_home);
        
        // Set up navbar clicks
        setupBottomNavigation();
        
        // Set up step indicators
        setupStepIndicators();
        
        // Set up the adapter
        adapter = new CreateRecipeAdapter(this, layouts);
        viewPager.setAdapter(adapter);
        adapter.setViewPager(viewPager);
        viewPager.setUserInputEnabled(false); // Disable swiping
        
        // Set up page change callback
        setupPageChangeCallback();
        
        // Initialize the UI for first page
        updateUI(0);
    }
    
    private void configureSystemUi() {
        // Make system bars (status and navigation) light
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Set status bar color to light background
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        
        // Make status bar icons dark (for light background)
        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(flags);
        
        // Ensure status bar is visible
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    
    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            finish(); // Go back to home
        });
    }
    
    private void setupStepIndicators() {
        View dotsLayout = findViewById(R.id.dotsLayout);
        stepDots = new ImageView[layouts.length];
        
        for (int i = 0; i < stepDots.length; i++) {
            stepDots[i] = new ImageView(this);
            stepDots[i].setImageResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            ((android.widget.LinearLayout) dotsLayout).addView(stepDots[i], params);
        }
    }
    
    private void setupPageChangeCallback() {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }
        });
    }
    
    private void updateUI(int position) {
        // Update step indicator dots
        for (int i = 0; i < stepDots.length; i++) {
            stepDots[i].setImageResource(i <= position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
    
    public void goToNextStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < layouts.length - 1) {
            viewPager.setCurrentItem(currentItem + 1);
        } else {
            finishRecipeCreation();
        }
    }
    
    public void goToPreviousStep() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1);
        } else {
            finish();
        }
    }
    
    private void finishRecipeCreation() {
        // Save recipe and finish
        // This would involve collecting data from all steps and saving to Firebase
        finish();
    }
} 