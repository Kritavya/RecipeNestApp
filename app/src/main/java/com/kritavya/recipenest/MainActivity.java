package com.kritavya.recipenest;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUserName;
    private ImageView profileImage, notificationsIcon;
    private LinearLayout navHome, navSearch, navAdd, navSaved, navProfile;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        tvUserName = findViewById(R.id.tv_user_name);
        profileImage = findViewById(R.id.profile_image);
        notificationsIcon = findViewById(R.id.notifications_icon);
        
        // Set up bottom navigation
        setupBottomNavigation();
        
        // Set up profile image click
        setupProfileInteractions();
        
        // Update UI with user information
        updateUIWithUserInfo();
    }
    
    private void updateUIWithUserInfo() {
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If not signed in, redirect to login
            redirectToLogin();
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
            Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
        });
        
        navSearch.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Search", Toast.LENGTH_SHORT).show();
            // Implement search functionality
        });
        
        navAdd.setOnClickListener(v -> {
            // Launch Create Recipe Activity
            Intent intent = new Intent(MainActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
        });
        
        navSaved.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Saved Recipes", Toast.LENGTH_SHORT).show();
            // Implement saved recipes functionality
        });
        
        navProfile.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            // Navigate to profile or account settings
        });
    }
    
    private void signOut() {
        mAuth.signOut();
        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            updateUIWithUserInfo();
        }
    }
}
