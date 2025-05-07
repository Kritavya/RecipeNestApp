package com.kritavya.recipenest.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date formatting and manipulation
 */
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    
    /**
     * Formats a timestamp (in milliseconds) to a readable date string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string (e.g., "21 May 2024")
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    /**
     * Formats a timestamp to a readable relative time (e.g., "2 hours ago", "Yesterday")
     * @param timestamp Timestamp in milliseconds
     * @return Relative time string
     */
    public static String getRelativeTimeSpan(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        // Convert to seconds
        long seconds = diff / 1000;
        
        if (seconds < 60) {
            return "Just now";
        }
        
        // Convert to minutes
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }
        
        // Convert to hours
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }
        
        // Convert to days
        long days = hours / 24;
        if (days < 7) {
            if (days == 1) {
                return "Yesterday";
            }
            return days + " days ago";
        }
        
        // Convert to weeks
        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        }
        
        // If older than 4 weeks, just return the date
        return formatDate(timestamp);
    }
} 