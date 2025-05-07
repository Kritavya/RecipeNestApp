package com.kritavya.recipenest;

import android.graphics.Typeface;
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

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView recipeImage;
    private TextView tvRecipeName, tvRating, tvDate, tvTime;
    private LinearLayout categoryContainer, ingredientsContainer, instructionsContainer;
    private Button btnAddToMyRecipes, btnDownloadRecipe, btnWriteReview;
    private TextView btnSeeMoreIngredients, btnSeeMoreInstructions;
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
        
        // Get recipe ID from intent
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadRecipeData();
        setupClickListeners();
    }
    
    private void initViews() {
        // Initialize all views
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
        
        btnSeeMoreIngredients = findViewById(R.id.btnSeeMoreIngredients);
        btnSeeMoreInstructions = findViewById(R.id.btnSeeMoreInstructions);
        
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
    
    private void loadRecipeData() {
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
        
        // Load recipe image
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .into(recipeImage);
        }
        
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
                categoryView.setTextColor(getResources().getColor(android.R.color.darker_gray));
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
            int count = 0;
            
            for (Recipe.Ingredient ingredient : ingredients) {
                if (count < maxVisibleIngredients) {
                    addIngredientView(ingredient);
                    
                    // Add divider except for the last visible item
                    if (count < maxVisibleIngredients - 1 && count < ingredients.size() - 1) {
                        View divider = new View(this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                        ingredientsContainer.addView(divider);
                    }
                }
                count++;
            }
            
            // Add "See More" button if there are more ingredients
            if (ingredients.size() > maxVisibleIngredients) {
                addSeeMoreIngredientsView();
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
    
    private void addSeeMoreIngredientsView() {
        // Add a gradient blur effect and "See More" button
        LinearLayout seeMoreContainer = new LinearLayout(this);
        seeMoreContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        seeMoreContainer.setOrientation(LinearLayout.VERTICAL);
        
        View blurView = new View(this);
        blurView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                getResources().getDimensionPixelSize(R.dimen.blur_height)));
        blurView.setBackgroundResource(R.drawable.gradient_blur);
        
        TextView seeMoreButton = new TextView(this);
        seeMoreButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        seeMoreButton.setText("See More ▼");
        seeMoreButton.setTextColor(getResources().getColor(R.color.colorAccent));
        seeMoreButton.setTextSize(14);
        seeMoreButton.setPadding(32, 32, 32, 32);
        seeMoreButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        
        seeMoreContainer.addView(blurView);
        seeMoreContainer.addView(seeMoreButton);
        
        ingredientsContainer.addView(seeMoreContainer);
        
        // Set click listener
        seeMoreButton.setOnClickListener(v -> {
            // Show all ingredients (implementation would depend on app design)
            Toast.makeText(RecipeDetailActivity.this, "Show all ingredients", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void updateInstructions(List<Recipe.Instruction> instructions) {
        // Implement similar to updateIngredients
        // This would be a simplified version as the full implementation would be similar
        
        instructionsContainer.removeAllViews();
        
        if (instructions != null && !instructions.isEmpty()) {
            int maxVisibleInstructions = 2;
            int count = 0;
            
            for (Recipe.Instruction instruction : instructions) {
                if (count < maxVisibleInstructions) {
                    // Add step title
                    TextView stepTitle = new TextView(this);
                    stepTitle.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    stepTitle.setText((count + 1) + ". " + instruction.getTitle());
                    stepTitle.setTextColor(getResources().getColor(android.R.color.black));
                    stepTitle.setTextSize(16);
                    stepTitle.setTypeface(Typeface.DEFAULT_BOLD);
                    stepTitle.setPadding(0, 0, 0, 32);
                    
                    instructionsContainer.addView(stepTitle);
                    
                    // Add step descriptions
                    for (String step : instruction.getSteps()) {
                        TextView stepDesc = new TextView(this);
                        stepDesc.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        stepDesc.setText("• " + step);
                        stepDesc.setTextColor(getResources().getColor(android.R.color.black));
                        stepDesc.setTextSize(15);
                        stepDesc.setPadding(0, 0, 0, 16);
                        
                        instructionsContainer.addView(stepDesc);
                    }
                }
                count++;
            }
            
            // Add "See More" button if there are more instructions
            if (instructions.size() > maxVisibleInstructions) {
                // Add a gradient blur and see more button (similar to ingredients)
                FrameLayout seeMoreContainer = new FrameLayout(this);
                seeMoreContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                
                View blurView = new View(this);
                blurView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, 
                        getResources().getDimensionPixelSize(R.dimen.blur_height)));
                blurView.setBackgroundResource(R.drawable.gradient_blur);
                
                TextView seeMoreButton = new TextView(this);
                FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                textParams.gravity = android.view.Gravity.CENTER;
                seeMoreButton.setLayoutParams(textParams);
                seeMoreButton.setText("See More ▼");
                seeMoreButton.setTextColor(getResources().getColor(R.color.colorAccent));
                seeMoreButton.setPadding(32, 32, 32, 32);
                
                seeMoreContainer.addView(blurView);
                seeMoreContainer.addView(seeMoreButton);
                
                instructionsContainer.addView(seeMoreContainer);
                
                // Set click listener
                seeMoreButton.setOnClickListener(v -> {
                    // Show all instructions (implementation would depend on app design)
                    Toast.makeText(RecipeDetailActivity.this, "Show all instructions", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
    
    private void updateRatingStars(float rating) {
        int filledStars = (int) Math.floor(rating);
        boolean hasHalfStar = rating - filledStars >= 0.5f;
        
        for (int i = 0; i < 5; i++) {
            if (i < filledStars) {
                ratingStars[i].setImageResource(android.R.drawable.btn_star_big_on);
            } else if (i == filledStars && hasHalfStar) {
                // This is simplified since Android doesn't have a built-in half star
                // In a real app, you'd use a custom drawable for half-star
                ratingStars[i].setImageResource(android.R.drawable.btn_star_big_on);
                ratingStars[i].setAlpha(0.5f);
            } else {
                ratingStars[i].setImageResource(android.R.drawable.btn_star_big_off);
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