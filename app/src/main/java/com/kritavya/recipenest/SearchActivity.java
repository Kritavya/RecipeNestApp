package com.kritavya.recipenest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kritavya.recipenest.adapters.RecipeAdapter;
import com.kritavya.recipenest.models.Recipe;
import com.kritavya.recipenest.utils.RecipeDataLoader;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton btnSearch, btnClearSearch, btnBack;
    private RecyclerView recyclerViewSearchResults;
    private RecipeAdapter searchAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> searchResults;
    
    // Navigation elements
    private LinearLayout navHome, navSearch, navAdd, navSaved, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        searchInput = findViewById(R.id.searchInput);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);

        // Initialize recipe lists
        recipeList = RecipeDataLoader.loadRecipes(this);
        searchResults = new ArrayList<>();

        // Setup RecyclerView
        recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        searchAdapter = new RecipeAdapter(this, searchResults);
        recyclerViewSearchResults.setAdapter(searchAdapter);

        // Set click listener for recipe items
        searchAdapter.setOnItemClickListener(recipe -> {
            Intent intent = new Intent(SearchActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            intent.putExtra("recipe_source", "local");
            startActivity(intent);
        });

        // Setup search functionality
        setupSearch();

        // Setup back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Setup bottom navigation
        setupBottomNavigation();
    }
    
    private void setupBottomNavigation() {
        // Initialize navigation items
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navSaved = findViewById(R.id.nav_saved);
        navProfile = findViewById(R.id.nav_profile);
        
        // Update colors to highlight "Search" tab
        updateNavItemColors(navSearch);
        
        // Set up click listeners for navigation
        navHome.setOnClickListener(v -> {
            finish(); // Go back to home
        });
        
        navSearch.setOnClickListener(v -> {
            // Already on search screen
        });
        
        navAdd.setOnClickListener(v -> {
            // Launch Create Recipe Activity
            Intent intent = new Intent(SearchActivity.this, CreateRecipeActivity.class);
            startActivity(intent);
        });
        
        navSaved.setOnClickListener(v -> {
            Toast.makeText(SearchActivity.this, "Saved Recipes", Toast.LENGTH_SHORT).show();
            // Implement saved recipes functionality
        });
        
        navProfile.setOnClickListener(v -> {
            Toast.makeText(SearchActivity.this, "Profile", Toast.LENGTH_SHORT).show();
            // Navigate to profile or account settings
        });
    }
    
    private void updateNavItemColors(LinearLayout activeNav) {
        // Reset all nav items to inactive color
        resetNavItem(navHome);
        resetNavItem(navSearch);
        resetNavItem(navSaved);
        resetNavItem(navProfile);
        
        // Set active item color
        if (activeNav != null) {
            ImageView icon = (ImageView) activeNav.getChildAt(0);
            TextView label = (TextView) activeNav.getChildAt(1);
            
            if (icon != null) {
                icon.setColorFilter(getResources().getColor(R.color.colorAccent));
            }
            
            if (label != null) {
                label.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        }
    }
    
    private void resetNavItem(LinearLayout navItem) {
        if (navItem != null && navItem.getChildCount() >= 2) {
            ImageView icon = (ImageView) navItem.getChildAt(0);
            TextView label = (TextView) navItem.getChildAt(1);
            
            if (icon != null) {
                icon.setColorFilter(0xFFAAAAAA); // Inactive gray color
            }
            
            if (label != null) {
                label.setTextColor(0xFFAAAAAA); // Inactive gray color
            }
        }
    }

    private void setupSearch() {
        // Set up search button click
        btnSearch.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            performSearch(query);
        });

        // Set up clear search button
        btnClearSearch.setOnClickListener(v -> {
            searchInput.setText("");
            btnClearSearch.setVisibility(View.GONE);
            // Clear search results
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
        });

        // Listen for text changes to show/hide clear button
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                // Auto search as user types
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (!query.isEmpty()) {
            // Search in local recipes
            searchResults.clear();
            searchResults.addAll(RecipeDataLoader.searchRecipes(this, query));
            searchAdapter.notifyDataSetChanged();

            if (searchResults.isEmpty()) {
                Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Clear search results if query is empty
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
        }
    }
} 