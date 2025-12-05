package com.happym.mathsquare.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.happym.mathsquare.sharedPreferences;
import com.happym.mathsquare.signInUp;

/**
 * Centralized session management utility
 * Handles logout operations consistently across the app
 */
public class SessionManager {
    
    /**
     * Logout student - clears all student-related data
     */
    public static void logoutStudent(Context context) {
        sharedPreferences.StudentIsSetLoggedIn(context, false);
        sharedPreferences.setLoggedIn(context, false);
        sharedPreferences.clearSection(context);
        sharedPreferences.clearGrade(context);
        sharedPreferences.clearFirstName(context);
        sharedPreferences.clearLastName(context);
        sharedPreferences.clearStudentNumber(context);
        
        navigateToLogin(context);
        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Logout teacher - clears teacher-related data
     */
    public static void logoutTeacher(Context context) {
        sharedPreferences.setLoggedIn(context, false);
        sharedPreferences.clearEmailId(context);
        
        navigateToLogin(context);
        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Logout admin - clears admin-related data
     */
    public static void logoutAdmin(Context context) {
        sharedPreferences.clearEmailId(context);
        
        navigateToLogin(context);
        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Navigate to login screen
     */
    private static void navigateToLogin(Context context) {
        Intent intent = new Intent(context, signInUp.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        
        // Finish activity if it's an Activity context
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).finish();
        }
    }
}

