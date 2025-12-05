package com.happym.mathsquare.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import com.happym.mathsquare.MainActivity;
import com.happym.mathsquare.R;

/**
 * Utility class for consistent back button handling across activities
 */
public class BackButtonHandler {
    
    /**
     * Show confirmation dialog before exiting quiz/game
     * Returns true if user confirmed exit, false if cancelled
     */
    public static void showExitConfirmation(Activity activity, String title, String message, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.RoundedAlertDialog);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
    
    /**
     * Standard back button behavior - just finish the activity
     */
    public static void handleStandardBack(Activity activity) {
        activity.finish();
    }
    
    /**
     * Back button that navigates to MainActivity (for quiz/game activities)
     */
    public static void handleBackToMain(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    
    /**
     * Back button with unsaved changes confirmation
     */
    public static void handleBackWithUnsavedChanges(Activity activity, Runnable onConfirmExit) {
        showExitConfirmation(
            activity,
            "Exit",
            "You have unsaved changes. Are you sure you want to exit?",
            () -> {
                if (onConfirmExit != null) {
                    onConfirmExit.run();
                } else {
                    activity.finish();
                }
            }
        );
    }
}

