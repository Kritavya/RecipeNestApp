package com.kritavya.recipenest.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kritavya.recipenest.models.Recipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to load recipe data from local JSON file
 */
public class RecipeDataLoader {
    private static final String TAG = "RecipeDataLoader";
    private static List<Recipe> cachedRecipes;

    /**
     * Load recipes from the local JSON file
     * @param context Application context
     * @return List of Recipe objects
     */
    public static List<Recipe> loadRecipes(Context context) {
        if (cachedRecipes != null) {
            return cachedRecipes;
        }

        List<Recipe> recipes = new ArrayList<>();
        
        try {
            String jsonString = loadJSONFromAsset(context, "recipes.json");
            if (jsonString != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Recipe>>(){}.getType();
                recipes = gson.fromJson(jsonString, listType);
                
                Log.d(TAG, "Loaded " + recipes.size() + " recipes from local storage");
                cachedRecipes = recipes;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recipes: " + e.getMessage());
        }
        
        return recipes;
    }
    
    /**
     * Get a single recipe by ID
     * @param context Application context
     * @param recipeId Recipe ID to find
     * @return Recipe object or null if not found
     */
    public static Recipe getRecipeById(Context context, String recipeId) {
        List<Recipe> recipes = loadRecipes(context);
        for (Recipe recipe : recipes) {
            if (recipe.getId().equals(recipeId)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Search for recipes by keyword in name, description or categories
     * @param context Application context
     * @param query Search query
     * @return List of matching Recipe objects
     */
    public static List<Recipe> searchRecipes(Context context, String query) {
        if (query == null || query.trim().isEmpty()) {
            return loadRecipes(context);
        }
        
        query = query.toLowerCase().trim();
        List<Recipe> recipes = loadRecipes(context);
        List<Recipe> results = new ArrayList<>();
        
        for (Recipe recipe : recipes) {
            // Search in recipe name
            if (recipe.getName() != null && recipe.getName().toLowerCase().contains(query)) {
                results.add(recipe);
                continue;
            }
            
            // Search in description
            if (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(query)) {
                results.add(recipe);
                continue;
            }
            
            // Search in categories
            if (recipe.getCategories() != null) {
                for (String category : recipe.getCategories()) {
                    if (category.toLowerCase().contains(query)) {
                        results.add(recipe);
                        break;
                    }
                }
                continue;
            }
            
            // Search in ingredients
            if (recipe.getIngredients() != null) {
                boolean found = false;
                for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                    if (ingredient.getName().toLowerCase().contains(query)) {
                        results.add(recipe);
                        found = true;
                        break;
                    }
                }
                if (found) continue;
            }
            
            // Search in cuisine type
            if (recipe.getCuisineType() != null && recipe.getCuisineType().toLowerCase().contains(query)) {
                results.add(recipe);
            }
        }
        
        return results;
    }
    
    /**
     * Filter recipes by cuisine type
     */
    public static List<Recipe> filterByCuisine(Context context, String cuisineType) {
        if (cuisineType == null || cuisineType.isEmpty()) {
            return loadRecipes(context);
        }
        
        List<Recipe> recipes = loadRecipes(context);
        List<Recipe> filtered = new ArrayList<>();
        
        for (Recipe recipe : recipes) {
            if (recipe.getCuisineType() != null && recipe.getCuisineType().equalsIgnoreCase(cuisineType)) {
                filtered.add(recipe);
            }
        }
        
        return filtered;
    }

    /**
     * Helper method to load JSON from assets folder
     */
    private static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.e(TAG, "Error reading JSON file: " + ex.getMessage());
            return null;
        }
        return json;
    }
} 