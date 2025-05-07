package com.kritavya.recipenest.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for Cloudinary operations
 */
public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    /**
     * Initialize Cloudinary with your account credentials
     * Call this method once in your Application class or SplashActivity
     */
    public static void initCloudinary(Context context) {
        if (isInitialized) {
            return;
        }

        try {
            // Initialize config helper
            ConfigHelper.init(context);
            
            // Get Cloudinary configuration from properties
            String cloudName = ConfigHelper.getConfigValue("cloudinary.cloud_name", "").trim();
            String apiKey = ConfigHelper.getConfigValue("cloudinary.api_key", "").trim();
            String apiSecret = ConfigHelper.getConfigValue("cloudinary.api_secret", "").trim();
            
            if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
                Log.e(TAG, "Cloudinary credentials are missing in config file");
                return;
            }
            
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", "true");
            
            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload an image to Cloudinary
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @param folder Cloudinary folder to upload to (e.g., "recipe_images")
     * @param callback Interface to handle upload results
     */
    public static String uploadImage(Context context, Uri imageUri, String folder, final CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            initCloudinary(context);
            
            if (!isInitialized) {
                callback.onError("Failed to initialize Cloudinary. Check credentials.");
                return null;
            }
        }

        final String requestId = MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                        callback.onUploadStarted();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes;
                        callback.onProgressUpdate(progress);
                        Log.d(TAG, "Upload progress: " + progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        String publicId = (String) resultData.get("public_id");
                        Log.d(TAG, "Upload successful: " + url);
                        callback.onSuccess(url, publicId);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();

        return requestId;
    }

    /**
     * Interface to handle Cloudinary upload callbacks
     */
    public interface CloudinaryUploadCallback {
        void onUploadStarted();
        void onProgressUpdate(double progress);
        void onSuccess(String url, String publicId);
        void onError(String errorMessage);
    }
} 