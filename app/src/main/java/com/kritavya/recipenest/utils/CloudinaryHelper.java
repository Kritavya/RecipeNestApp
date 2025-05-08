package com.kritavya.recipenest.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
     * Delete an image from Cloudinary by its URL
     * 
     * This method uses a direct HTTP request to Cloudinary's REST API to delete an image
     * 
     * @param context Application context
     * @param imageUrl The URL of the image to delete
     * @param callback Interface to handle delete results
     */
    public static void deleteImage(Context context, String imageUrl, CloudinaryDeleteCallback callback) {
        if (!isInitialized) {
            initCloudinary(context);
            
            if (!isInitialized) {
                if (callback != null) {
                    callback.onError("Failed to initialize Cloudinary. Check credentials.");
                }
                return;
            }
        }
        
        // Extract the public_id from the URL
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId == null) {
            if (callback != null) {
                callback.onError("Invalid Cloudinary URL format");
            }
            return;
        }
        
        // Log the deletion attempt
        Log.d(TAG, "Attempting to delete Cloudinary image with public ID: " + publicId);
        
        // Get credentials
        String cloudName = ConfigHelper.getConfigValue("cloudinary.cloud_name", "").trim();
        String apiKey = ConfigHelper.getConfigValue("cloudinary.api_key", "").trim();
        String apiSecret = ConfigHelper.getConfigValue("cloudinary.api_secret", "").trim();
        
        // Execute the deletion in background thread
        new DeleteCloudinaryImageTask(cloudName, apiKey, apiSecret, publicId, callback).execute();
    }
    
    /**
     * AsyncTask to delete a Cloudinary image using the REST API
     */
    private static class DeleteCloudinaryImageTask extends AsyncTask<Void, Void, Boolean> {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
        private String publicId;
        private CloudinaryDeleteCallback callback;
        private String errorMessage;
        
        public DeleteCloudinaryImageTask(String cloudName, String apiKey, String apiSecret, 
                                        String publicId, CloudinaryDeleteCallback callback) {
            this.cloudName = cloudName;
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.publicId = publicId;
            this.callback = callback;
        }
        
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Generate timestamp and signature
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                
                // Create the string to sign
                String toSign = "public_id=" + publicId + "&timestamp=" + timestamp + apiSecret;
                
                // Generate signature
                String signature = generateSHA1(toSign);
                
                // Create API URL
                String apiUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
                
                // Create connection
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                // Create JSON request body
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("public_id", publicId);
                jsonParam.put("api_key", apiKey);
                jsonParam.put("timestamp", timestamp);
                jsonParam.put("signature", signature);
                
                // Send request
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // Get response
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }
                    
                    // Check response for success
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.optString("result", "").equals("ok");
                } else {
                    // Error response
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                    }
                    errorMessage = "API Error: " + responseCode + " " + response.toString();
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting Cloudinary image: " + e.getMessage(), e);
                errorMessage = "Error: " + e.getMessage();
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                if (success) {
                    Log.d(TAG, "Cloudinary image deleted successfully: " + publicId);
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Failed to delete Cloudinary image: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }
        }
    }
    
    /**
     * Generate SHA1 hash
     */
    private static String generateSHA1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    
    /**
     * Extract the public_id from a Cloudinary URL
     * @param url The Cloudinary URL
     * @return The public_id of the image
     */
    private static String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // Expected format: https://res.cloudinary.com/cloud_name/image/upload/v123456789/folder/image_name.jpg
            // We need to extract: folder/image_name
            
            // Split URL by domain first
            String[] parts = url.split("cloudinary\\.com/");
            if (parts.length < 2) {
                return null;
            }
            
            // Get the path after the domain
            String path = parts[1];
            
            // Split by /upload/
            String[] uploadParts = path.split("/upload/");
            if (uploadParts.length < 2) {
                return null;
            }
            
            // Get the path after /upload/
            String uploadPath = uploadParts[1];
            
            // Remove the version number if present (v123456789/)
            if (uploadPath.startsWith("v")) {
                int versionEnd = uploadPath.indexOf('/');
                if (versionEnd > 0) {
                    uploadPath = uploadPath.substring(versionEnd + 1);
                }
            }
            
            // Remove file extension if present
            int extensionStart = uploadPath.lastIndexOf('.');
            if (extensionStart > 0) {
                uploadPath = uploadPath.substring(0, extensionStart);
            }
            
            return uploadPath;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting public ID from URL: " + e.getMessage());
            return null;
        }
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
    
    /**
     * Interface to handle Cloudinary delete callbacks
     */
    public interface CloudinaryDeleteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
} 