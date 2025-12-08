package com.happym.mathsquare.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.happym.mathsquare.MainActivity;
import com.happym.mathsquare.R;

/**
 * Utility class for consistent back button handling across activities
 * Uses modern OnBackPressedDispatcher API (Android 13+)
 */
public class BackButtonHandler {
    
    /**
     * Show confirmation dialog before exiting quiz/game
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
     * Register standard back button behavior - just finish the activity
     * Uses modern OnBackPressedDispatcher API
     */
    public static OnBackPressedCallback registerStandardBack(AppCompatActivity activity) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                activity.finish();
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, callback);
        return callback;
    }
    
    /**
     * Register back button that navigates to MainActivity (for quiz/game activities)
     * Uses modern OnBackPressedDispatcher API
     */
    public static OnBackPressedCallback registerBackToMain(AppCompatActivity activity) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, callback);
        return callback;
    }
    
    /**
     * Register back button with unsaved changes confirmation
     * Uses modern OnBackPressedDispatcher API
     */
    public static OnBackPressedCallback registerBackWithUnsavedChanges(
            AppCompatActivity activity, 
            java.util.function.Supplier<Boolean> hasUnsavedChanges,
            Runnable onConfirmExit) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges != null && hasUnsavedChanges.get()) {
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
                } else {
                    activity.finish();
                }
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, callback);
        return callback;
    }
    
    /**
     * Register back button with custom confirmation dialog
     * Uses modern OnBackPressedDispatcher API
     */
    public static OnBackPressedCallback registerBackWithConfirmation(
            AppCompatActivity activity,
            String title,
            String message,
            Runnable onConfirm) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation(activity, title, message, onConfirm);
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, callback);
        return callback;
    }
    
    // Legacy methods for backward compatibility (deprecated)
    /**
     * @deprecated Use registerStandardBack() instead
     */
    @Deprecated
    public static void handleStandardBack(Activity activity) {
        activity.finish();
    }
    
    /**
     * @deprecated Use registerBackToMain() instead
     */
    @Deprecated
    public static void handleBackToMain(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    
    /**
     * @deprecated Use registerBackWithUnsavedChanges() instead
     */
    @Deprecated
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

