package com.happym.mathsquare.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.happym.mathsquare.sharedPreferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks tutorial completion progress with grade-specific sequences
 * Implements defense comment #3: Tutorial - Path - finish 1 module before the next
 * Each grade has its own step-by-step tutorial path
 */
public class TutorialProgressTracker {
    
    private static final String PREF_NAME = "tutorial_progress";
    private static final String KEY_COMPLETED_TUTORIALS = "completed_tutorials";
    
    // Define grade-specific tutorial orders - must complete in sequence
    // Grade 1: Addition -> Subtraction
    private static final String[] GRADE_1_ORDER = {
        "addition",
        "subtraction"
    };
    
    // Grade 2: Addition -> Subtraction (can be same or different)
    private static final String[] GRADE_2_ORDER = {
        "addition",
        "subtraction"
    };
    
    // Grade 3: Addition -> Subtraction -> Multiplication
    private static final String[] GRADE_3_ORDER = {
        "addition",
        "subtraction",
        "multiplication"
    };
    
    // Grade 4: Addition -> Subtraction -> Multiplication -> Division
    private static final String[] GRADE_4_ORDER = {
        "addition",
        "subtraction",
        "multiplication",
        "division"
    };
    
    // Grade 5: Addition -> Subtraction -> Multiplication -> Division -> Decimals
    private static final String[] GRADE_5_ORDER = {
        "addition",
        "subtraction",
        "multiplication",
        "division",
        "decimals"
    };
    
    // Grade 6: All tutorials in sequence
    private static final String[] GRADE_6_ORDER = {
        "addition",
        "subtraction",
        "multiplication",
        "division",
        "decimals",
        "percentage"
    };
    
    // Map to store grade-specific orders
    private static final Map<Integer, String[]> GRADE_TUTORIAL_ORDERS = new HashMap<>();
    
    static {
        GRADE_TUTORIAL_ORDERS.put(1, GRADE_1_ORDER);
        GRADE_TUTORIAL_ORDERS.put(2, GRADE_2_ORDER);
        GRADE_TUTORIAL_ORDERS.put(3, GRADE_3_ORDER);
        GRADE_TUTORIAL_ORDERS.put(4, GRADE_4_ORDER);
        GRADE_TUTORIAL_ORDERS.put(5, GRADE_5_ORDER);
        GRADE_TUTORIAL_ORDERS.put(6, GRADE_6_ORDER);
    }
    
    private SharedPreferences androidSharedPreferences;
    private Context context;
    
