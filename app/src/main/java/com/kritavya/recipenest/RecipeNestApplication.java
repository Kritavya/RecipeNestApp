package com.kritavya.recipenest;

import android.util.Log;

import com.kritavya.recipenest.utils.CloudinaryHelper;

/**
 * Custom Application class for initializing app-wide resources
 */
public class RecipeNestApplication extends android.app.Application {
    private static final String TAG = "RecipeNestApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Cloudinary
        Log.d(TAG, "Initializing Cloudinary on app startup");
        CloudinaryHelper.initCloudinary(this);
    }
} 