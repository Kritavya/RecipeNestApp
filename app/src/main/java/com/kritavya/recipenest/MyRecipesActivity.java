package com.kritavya.recipenest;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kritavya.recipenest.adapters.RecipeAdapter;
import com.kritavya.recipenest.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class MyRecipesActivity extends AppCompatActivity {

    private static final String TAG = "MyRecipesActivity";

    private TextView tabUploaded, tabSaved;
    private View indicatorUploaded, indicatorSaved;
    private LinearLayout navHome, navSearch, navAdd, navSaved, navProfile;
    private RecyclerView recipeRecyclerView;
    private ProgressBar loadingProgress;
    private View emptyView;
    private Button btnCreateRecipe;
    private ImageButton btnSort;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ValueEventListener uploadedRecipesListener;
    private ValueEventListener savedRecipesListener;

    // Data
    private List<Recipe> uploadedRecipes = new ArrayList<>();
    private List<Recipe> savedRecipes = new ArrayList<>();
    private RecipeAdapter recipeAdapter;
    private boolean isShowingUploaded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Initialize views
        initViews();
        
        // Set up listeners - first the adapter, then the tab listeners
        setupRecipeAdapter();
        setupTabListeners();
        setupBottomNavigation();
        
        // Check user authentication
        checkUserAuthentication();
    }
    
    private void initViews() {
        // Initialize tab views
        tabUploaded = findViewById(R.id.tabUploaded);
        tabSaved = findViewById(R.id.tabSaved);
        indicatorUploaded = findViewById(R.id.indicatorUploaded);
        indicatorSaved = findViewById(R.id.indicatorSaved);
        
        // Initialize bottom navigation
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navSaved = findViewById(R.id.nav_saved);
        navProfile = findViewById(R.id.nav_profile);
        
        // Initialize recipe views
        recipeRecyclerView = findViewById(R.id.recipeRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyView = findViewById(R.id.emptyView);
        btnCreateRecipe = findViewById(R.id.btnCreateRecipe);
        
        // Sort button
        btnSort = findViewById(R.id.btnSort);
        
        // Set up create recipe button
        btnCreateRecipe.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateRecipeActivity.class));
        });
    }
    
    private void setupTabListeners() {
        tabUploaded.setOnClickListener(v -> switchTab(true));
        tabSaved.setOnClickListener(v -> switchTab(false));
        
        // Set initial UI state
        tabUploaded.setTextColor(getResources().getColor(R.color.colorAccent));
        tabSaved.setTextColor(getResources().getColor(R.color.colorLightText));
        indicatorUploaded.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        indicatorSaved.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        
        // Set up sort button
        btnSort.setOnClickListener(v -> {
            Toast.makeText(this, "Sort options will be implemented", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void switchTab(boolean isUploaded) {
        isShowingUploaded = isUploaded;
        
        // Update tab colors
        tabUploaded.setTextColor(getResources().getColor(isUploaded ? 
                R.color.colorAccent : R.color.colorLightText));
        tabSaved.setTextColor(getResources().getColor(isUploaded ? 
                R.color.colorLightText : R.color.colorAccent));
        
        // Update indicators
        indicatorUploaded.setBackgroundColor(getResources().getColor(isUploaded ? 
                R.color.colorAccent : android.R.color.transparent));
        indicatorSaved.setBackgroundColor(getResources().getColor(isUploaded ? 
                android.R.color.transparent : R.color.colorAccent));
        
        // Update displayed recipes - only if adapter is initialized
        if (recipeAdapter != null) {
            recipeAdapter.setIsUploadedRecipes(isUploaded);
            if (isUploaded) {
                recipeAdapter.updateRecipes(uploadedRecipes);
                updateEmptyState(uploadedRecipes.isEmpty(), "You haven't uploaded any recipes yet.");
            } else {
                recipeAdapter.updateRecipes(savedRecipes);
                updateEmptyState(savedRecipes.isEmpty(), "You haven't saved any recipes yet.");
            }
        }
    }
    
    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        
        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
        
        navAdd.setOnClickListener(v -> {
            // Check if user is logged in before adding recipe
            if (mAuth.getCurrentUser() == null) {
                // User is not logged in, redirect to onboarding
                Toast.makeText(this, "Please sign in to create recipes", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
                return;
            }
            startActivity(new Intent(this, CreateRecipeActivity.class));
        });
        
        navSaved.setOnClickListener(v -> {
            // Already on My Recipes
        });
        
        navProfile.setOnClickListener(v -> {
            // Check if user is logged in before showing profile
            if (mAuth.getCurrentUser() == null) {
                // User is not logged in, redirect to onboarding
                Toast.makeText(this, "Please sign in to view your profile", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
                return;
            }
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
    
    private void setupRecipeAdapter() {
        recipeAdapter = new RecipeAdapter(this, isShowingUploaded ? uploadedRecipes : savedRecipes);
        recipeAdapter.setIsUploadedRecipes(isShowingUploaded);
        
        // Set up item click listener (open recipe details)
        recipeAdapter.setOnItemClickListener(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });
        
        // Set up delete click listener
        recipeAdapter.setOnDeleteClickListener((recipe, position) -> {
            showDeleteConfirmationDialog(recipe, position);
        });
        
        // Set up RecyclerView
        recipeRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recipeRecyclerView.setAdapter(recipeAdapter);
    }
    
    private void showDeleteConfirmationDialog(Recipe recipe, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Recipe");
        builder.setMessage("Are you sure you want to delete \"" + recipe.getName() + "\"?");
        
        // Add delete button
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteRecipe(recipe, position);
        });
        
        // Add cancel button
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void deleteRecipe(Recipe recipe, int position) {
        if (recipe == null || recipe.getId() == null) {
            Toast.makeText(this, "Cannot delete recipe: Invalid recipe data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Delete recipe from database
        mDatabase.child("recipes").child(recipe.getId()).removeValue()
            .addOnSuccessListener(aVoid -> {
                // Remove from user's recipes as well
                if (mAuth.getCurrentUser() != null) {
                    mDatabase.child("users").child(mAuth.getCurrentUser().getUid())
                            .child("recipes").child(recipe.getId()).removeValue();
                    
                    // Remove references from other users who have saved this recipe
                    mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                mDatabase.child("users").child(userSnapshot.getKey())
                                        .child("savedRecipes").child(recipe.getId()).removeValue();
                            }
                            
                            // Delete images from storage if any
                            deleteRecipeImages(recipe);
                            
                            // Update UI
                            recipeAdapter.removeRecipe(position);
                            showLoading(false);
                            
                            // Show success message
                            Snackbar.make(recipeRecyclerView, "Recipe deleted successfully", Snackbar.LENGTH_SHORT).show();
                            
                            // Check for empty state
                            if (isShowingUploaded && uploadedRecipes.size() == 0) {
                                updateEmptyState(true, "You haven't uploaded any recipes yet.");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error removing saved references: " + databaseError.getMessage());
                            showLoading(false);
                        }
                    });
                } else {
                    // Delete images from storage if any
                    deleteRecipeImages(recipe);
                    
                    // Update UI
                    recipeAdapter.removeRecipe(position);
                    showLoading(false);
                    
                    // Show success message
                    Snackbar.make(recipeRecyclerView, "Recipe deleted successfully", Snackbar.LENGTH_SHORT).show();
                    
                    // Check for empty state
                    if (isShowingUploaded && uploadedRecipes.size() == 0) {
                        updateEmptyState(true, "You haven't uploaded any recipes yet.");
                    }
                }
            })
            .addOnFailureListener(e -> {
                // Show error message
                showLoading(false);
                Toast.makeText(this, "Failed to delete recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error deleting recipe: " + e.getMessage());
            });
    }
    
    private void deleteRecipeImages(Recipe recipe) {
        // Delete recipe images from Storage if any
        if (recipe.getImageUrls() != null && !recipe.getImageUrls().isEmpty()) {
            for (String imageUrl : recipe.getImageUrls()) {
                if (imageUrl != null && imageUrl.contains("firebase")) {
                    try {
                        // Extract the image path from the URL
                        String imagePath = imageUrl.substring(imageUrl.indexOf("/o/") + 3, imageUrl.indexOf("?"));
                        imagePath = imagePath.replace("%2F", "/");
                        
                        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                        imageRef.delete().addOnFailureListener(e -> {
                            Log.e(TAG, "Error deleting image: " + e.getMessage());
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing image URL: " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void checkUserAuthentication() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // User is not logged in, show a message and redirect to login
            Toast.makeText(this, "Please log in to view your recipes", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            // User is logged in, load their recipes
            loadRecipes(user.getUid());
        }
    }
    
    private void loadRecipes(String userId) {
        showLoading(true);
        
        // Load uploaded recipes
        loadUploadedRecipes(userId);
        
        // Load saved recipes
        loadSavedRecipes(userId);
    }
    
    private void loadUploadedRecipes(String userId) {
        if (uploadedRecipesListener != null) {
            mDatabase.child("recipes").removeEventListener(uploadedRecipesListener);
        }
        
        uploadedRecipesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uploadedRecipes.clear();
                
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.getUserId().equals(userId)) {
                        uploadedRecipes.add(recipe);
                    }
                }
                
                // If currently showing uploaded recipes, update the adapter
                if (isShowingUploaded && recipeAdapter != null) {
                    recipeAdapter.updateRecipes(uploadedRecipes);
                    updateEmptyState(uploadedRecipes.isEmpty(), "You haven't uploaded any recipes yet.");
                }
                
                showLoading(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading uploaded recipes: " + databaseError.getMessage());
                Toast.makeText(MyRecipesActivity.this, "Error loading recipes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        };
        
        mDatabase.child("recipes").orderByChild("userId").equalTo(userId).addValueEventListener(uploadedRecipesListener);
    }
    
    private void loadSavedRecipes(String userId) {
        if (savedRecipesListener != null) {
            mDatabase.child("users").child(userId).child("savedRecipes").removeEventListener(savedRecipesListener);
        }
        
        savedRecipesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    if (!isShowingUploaded && recipeAdapter != null) {
                        updateEmptyState(true, "You haven't saved any recipes yet.");
                    }
                    return;
                }
                
                List<String> savedRecipeIds = new ArrayList<>();
                for (DataSnapshot idSnapshot : dataSnapshot.getChildren()) {
                    if (idSnapshot.getValue(Boolean.class) != null && idSnapshot.getValue(Boolean.class)) {
                        savedRecipeIds.add(idSnapshot.getKey());
                    }
                }
                
                if (savedRecipeIds.isEmpty()) {
                    savedRecipes.clear();
                    if (!isShowingUploaded && recipeAdapter != null) {
                        recipeAdapter.updateRecipes(savedRecipes);
                        updateEmptyState(true, "You haven't saved any recipes yet.");
                    }
                    return;
                }
                
                // Fetch the actual recipe data for each saved ID
                fetchSavedRecipesData(savedRecipeIds);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading saved recipes: " + databaseError.getMessage());
                Toast.makeText(MyRecipesActivity.this, "Error loading saved recipes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                if (!isShowingUploaded) {
                    showLoading(false);
                }
            }
        };
        
        mDatabase.child("users").child(userId).child("savedRecipes").addValueEventListener(savedRecipesListener);
    }
    
    private void fetchSavedRecipesData(List<String> recipeIds) {
        savedRecipes.clear();
        final int[] completedQueries = {0};
        final int totalQueries = recipeIds.size();
        
        for (String recipeId : recipeIds) {
            mDatabase.child("recipes").child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        savedRecipes.add(recipe);
                    }
                    
                    completedQueries[0]++;
                    if (completedQueries[0] >= totalQueries) {
                        // All queries completed
                        if (!isShowingUploaded && recipeAdapter != null) {
                            recipeAdapter.updateRecipes(savedRecipes);
                            updateEmptyState(savedRecipes.isEmpty(), "You haven't saved any recipes yet.");
                        }
                        showLoading(false);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error fetching saved recipe details: " + databaseError.getMessage());
                    completedQueries[0]++;
                    if (completedQueries[0] >= totalQueries) {
                        // All queries completed
                        if (!isShowingUploaded && recipeAdapter != null) {
                            recipeAdapter.updateRecipes(savedRecipes);
                            updateEmptyState(savedRecipes.isEmpty(), "You haven't saved any recipes yet.");
                        }
                        showLoading(false);
                    }
                }
            });
        }
    }
    
    private void showLoading(boolean isLoading) {
        if (loadingProgress == null || recipeRecyclerView == null || emptyView == null) return;
        
        loadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recipeRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        
        // Hide empty state during loading
        if (isLoading) {
            emptyView.setVisibility(View.GONE);
        }
    }
    
    private void updateEmptyState(boolean isEmpty, String message) {
        if (emptyView == null || recipeRecyclerView == null) return;
        
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recipeRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        
        // Update empty state message
        TextView emptyMessage = emptyView.findViewById(R.id.emptyStateMessage);
        if (emptyMessage != null) {
            emptyMessage.setText(message);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Remove Firebase listeners
        if (uploadedRecipesListener != null) {
            mDatabase.child("recipes").removeEventListener(uploadedRecipesListener);
        }
        
        if (savedRecipesListener != null && mAuth.getCurrentUser() != null) {
            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("savedRecipes").removeEventListener(savedRecipesListener);
        }
    }
} 