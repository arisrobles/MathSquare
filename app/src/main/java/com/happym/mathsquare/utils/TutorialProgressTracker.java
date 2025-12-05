package com.happym.mathsquare.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks tutorial completion progress
 * Implements defense comment #3: Tutorial - Path - finish 1 module before the next
 */
public class TutorialProgressTracker {
    
    private static final String PREF_NAME = "tutorial_progress";
    private static final String KEY_COMPLETED_TUTORIALS = "completed_tutorials";
    private static final String KEY_TUTORIAL_ORDER = "tutorial_order";
    
    // Define tutorial order - must complete in sequence
    private static final String[] TUTORIAL_ORDER = {
        "addition",
        "subtraction", 
        "multiplication",
        "division",
        "decimals",
        "percentage"
    };
    
    private SharedPreferences sharedPreferences;
    
    public TutorialProgressTracker(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Mark a tutorial as completed
     */
    public void markTutorialCompleted(String tutorialName) {
        Set<String> completed = getCompletedTutorials();
        completed.add(tutorialName.toLowerCase());
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_COMPLETED_TUTORIALS, completed);
        editor.apply();
    }
    
    /**
     * Check if a tutorial is completed
     */
    public boolean isTutorialCompleted(String tutorialName) {
        Set<String> completed = getCompletedTutorials();
        return completed.contains(tutorialName.toLowerCase());
    }
    
    /**
     * Check if previous tutorial is completed (for progression)
     */
    public boolean canAccessTutorial(String tutorialName) {
        String tutLower = tutorialName.toLowerCase();
        
        // Find index of current tutorial
        int currentIndex = -1;
        for (int i = 0; i < TUTORIAL_ORDER.length; i++) {
            if (TUTORIAL_ORDER[i].equals(tutLower)) {
                currentIndex = i;
                break;
            }
        }
        
        // If tutorial not in order list, allow access (for backwards compatibility)
        if (currentIndex == -1) {
            return true;
        }
        
        // First tutorial is always accessible
        if (currentIndex == 0) {
            return true;
        }
        
        // Check if previous tutorial is completed
        String previousTutorial = TUTORIAL_ORDER[currentIndex - 1];
        return isTutorialCompleted(previousTutorial);
    }
    
    /**
     * Get all completed tutorials
     */
    public Set<String> getCompletedTutorials() {
        return sharedPreferences.getStringSet(KEY_COMPLETED_TUTORIALS, new HashSet<>());
    }
    
    /**
     * Get next available tutorial
     */
    public String getNextTutorial() {
        Set<String> completed = getCompletedTutorials();
        
        for (String tutorial : TUTORIAL_ORDER) {
            if (!completed.contains(tutorial)) {
                return tutorial;
            }
        }
        
        return null; // All tutorials completed
    }
    
    /**
     * Reset all tutorial progress
     */
    public void resetProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_COMPLETED_TUTORIALS);
        editor.apply();
    }
}

