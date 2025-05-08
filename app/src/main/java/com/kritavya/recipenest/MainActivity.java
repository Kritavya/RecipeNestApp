package com.kritavya.recipenest;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kritavya.recipenest.adapters.RecipeAdapter;
import com.kritavya.recipenest.models.Recipe;
import com.kritavya.recipenest.utils.RecipeDataLoader;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    
    private TextView tvUserName;
    private ImageView profileImage, notificationsIcon;
    private LinearLayout navHome, navSearch, navAdd, navSaved, navProfile;
    private RecyclerView recyclerViewRecipes, recyclerViewRecentRecipes;
    private RecipeAdapter recipeAdapter, recentRecipesAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> recentRecipesList;
    private EditText searchInput;
    private ImageView btnClearSearch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize views
        tvUserName = findViewById(R.id.tv_user_name);
        profileImage = findViewById(R.id.profile_image);
        notificationsIcon = findViewById(R.id.notifications_icon);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        recyclerViewRecentRecipes = findViewById(R.id.recyclerViewRecentRecipes);
        searchInput = findViewById(R.id.searchInput);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        
        // Initialize recipe lists
        recipeList = new ArrayList<>();
        recentRecipesList = new ArrayList<>();
        
        // Set up recycler views
        setupRecyclerViews();
        
        // Load recipes 
        loadLocalRecipes();
        loadRecentRecipes();
        loadDailyRecipe();
        
        // Set up bottom navigation
        setupBottomNavigation();
        
        // Set up profile image click
        setupProfileInteractions();
        
        // Set up search functionality
        setupSearch();
        
        // Update UI with user information
        updateUIWithUserInfo();
    }
    
    private void loadLocalRecipes() {
        recipeList = RecipeDataLoader.loadRecipes(this);
        recipeAdapter.updateRecipes(recipeList);
    }
    
    private void loadRecentRecipes() {
        // Get the most recent 10 recipes from Firebase
        Query recentRecipesQuery = mDatabase.child("recipes")
                .orderByChild("createdAt")
                .limitToLast(10);
        
        recentRecipesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recentRecipesList.clear();
                
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.isPublic()) {
                        recentRecipesList.add(0, recipe); // Add at the beginning for reverse chronological order
                    }
                }
                
                // Update adapter and visibility
                recentRecipesAdapter.updateRecipes(recentRecipesList);
                
                // Show or hide "Recently Uploaded" section based on whether we have recent recipes
                View recentSection = findViewById(R.id.recentRecipesSection);
                if (recentSection != null) {
                    recentSection.setVisibility(recentRecipesList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load recent recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadDailyRecipe() {
        // Get daily recipe views
        ImageView featureRecipeImage = findViewById(R.id.feature_recipe_image);
        TextView dailyRecipeName = findViewById(R.id.daily_recipe_name);
        TextView dailyRecipeRating = findViewById(R.id.daily_recipe_rating);
        TextView dailyRecipeCuisine = findViewById(R.id.daily_recipe_cuisine);
        
        // Get a random recipe from the loaded recipes
        if (recipeList != null && !recipeList.isEmpty()) {
            int randomIndex = (int) (Math.random() * recipeList.size());
            Recipe dailyRecipe = recipeList.get(randomIndex);
            
            // Update UI with the daily recipe
            if (dailyRecipe != null) {
                // Set recipe name and rating
                dailyRecipeName.setText(dailyRecipe.getName());
                dailyRecipeRating.setText(String.format("%.1f", dailyRecipe.getRating()));
                
                // Set cuisine type if available
                if (dailyRecipe.getCuisineType() != null && !dailyRecipe.getCuisineType().isEmpty()) {
                    dailyRecipeCuisine.setText(dailyRecipe.getCuisineType());
                    dailyRecipeCuisine.setVisibility(View.VISIBLE);
                } else {
                    dailyRecipeCuisine.setVisibility(View.GONE);
                }
                
                // Load recipe image
                String imageUrl = null;
                if (dailyRecipe.getImageUrls() != null && !dailyRecipe.getImageUrls().isEmpty()) {
                    imageUrl = dailyRecipe.getImageUrls().get(0);
                } else if (dailyRecipe.getImageUrl() != null && !dailyRecipe.getImageUrl().isEmpty()) {
                    imageUrl = dailyRecipe.getImageUrl();
                }
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_food)
                        .into(featureRecipeImage);
                }
                
                // Make the card clickable to view recipe details
                View dailyRecipeCard = findViewById(R.id.daily_recipe_card);
                if (dailyRecipeCard != null) {
                    dailyRecipeCard.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
                        intent.putExtra("recipe_id", dailyRecipe.getId());
                        intent.putExtra("recipe_source", "local");
                        startActivity(intent);
                    });
                }
            }
        }
    }
    
    private void setupRecyclerViews() {
        // Set up main recipes grid
        recyclerViewRecipes.setLayoutManager(new GridLayoutManager(this, 2));
        recipeAdapter = new RecipeAdapter(this, recipeList);
        recyclerViewRecipes.setAdapter(recipeAdapter);
        
        // Set item click listener for local recipes
        recipeAdapter.setOnItemClickListener(recipe -> {
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            intent.putExtra("recipe_source", "local"); // Flag to indicate this is a local recipe
            startActivity(intent);
        });
        
        // Set up recent recipes horizontal list
        if (recyclerViewRecentRecipes != null) {
            recyclerViewRecentRecipes.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recentRecipesAdapter = new RecipeAdapter(this, recentRecipesList, true);
            recyclerViewRecentRecipes.setAdapter(recentRecipesAdapter);
            
            // Set item click listener for recent recipes
            recentRecipesAdapter.setOnItemClickListener(recipe -> {
                Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipe.getId());
                intent.putExtra("recipe_source", "firebase"); // Flag to indicate this is from Firebase
                startActivity(intent);
            });
        }
    }
    
    private void setupSearch() {
        // Check if search views exist (they might have been removed from the layout)
        ImageView btnSearch = findViewById(R.id.btnSearch);
        if (searchInput == null || btnClearSearch == null || btnSearch == null) {
            // Search functionality has been moved to SearchActivity, so we don't need to set it up here
            return;
        }
    
        // Set up search button click
        btnSearch.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            performSearch(query);
        });
        
        // Set up clear search button
        btnClearSearch.setOnClickListener(v -> {
            searchInput.setText("");
            btnClearSearch.setVisibility(View.GONE);
            resetSearch();
        });
        
        // Listen for text changes to show/hide clear button
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    private void performSearch(String query) {
        if (!query.isEmpty()) {
            // Search in local recipes
            List<Recipe> searchResults = RecipeDataLoader.searchRecipes(this, query);
            recipeAdapter.updateRecipes(searchResults);
            
            // Hide recent recipes section during search
            View recentSection = findViewById(R.id.recentRecipesSection);
            if (recentSection != null) {
                recentSection.setVisibility(View.GONE);
            }
            
            if (searchResults.isEmpty()) {
                Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show();
            }
        } else {
            resetSearch();
        }
    }
    
    private void resetSearch() {
        // Reset to all recipes if search is empty
        recipeAdapter.updateRecipes(recipeList);
        
        // Show recent recipes section again
        View recentSection = findViewById(R.id.recentRecipesSection);
        if (recentSection != null) {
            recentSection.setVisibility(recentRecipesList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }
    
    private void updateUIWithUserInfo() {
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User shouldn't be here if not signed in, but just in case
            return;
        }
        
        // First set a default greeting while we fetch user data
        tvUserName.setText("Hello, User");
        
        // Try to get the user's name from Firestore
        db.collection("users").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String userName = "User";
                
                if (documentSnapshot.exists() && documentSnapshot.contains("fullName")) {
                    // Get full name from Firestore
                    String fullName = documentSnapshot.getString("fullName");
                    
                    // Extract first name (everything before the first space)
                    if (fullName != null && !fullName.isEmpty()) {
                        int spaceIndex = fullName.indexOf(' ');
                        if (spaceIndex > 0) {
                            userName = fullName.substring(0, spaceIndex);
                        } else {
                            userName = fullName; // No space found, use full name
                        }
                        
                        // Update Firebase Auth display name if it's not set
                        if (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()) {
                            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build());
                        }
                    }
                } else if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    // Use display name if available as fallback
                    userName = currentUser.getDisplayName();
                } else if (currentUser.getEmail() != null) {
                    // Otherwise use email but remove the domain part as fallback
                    String email = currentUser.getEmail();
                    int atIndex = email.indexOf('@');
                    if (atIndex > 0) {
                        userName = email.substring(0, atIndex);
                        // Capitalize first letter
                        userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                        
                        // Update Firebase Auth display name if it's not set
                        currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .build());
                    } else {
                        userName = email;
                    }
                }
                
                // Update UI with the user's name
                tvUserName.setText("Hello, " + userName);
            })
            .addOnFailureListener(e -> {
                // Fallback to basic name extraction if Firestore fails
                String userName = "User";
                
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    userName = currentUser.getDisplayName();
                } else if (currentUser.getEmail() != null) {
                    String email = currentUser.getEmail();
                    int atIndex = email.indexOf('@');
                    if (atIndex > 0) {
                        userName = email.substring(0, atIndex);
                        userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                        
                        // Update Firebase Auth display name if it's not set
                        currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .build());
                    } else {
                        userName = email;
                    }
                }
                
                tvUserName.setText("Hello, " + userName);
            });
    }
    
    private void setupProfileInteractions() {
        // Profile image click for logout
        profileImage.setOnClickListener(v -> {
            showLogoutDialog();
        });
        
        // Notifications click
        notificationsIcon.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Notifications", Toast.LENGTH_SHORT).show();
            // Implement notifications functionality
        });
    }
    
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", (dialog, which) -> {
            signOut();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }
    
    private void setupBottomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navSaved = findViewById(R.id.nav_saved);
        navProfile = findViewById(R.id.nav_profile);
        
        // Set up click listeners
        navHome.setOnClickListener(v -> {
            // Already on home screen
        });
        
        navSearch.setOnClickListener(v -> {
            // Navigate to search page
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        
        navAdd.setOnClickListener(v -> {
            // Check if user is logged in
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(MainActivity.this, "Please sign in to create recipes", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }
            
            // Launch Create Recipe Activity
            Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
        });
        
        navSaved.setOnClickListener(v -> {
            // Check if user is logged in
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(MainActivity.this, "Please sign in to view your recipes", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }
            
            // Navigate to My Recipes Activity
            Intent intent = new Intent(MainActivity.this, MyRecipesActivity.class);
            startActivity(intent);
        });
        
        navProfile.setOnClickListener(v -> {
            // Check if user is logged in
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(MainActivity.this, "Please sign in to view your profile", Toast.LENGTH_SHORT).show();
                redirectToLogin();
                return;
            }
            
            // Navigate to Profile Activity
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
    
    private void signOut() {
        mAuth.signOut();
        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // User must be signed in if they're in this activity,
        // as SplashScreenActivity handles the authentication check
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUIWithUserInfo();
        }
    }
}
