package com.happym.mathsquare.utils;

import android.content.Context;
import com.happym.mathsquare.sharedPreferences;

/**
 * Utility class for grade level filtering and restrictions
 * Implements defense comment #1: Grade level - Filtering/Restrictions
 */
public class GradeRestrictionUtil {
    
    /**
     * Check if a grade level is allowed to access a specific operation
     */
    public static boolean isOperationAllowedForGrade(String grade, String operation) {
        if (grade == null || operation == null) return false;
        
        int gradeNum = getGradeNumber(grade);
        String opLower = operation.toLowerCase();
        
        // Grades 1-2: Only addition and subtraction
        if (gradeNum <= 2) {
            return opLower.equals("addition") || opLower.equals("subtraction");
        }
        // Grades 3-4: All basic operations except decimals and percentages
        else if (gradeNum <= 4) {
            return !opLower.contains("decimal") && !opLower.contains("percentage");
        }
        // Grades 5-6: All operations allowed
        else {
            return true;
        }
    }
    
    /**
     * Check if a grade level is allowed to access a specific tutorial
     * Implements defense comment #7: Tutorial - Appropriate to grade level
     * Note: Guests (grade == null) have access to all tutorials
     */
    public static boolean isTutorialAllowedForGrade(String grade, String tutorialName) {
        if (tutorialName == null) return false;
        
        // Guests (grade == null) have access to all tutorials
        if (grade == null) {
            return true;
        }
        
        int gradeNum = getGradeNumber(grade);
        String tutLower = tutorialName.toLowerCase();
        
        // Grades 1-2: Only basic operations
        if (gradeNum <= 2) {
            return tutLower.contains("addition") || tutLower.contains("subtraction");
        }
        // Grades 3-4: All basic operations
        else if (gradeNum <= 4) {
            return !tutLower.contains("decimal") && !tutLower.contains("percentage");
        }
        // Grades 5-6: All tutorials
        else {
            return true;
        }
    }
    
    /**
     * Get grade number from grade string
     */
    private static int getGradeNumber(String grade) {
        try {
            // Handle both "1" and "grade_one" formats
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
     * Check if student's grade matches the required grade for access
     */
    public static boolean checkGradeAccess(Context context, String requiredGrade) {
        String studentGrade = sharedPreferences.getGrade(context);
        if (studentGrade == null) return false;
        
        int studentGradeNum = getGradeNumber(studentGrade);
        int requiredGradeNum = getGradeNumber(requiredGrade);
        
        return studentGradeNum == requiredGradeNum;
    }
    
    /**
     * Restrict access based on grade - returns true if access is allowed
     */
    public static boolean allowAccess(Context context, String operation) {
        String grade = sharedPreferences.getGrade(context);
        return isOperationAllowedForGrade(grade, operation);
    }
}

