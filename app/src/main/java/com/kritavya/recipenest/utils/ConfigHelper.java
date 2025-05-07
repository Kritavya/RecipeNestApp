package com.kritavya.recipenest.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class to read configuration from config.properties file
 */
public class ConfigHelper {
    private static final String TAG = "ConfigHelper";
    private static Properties properties;

    /**
     * Initialize the properties from config.properties file
     * @param context Application context
     */
    public static void init(Context context) {
        if (properties != null) {
            return;
        }

        properties = new Properties();
        InputStream inputStream = null;
        
        try {
            inputStream = context.getAssets().open("config.properties");
            properties.load(inputStream);
            Log.d(TAG, "Successfully loaded config.properties file");
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file: " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream: " + e.getMessage());
                }
            }
        }
        
        // Log key counts for debug
        if (properties != null) {
            Log.d(TAG, "Loaded " + properties.size() + " properties from config file");
        }
    }

    /**
     * Get a configuration value by key
     * @param key Configuration key
     * @param defaultValue Default value if key is not found
     * @return Configuration value
     */
    public static String getConfigValue(String key, String defaultValue) {
        if (properties == null) {
            Log.e(TAG, "Properties not initialized. Call init() first.");
            return defaultValue;
        }
        
        String value = properties.getProperty(key);
        if (value == null) {
            Log.w(TAG, "Configuration key not found: " + key + ", using default value");
            return defaultValue;
        }
        
        return value;
    }
}