    public TutorialProgressTracker(Context context) {
        this.context = context;
        androidSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get tutorial order for a specific grade
     */
    private String[] getTutorialOrderForGrade(String grade) {
        int gradeNum = getGradeNumber(grade);
        String[] order = GRADE_TUTORIAL_ORDERS.get(gradeNum);
        return order != null ? order : GRADE_1_ORDER; // Default to grade 1 order
    }
    
    /**
     * Get grade number from grade string
     */
    private int getGradeNumber(String grade) {
        try {
            if (grade == null) {
                // Try to get from shared preferences
                grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
            }
            if (grade == null) return 1;
            
            if (grade.startsWith("grade_")) {
                String numStr = grade.replace("grade_", "").replace("one", "1")
                    .replace("two", "2").replace("three", "3")
                    .replace("four", "4").replace("five", "5")
                    .replace("six", "6");
                return Integer.parseInt(numStr);
            } else {
                return Integer.parseInt(grade);
            }
        } catch (NumberFormatException e) {
            return 1; // Default to grade 1
        }
    }
    
    /**
     * Get the key for storing completed tutorials (grade-specific)
     */
    private String getCompletedTutorialsKey(String grade) {
        int gradeNum = getGradeNumber(grade);
        return KEY_COMPLETED_TUTORIALS + "_grade_" + gradeNum;
    }
    
    /**
     * Mark a tutorial as completed (grade-specific)
     */
    public void markTutorialCompleted(String tutorialName) {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        Set<String> completed = getCompletedTutorials(grade);
        completed.add(tutorialName.toLowerCase());
        
        SharedPreferences.Editor editor = androidSharedPreferences.edit();
        editor.putStringSet(getCompletedTutorialsKey(grade), completed);
        editor.apply();
    }
    
    /**
     * Check if a tutorial is completed (grade-specific)
     */
    public boolean isTutorialCompleted(String tutorialName) {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        Set<String> completed = getCompletedTutorials(grade);
        return completed.contains(tutorialName.toLowerCase());
    }
    
    /**
     * Check if previous tutorial is completed (for progression) - grade-specific
     */
    public boolean canAccessTutorial(String tutorialName) {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        String[] tutorialOrder = getTutorialOrderForGrade(grade);
        String tutLower = tutorialName.toLowerCase();
        
        // Find index of current tutorial in grade-specific order
        int currentIndex = -1;
        for (int i = 0; i < tutorialOrder.length; i++) {
            if (tutorialOrder[i].equals(tutLower)) {
                currentIndex = i;
                break;
            }
        }
        
        // If tutorial not in order list for this grade, check if it's allowed for this grade
        if (currentIndex == -1) {
            // Tutorial not in this grade's sequence - check if it's allowed at all
            return GradeRestrictionUtil.isTutorialAllowedForGrade(grade, tutorialName);
        }
        
        // First tutorial in sequence is always accessible
        if (currentIndex == 0) {
            return true;
        }
        
        // Check if previous tutorial in the sequence is completed
        String previousTutorial = tutorialOrder[currentIndex - 1];
        return isTutorialCompleted(previousTutorial);
    }
    
    /**
     * Get all completed tutorials for a specific grade
     */
    public Set<String> getCompletedTutorials(String grade) {
        String key = getCompletedTutorialsKey(grade);
        return androidSharedPreferences.getStringSet(key, new HashSet<>());
    }
    
    /**
     * Get all completed tutorials for current student's grade
     */
    public Set<String> getCompletedTutorials() {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        return getCompletedTutorials(grade);
    }
    
    /**
     * Get next available tutorial for current student's grade
     */
    public String getNextTutorial() {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        String[] tutorialOrder = getTutorialOrderForGrade(grade);
        Set<String> completed = getCompletedTutorials(grade);
        
        for (String tutorial : tutorialOrder) {
            if (!completed.contains(tutorial)) {
                return tutorial;
            }
        }
        
        return null; // All tutorials for this grade completed
    }
    
    /**
     * Get tutorial order for current student's grade
     */
    public String[] getTutorialOrder() {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        return getTutorialOrderForGrade(grade);
    }
    
    /**
     * Get previous tutorial in sequence for current student's grade
     */
    public String getPreviousTutorial(String currentTutorial) {
        String grade = com.happym.mathsquare.sharedPreferences.getGrade(context);
        String[] tutorialOrder = getTutorialOrderForGrade(grade);
        String tutLower = currentTutorial.toLowerCase();
        
        for (int i = 1; i < tutorialOrder.length; i++) {
            if (tutorialOrder[i].equals(tutLower)) {
                return tutorialOrder[i - 1];
            }
        }
        return "";
    }
    
    /**
     * Reset tutorial progress for a specific grade
     */
    public void resetProgress(String grade) {
        SharedPreferences.Editor editor = androidSharedPreferences.edit();
        editor.remove(getCompletedTutorialsKey(grade));
        editor.apply();
    }
    
    /**
     * Reset all tutorial progress (all grades)
     */
    public void resetProgress() {
        SharedPreferences.Editor editor = androidSharedPreferences.edit();
        // Remove progress for all grades
        for (int i = 1; i <= 6; i++) {
            editor.remove(KEY_COMPLETED_TUTORIALS + "_grade_" + i);
        }
        editor.apply();
    }
}

