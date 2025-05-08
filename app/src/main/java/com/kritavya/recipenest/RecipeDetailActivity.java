package com.kritavya.recipenest;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kritavya.recipenest.models.Recipe;
import com.kritavya.recipenest.utils.DateUtils;
import com.kritavya.recipenest.utils.RecipeDataLoader;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView recipeImage;
    private TextView tvRecipeName, tvRating, tvDate, tvTime;
    private LinearLayout categoryContainer, ingredientsContainer, instructionsContainer;
    private Button btnAddToMyRecipes, btnDownloadRecipe, btnWriteReview;
    private ImageView[] ratingStars = new ImageView[5];
    
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String recipeId;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_detail_template);
        
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        
        // Get recipe ID and source from intent
        recipeId = getIntent().getStringExtra("recipe_id");
        String recipeSource = getIntent().getStringExtra("recipe_source");
        
        if (recipeId == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        
        // Load recipe based on its source
        if ("local".equals(recipeSource)) {
            loadLocalRecipe(recipeId);
        } else {
            loadRecipeFromFirebase();
        }
        
        setupClickListeners();
    }
    
    private void initViews() {
        // Initialize views
        recipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvRating = findViewById(R.id.tvRating);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        
        categoryContainer = findViewById(R.id.categoryContainer);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        instructionsContainer = findViewById(R.id.instructionsContainer);
        
        btnAddToMyRecipes = findViewById(R.id.btnAddToMyRecipes);
        btnDownloadRecipe = findViewById(R.id.btnDownloadRecipe);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        
        // Initialize rating stars
        ratingStars[0] = findViewById(R.id.ratingStar1);
        ratingStars[1] = findViewById(R.id.ratingStar2);
        ratingStars[2] = findViewById(R.id.ratingStar3);
        ratingStars[3] = findViewById(R.id.ratingStar4);
        ratingStars[4] = findViewById(R.id.ratingStar5);
        
        // Set up back button
        ImageButton btnBackArrow = findViewById(R.id.btnBackArrow);
        if (btnBackArrow != null) {
            btnBackArrow.setOnClickListener(v -> onBackPressed());
        }
    }
    
    private void loadLocalRecipe(String recipeId) {
        // Load recipe from local JSON data
        Recipe recipe = RecipeDataLoader.getRecipeById(this, recipeId);
        if (recipe != null) {
            currentRecipe = recipe;
            updateUI(recipe);
        } else {
            Toast.makeText(this, "Local recipe not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadRecipeFromFirebase() {
        // Load recipe data from Firebase
        mDatabase.child("recipes").child(recipeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentRecipe = dataSnapshot.getValue(Recipe.class);
                    if (currentRecipe != null) {
                        updateUI(currentRecipe);
                    }
                } else {
                    Toast.makeText(RecipeDetailActivity.this, "Recipe not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RecipeDetailActivity.this, "Failed to load recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(Recipe recipe) {
        // Update UI with recipe data
        tvRecipeName.setText(recipe.getName());
        tvRating.setText(String.format("%.1f", recipe.getRating()));
        tvDate.setText(DateUtils.formatDate(recipe.getCreatedAt().getTime()));
        tvTime.setText(recipe.getCookingTime() + " Minutes");
        
        // Load recipe image and setup image indicators
        setupRecipeImages(recipe);
        
        // Update categories
        updateCategories(recipe.getCategories());
        
        // Update ingredients
        updateIngredients(recipe.getIngredients());
        
        // Update instructions
        updateInstructions(recipe.getInstructions());
        
        // Update rating stars based on the recipe's rating
        updateRatingStars(recipe.getRating());
        
        // Check if the recipe is already in user's saved recipes
        checkIfRecipeSaved();
    }
    
    private void setupRecipeImages(Recipe recipe) {
        List<String> imageUrls = new ArrayList<>();
        
        // Get all possible image sources
        if (recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty()) {
            imageUrls.addAll(recipe.getImageUrls());
        } else if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            imageUrls.add(recipe.getImageUrl());
        }
        
        // Load the first image if available
        if (!imageUrls.isEmpty()) {
            Glide.with(this)
                .load(imageUrls.get(0))
                .placeholder(R.drawable.placeholder_food)
                .into(recipeImage);
        }
        
        // Setup image indicators
        View dotIndicator1 = findViewById(R.id.dotIndicator1);
        View dotIndicator2 = findViewById(R.id.dotIndicator2);
        View dotIndicator3 = findViewById(R.id.dotIndicator3);
        ImageButton btnNextImage = findViewById(R.id.btnNextImage);
        
        // Show only the needed indicators based on image count
        if (imageUrls.size() <= 1) {
            // Hide all indicators and next button if there's only one image
            dotIndicator1.setVisibility(View.GONE);
            dotIndicator2.setVisibility(View.GONE);
            dotIndicator3.setVisibility(View.GONE);
            btnNextImage.setVisibility(View.GONE);
        } else {
            // Show only the needed number of indicators
            dotIndicator1.setVisibility(View.VISIBLE);
            dotIndicator2.setVisibility(imageUrls.size() >= 2 ? View.VISIBLE : View.GONE);
            dotIndicator3.setVisibility(imageUrls.size() >= 3 ? View.VISIBLE : View.GONE);
            btnNextImage.setVisibility(View.VISIBLE);
            
            // Set the proper tint color for indicators
            dotIndicator1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            
            // Store the image URLs for navigation
            final int[] currentImageIndex = {0};
            
            // Setup next image button click
            btnNextImage.setOnClickListener(v -> {
                currentImageIndex[0] = (currentImageIndex[0] + 1) % imageUrls.size();
                Glide.with(RecipeDetailActivity.this)
                    .load(imageUrls.get(currentImageIndex[0]))
                    .placeholder(R.drawable.placeholder_food)
                    .into(recipeImage);
                
                // Update indicators
                dotIndicator1.setBackgroundResource(currentImageIndex[0] == 0 ? 
                    R.drawable.dot_active : R.drawable.dot_inactive);
                dotIndicator1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(
                    currentImageIndex[0] == 0 ? R.color.colorAccent : R.color.colorLightText)));
                
                if (dotIndicator2.getVisibility() == View.VISIBLE) {
                    dotIndicator2.setBackgroundResource(currentImageIndex[0] == 1 ? 
                        R.drawable.dot_active : R.drawable.dot_inactive);
                    dotIndicator2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(
                        currentImageIndex[0] == 1 ? R.color.colorAccent : R.color.colorLightText)));
                }
                
                if (dotIndicator3.getVisibility() == View.VISIBLE) {
                    dotIndicator3.setBackgroundResource(currentImageIndex[0] == 2 ? 
                        R.drawable.dot_active : R.drawable.dot_inactive);
                    dotIndicator3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(
                        currentImageIndex[0] == 2 ? R.color.colorAccent : R.color.colorLightText)));
                }
            });
        }
    }
    
    private void updateCategories(List<String> categories) {
        categoryContainer.removeAllViews();
        
        if (categories != null && !categories.isEmpty()) {
            for (String category : categories) {
                TextView categoryView = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 32, 0); // right margin
                categoryView.setLayoutParams(params);
                
                categoryView.setText(category);
                categoryView.setTextColor(getResources().getColor(R.color.colorLightText));
                categoryView.setTextSize(14);
                categoryView.setPadding(48, 24, 48, 24);
                categoryView.setBackgroundResource(R.drawable.rounded_light_background);
                
                categoryContainer.addView(categoryView);
            }
        }
    }
    
    private void updateIngredients(List<Recipe.Ingredient> ingredients) {
        // Clear existing ingredients and only keep the "See More" button
        ingredientsContainer.removeAllViews();
        
        if (ingredients != null && !ingredients.isEmpty()) {
            int maxVisibleIngredients = 5;
            final boolean[] isExpanded = {false};
            
            // First add all ingredients (we'll control visibility later)
            for (Recipe.Ingredient ingredient : ingredients) {
                addIngredientView(ingredient);
                
                // Add divider except for the last item
                if (ingredients.indexOf(ingredient) < ingredients.size() - 1) {
                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    ingredientsContainer.addView(divider);
                }
            }
            
            // If there are more ingredients than the max visible count, add "See More" button
            if (ingredients.size() > maxVisibleIngredients) {
                // Initially hide extra ingredients
                for (int i = maxVisibleIngredients * 2; i < ingredientsContainer.getChildCount(); i++) {
                    ingredientsContainer.getChildAt(i).setVisibility(View.GONE);
                }
                
                // Add "See More" button
                View seeMoreView = getLayoutInflater().inflate(R.layout.item_see_more, ingredientsContainer, false);
                TextView seeMoreButton = seeMoreView.findViewById(R.id.btnSeeMore);
                seeMoreButton.setText("See More ▼");
                ingredientsContainer.addView(seeMoreView);
                
                // Set click listener for "See More" button
                seeMoreButton.setOnClickListener(v -> {
                    isExpanded[0] = !isExpanded[0];
                    
                    // Show/hide ingredients based on expanded state
                    for (int i = maxVisibleIngredients * 2; i < ingredientsContainer.getChildCount() - 1; i++) {
                        ingredientsContainer.getChildAt(i).setVisibility(isExpanded[0] ? View.VISIBLE : View.GONE);
                    }
                    
                    // Update button text
                    seeMoreButton.setText(isExpanded[0] ? "See Less ▲" : "See More ▼");
                });
            }
        }
    }
    
    private void addIngredientView(Recipe.Ingredient ingredient) {
        LinearLayout ingredientRow = new LinearLayout(this);
        ingredientRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ingredientRow.setOrientation(LinearLayout.HORIZONTAL);
        ingredientRow.setPadding(0, 48, 0, 48);
        
        TextView nameView = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameView.setLayoutParams(nameParams);
        nameView.setText(ingredient.getName());
        nameView.setTextColor(getResources().getColor(android.R.color.black));
        nameView.setTextSize(15);
        
        TextView amountView = new TextView(this);
        amountView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        amountView.setText(ingredient.getAmount() + " " + ingredient.getUnit());
        amountView.setTextColor(getResources().getColor(android.R.color.black));
        amountView.setTextSize(15);
        amountView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        
        ingredientRow.addView(nameView);
        ingredientRow.addView(amountView);
        
        ingredientsContainer.addView(ingredientRow);
    }
    
    private void updateInstructions(List<Recipe.Instruction> instructions) {
        // Clear existing instructions
        instructionsContainer.removeAllViews();
        
        if (instructions != null && !instructions.isEmpty()) {
            int maxVisibleInstructions = 2;
            final boolean[] isExpanded = {false};
            
            // First add all instructions (we'll control visibility later)
            int count = 0;
            for (Recipe.Instruction instruction : instructions) {
                // Add step title with proper styling to match the image
                TextView stepTitle = new TextView(this);
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                titleParams.setMargins(0, (count > 0) ? 24 : 0, 0, 8);
                stepTitle.setLayoutParams(titleParams);
                stepTitle.setText((count + 1) + ". " + instruction.getTitle());
                stepTitle.setTextColor(getResources().getColor(android.R.color.black));
                stepTitle.setTextSize(16);
                stepTitle.setTypeface(Typeface.DEFAULT_BOLD);
                
                instructionsContainer.addView(stepTitle);
                
                // Create a container for instruction steps with less congestion
                for (String step : instruction.getSteps()) {
                    TextView stepDesc = new TextView(this);
                    LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    stepParams.setMargins(0, 0, 0, 8);
                    stepDesc.setLayoutParams(stepParams);
                    stepDesc.setText("• " + step);
                    stepDesc.setTextColor(getResources().getColor(android.R.color.black));
                    stepDesc.setTextSize(15);
                    
                    instructionsContainer.addView(stepDesc);
                }
                
                // Add a light gray divider between instructions
                if (instructions.indexOf(instruction) < instructions.size() - 1 && count < maxVisibleInstructions) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    dividerParams.setMargins(0, 16, 0, 0);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(0xFFE0E0E0); // Light gray color
                    instructionsContainer.addView(divider);
                }
                
                count++;
                
                // Only add maxVisibleInstructions instructions initially
                if (count >= maxVisibleInstructions && !isExpanded[0]) {
                    break;
                }
            }
            
            // If there are more instructions than the max visible count, implement "See More" functionality
            if (instructions.size() > maxVisibleInstructions) {
                // Store the remaining instructions for showing/hiding
                final List<Recipe.Instruction> remainingInstructions = 
                    new ArrayList<>(instructions.subList(maxVisibleInstructions, instructions.size()));
                
                // Save original view state so we can restore it when collapsing
                final List<View> originalViews = new ArrayList<>();
                for (int i = 0; i < instructionsContainer.getChildCount(); i++) {
                    originalViews.add(instructionsContainer.getChildAt(i));
                }
                
                // Add "See More" button view
                View seeMoreView = getLayoutInflater().inflate(R.layout.item_see_more, instructionsContainer, false);
                TextView seeMoreButton = seeMoreView.findViewById(R.id.btnSeeMore);
                seeMoreButton.setText("See More ▼");
                
                // Add the view to the container
                instructionsContainer.addView(seeMoreView);
                
                // Set up See More/Less button click listener
                seeMoreButton.setOnClickListener(v -> {
                    isExpanded[0] = !isExpanded[0];
                    
                    // Handle expand/collapse
                    if (isExpanded[0]) {
                        // Show remaining instructions
                        int currentPosition = maxVisibleInstructions;
                        for (Recipe.Instruction instruction : remainingInstructions) {
                            // Add divider before additional instructions
                            View divider = new View(this);
                            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
                            dividerParams.setMargins(0, 16, 0, 0);
                            divider.setLayoutParams(dividerParams);
                            divider.setBackgroundColor(0xFFE0E0E0); // Light gray color
                            instructionsContainer.addView(divider, instructionsContainer.indexOfChild(seeMoreView));
                            
                            // Add instruction title
                            TextView stepTitle = new TextView(this);
                            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            titleParams.setMargins(0, 24, 0, 8);
                            stepTitle.setLayoutParams(titleParams);
                            stepTitle.setText((currentPosition + 1) + ". " + instruction.getTitle());
                            stepTitle.setTextColor(getResources().getColor(android.R.color.black));
                            stepTitle.setTextSize(16);
                            stepTitle.setTypeface(Typeface.DEFAULT_BOLD);
                            instructionsContainer.addView(stepTitle, instructionsContainer.indexOfChild(seeMoreView));
                            
                            // Add instruction steps
                            for (String step : instruction.getSteps()) {
                                TextView stepDesc = new TextView(this);
                                LinearLayout.LayoutParams stepParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                                stepParams.setMargins(0, 0, 0, 8);
                                stepDesc.setLayoutParams(stepParams);
                                stepDesc.setText("• " + step);
                                stepDesc.setTextColor(getResources().getColor(android.R.color.black));
                                stepDesc.setTextSize(15);
                                instructionsContainer.addView(stepDesc, instructionsContainer.indexOfChild(seeMoreView));
                            }
                            
                            currentPosition++;
                        }
                        
                        // Hide the gradient
                        if (seeMoreView instanceof FrameLayout && ((FrameLayout) seeMoreView).getChildCount() > 0) {
                            View gradientView = ((FrameLayout) seeMoreView).getChildAt(0);
                            gradientView.setVisibility(View.GONE);
                        }
                    } else {
                        // Clear the container and restore original state
                        instructionsContainer.removeAllViews();
                        
                        // Re-add the original views
                        for (View view : originalViews) {
                            instructionsContainer.addView(view);
                        }
                        
                        // Re-add the See More button
                        instructionsContainer.addView(seeMoreView);
                        
                        // Show the gradient
                        if (seeMoreView instanceof FrameLayout && ((FrameLayout) seeMoreView).getChildCount() > 0) {
                            View gradientView = ((FrameLayout) seeMoreView).getChildAt(0);
                            gradientView.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    // Update button text
                    seeMoreButton.setText(isExpanded[0] ? "See Less ▲" : "See More ▼");
                });
            }
        }
    }
    
    private int getIndexOfInstruction(int instructionNumber) {
        int index = 0;
        int currentInstruction = 0;
        
        while (index < instructionsContainer.getChildCount()) {
            View view = instructionsContainer.getChildAt(index);
            if (view instanceof TextView && 
                ((TextView) view).getText().toString().matches("\\d+\\..*")) {
                currentInstruction++;
                if (currentInstruction == instructionNumber) {
                    return index;
                }
            }
            index++;
        }
        
        return 0;
    }
    
    private void updateRatingStars(float rating) {
        int filledStars = (int) Math.floor(rating);
        boolean hasHalfStar = rating - filledStars >= 0.5f;
        
        for (int i = 0; i < 5; i++) {
            if (i < filledStars) {
                ratingStars[i].setImageResource(android.R.drawable.btn_star_big_on);
                ratingStars[i].setColorFilter(getResources().getColor(R.color.colorAccent));
            } else if (i == filledStars && hasHalfStar) {
                // This is simplified since Android doesn't have a built-in half star
                // In a real app, you'd use a custom drawable for half-star
                ratingStars[i].setImageResource(android.R.drawable.btn_star_big_on);
                ratingStars[i].setColorFilter(getResources().getColor(R.color.colorAccent));
                ratingStars[i].setAlpha(0.5f);
            } else {
                ratingStars[i].setImageResource(android.R.drawable.btn_star_big_off);
                ratingStars[i].setColorFilter(getResources().getColor(R.color.colorLightText));
            }
        }
    }
    
    private void checkIfRecipeSaved() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && currentRecipe != null) {
            mDatabase.child("users").child(user.getUid()).child("savedRecipes")
                    .child(currentRecipe.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Recipe is already saved
                        btnAddToMyRecipes.setText("Remove from My Recipes");
                    } else {
                        // Recipe is not saved
                        btnAddToMyRecipes.setText("Add to My Recipes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
    
    private void setupClickListeners() {
        // Add to My Recipes button
        btnAddToMyRecipes.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && currentRecipe != null) {
                toggleSaveRecipe(user.getUid(), currentRecipe.getId());
            } else {
                // Prompt to sign in
                Toast.makeText(this, "Please sign in to save recipes", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Download Recipe button
        btnDownloadRecipe.setOnClickListener(v -> {
            // Implementation for downloading recipe
            Toast.makeText(this, "Recipe download started", Toast.LENGTH_SHORT).show();
        });
        
        // Write Review button
        btnWriteReview.setOnClickListener(v -> {
            // Open review writing dialog/screen
            Toast.makeText(this, "Write a review", Toast.LENGTH_SHORT).show();
        });
        
        // Rating stars click listeners
        for (int i = 0; i < ratingStars.length; i++) {
            final int rating = i + 1;
            ratingStars[i].setOnClickListener(v -> submitRating(rating));
        }
    }
    
    private void toggleSaveRecipe(String userId, String recipeId) {
        mDatabase.child("users").child(userId).child("savedRecipes")
                .child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Recipe is saved, so remove it
                    mDatabase.child("users").child(userId).child("savedRecipes")
                            .child(recipeId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                btnAddToMyRecipes.setText("Add to My Recipes");
                                Toast.makeText(RecipeDetailActivity.this, 
                                    "Recipe removed from your collection", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(RecipeDetailActivity.this, 
                                    "Failed to remove recipe", Toast.LENGTH_SHORT).show()
                            );
                } else {
                    // Recipe is not saved, so add it
                    mDatabase.child("users").child(userId).child("savedRecipes")
                            .child(recipeId).setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                btnAddToMyRecipes.setText("Remove from My Recipes");
                                Toast.makeText(RecipeDetailActivity.this, 
                                    "Recipe added to your collection", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> 
                                Toast.makeText(RecipeDetailActivity.this, 
                                    "Failed to save recipe", Toast.LENGTH_SHORT).show()
                            );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RecipeDetailActivity.this, 
                    "Error updating recipe status", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void submitRating(int rating) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && currentRecipe != null) {
            // Submit user rating to Firebase
            mDatabase.child("ratings").child(currentRecipe.getId())
                    .child(user.getUid()).setValue(rating)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(RecipeDetailActivity.this, 
                            "Thank you for rating!", Toast.LENGTH_SHORT).show();
                        
                        // Update UI to show the user's rating
                        updateRatingStars(rating);
                        
                        // Recalculate the average rating for this recipe
                        recalculateAverageRating(currentRecipe.getId());
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(RecipeDetailActivity.this, 
                            "Failed to submit rating", Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Prompt to sign in
            Toast.makeText(this, "Please sign in to rate recipes", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void recalculateAverageRating(String recipeId) {
        mDatabase.child("ratings").child(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    float totalRating = 0;
                    int count = 0;
                    
                    for (DataSnapshot ratingSnapshot : dataSnapshot.getChildren()) {
                        Integer rating = ratingSnapshot.getValue(Integer.class);
                        if (rating != null) {
                            totalRating += rating;
                            count++;
                        }
                    }
                    
                    if (count > 0) {
                        float averageRating = totalRating / count;
                        
                        // Update recipe's average rating in the database
                        mDatabase.child("recipes").child(recipeId)
                                .child("rating").setValue(averageRating);
                        
                        // Update UI with new rating
                        tvRating.setText(String.format("%.1f", averageRating));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
} 