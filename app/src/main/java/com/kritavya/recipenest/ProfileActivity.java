package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView tvUserName, tvUserEmail, tvRecipeCount, tvFollowersCount, tvFollowingCount;
    private Button btnEditProfile;
    private LinearLayout menuMyRecipes, menuSavedRecipes, menuShoppingList, menuLogout;
    private LinearLayout navHome, navSearch, navAdd, navSaved, navProfile;
    private ImageButton btnSettings;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize views
        initViews();
        
        // Load user data
        loadUserData();
        
        // Setup listeners
        setupClickListeners();
        setupBottomNavigation();
    }
    
    private void initViews() {
        // Profile views
        profileImage = findViewById(R.id.profileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        
        // Menu items
        menuMyRecipes = findViewById(R.id.menuMyRecipes);
        menuSavedRecipes = findViewById(R.id.menuSavedRecipes);
        menuShoppingList = findViewById(R.id.menuShoppingList);
        menuLogout = findViewById(R.id.menuLogout);
        
        // Bottom navigation
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navSaved = findViewById(R.id.nav_saved);
        navProfile = findViewById(R.id.nav_profile);
        
        // Settings button
        btnSettings = findViewById(R.id.btnSettings);
    }
    
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set basic user info from FirebaseUser
            tvUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            tvUserEmail.setText(user.getEmail());
            
            // Load profile image
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(profileImage);
            }
            
            // Load additional user data from Realtime Database
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get recipe count
                        DataSnapshot recipesSnapshot = dataSnapshot.child("recipes");
                        tvRecipeCount.setText(String.valueOf(recipesSnapshot.getChildrenCount()));
                        
                        // Get followers and following count
                        DataSnapshot followersSnapshot = dataSnapshot.child("followers");
                        DataSnapshot followingSnapshot = dataSnapshot.child("following");
                        
                        tvFollowersCount.setText(String.valueOf(followersSnapshot.getChildrenCount()));
                        tvFollowingCount.setText(String.valueOf(followingSnapshot.getChildrenCount()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // User is not logged in, show demo data
            tvUserName.setText("Guest User");
            tvUserEmail.setText("guest@example.com");
            
            // Show demo stats
            tvRecipeCount.setText("0");
            tvFollowersCount.setText("0");
            tvFollowingCount.setText("0");
            
            Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupClickListeners() {
        // Edit profile button
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
            // Implement profile editing functionality
        });
        
        // Settings button
        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            // Launch settings screen
        });
        
        // Menu items
        menuMyRecipes.setOnClickListener(v -> {
            startActivity(new Intent(this, MyRecipesActivity.class));
        });
        
        menuSavedRecipes.setOnClickListener(v -> {
            startActivity(new Intent(this, MyRecipesActivity.class));
        });
        
        menuShoppingList.setOnClickListener(v -> {
            Toast.makeText(this, "Shopping List clicked", Toast.LENGTH_SHORT).show();
            // Launch shopping list screen
        });
        
        menuLogout.setOnClickListener(v -> {
            // Perform logout
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Reload the activity to show guest state
            recreate();
        });
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
            startActivity(new Intent(this, CreateRecipeActivity.class));
        });
        
        navSaved.setOnClickListener(v -> {
            startActivity(new Intent(this, MyRecipesActivity.class));
        });
        
        navProfile.setOnClickListener(v -> {
            // Already on Profile
        });
    }
} 