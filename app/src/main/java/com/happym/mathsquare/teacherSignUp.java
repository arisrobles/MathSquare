package com.happym.mathsquare;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import com.happym.mathsquare.MainActivity;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.UUID;
import com.happym.mathsquare.sharedPreferences;
import androidx.core.view.WindowCompat;
public class teacherSignUp extends AppCompatActivity {

    // Firestore instance as class field
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        FirebaseApp.initializeApp(this);

        setContentView(R.layout.layoutteacher_sign_up);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        TextInputLayout emailLayout = findViewById(R.id.email_address_layout);
        TextInputLayout firstNameLayout = findViewById(R.id.first_name_layout);
        TextInputLayout passwordLayout = findViewById(R.id.password);
        TextInputLayout passwordRepeatLayout = findViewById(R.id.password_repeat);

        AppCompatButton submitButton = findViewById(R.id.btn_submit);

       TextInputEditText emailEditText = (TextInputEditText) emailLayout.getEditText();
TextInputEditText firstNameEditText = (TextInputEditText) firstNameLayout.getEditText();

InputFilter noSpacesFilter = (source, start, end, dest, dstart, dend) -> {
    if (source.toString().contains(" ")) {
        return "";
    }
    return source;
};

if (emailEditText != null) {
    emailEditText.setFilters(new InputFilter[]{noSpacesFilter});
           firstNameEditText.setFilters(new InputFilter[]{noSpacesFilter});
}

       TextInputEditText passwordEditText = (TextInputEditText) passwordLayout.getEditText();
TextInputEditText passwordREditText = (TextInputEditText) passwordRepeatLayout.getEditText();



if (passwordEditText != null) {
    passwordEditText.setFilters(new InputFilter[]{noSpacesFilter});
           passwordREditText.setFilters(new InputFilter[]{noSpacesFilter});
}


        submitButton.setOnClickListener(v -> {
            boolean hasError = false;
            emailLayout.setError(null);
            firstNameLayout.setError(null);
            passwordLayout.setError(null);
            passwordRepeatLayout.setError(null);

            // Validate First Name
            String firstName = ((TextInputEditText) firstNameLayout.getEditText()).getText().toString().trim();
            if (TextUtils.isEmpty(firstName)) {
                firstNameLayout.setError("Teacher's First Name is required");
                animateShakeRotateEditTextErrorAnimation(firstNameLayout);
                hasError = true;
            }

            // Validate Email
            String email = ((TextInputEditText) emailLayout.getEditText()).getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.setError("Valid email address is required");
                animateShakeRotateEditTextErrorAnimation(emailLayout);
                hasError = true;
            }

            // Validate Password
            String password = ((TextInputEditText) passwordLayout.getEditText()).getText().toString();
            String passwordRepeat = ((TextInputEditText) passwordRepeatLayout.getEditText()).getText().toString();
            if (TextUtils.isEmpty(password) || password.length() < 8) {
                passwordLayout.setError("Password must be at least 8 characters");
                animateShakeRotateEditTextErrorAnimation(passwordLayout);
                hasError = true;
            } else if (!password.equals(passwordRepeat)) {
                passwordRepeatLayout.setError("Passwords do not match");
                animateShakeRotateEditTextErrorAnimation(passwordRepeatLayout);
                hasError = true;
            }

            if (!hasError) {
                // Check if email already exists before creating account
                checkEmailExists(email, firstName, password, submitButton);
            }
        });
    }

    // Check if email already exists in any teacher collection
    private void checkEmailExists(String email, String firstName, String password, AppCompatButton submitButton) {
        Log.d("EMAIL_VALIDATION", "Checking if email exists: " + email);

        // First check: TeacherProfiles collection
        db.collection("TeacherProfiles")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(task1 -> {
                if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                    // Email found in TeacherProfiles
                    Log.d("EMAIL_VALIDATION", "Email found in TeacherProfiles: " + email);
                    showEmailExistsError();
                } else {
                    // Check second location: TeacherEmailList
                    String safeEmail = email.replace(".", "_").replace("@", "_at_");
                    db.collection("TeacherEmailList")
                        .document(safeEmail)
                        .get()
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful() && task2.getResult().exists()) {
                                // Email found in TeacherEmailList
                                Log.d("EMAIL_VALIDATION", "Email found in TeacherEmailList: " + email);
                                showEmailExistsError();
                            } else {
                                // Check third location: Original teacher structure
                                db.collection("Accounts")
                                    .document("Teachers")
                                    .collection(email)
                                    .document("MyProfile")
                                    .get()
                                    .addOnCompleteListener(task3 -> {
                                        if (task3.isSuccessful() && task3.getResult().exists()) {
                                            // Email found in original structure
                                            Log.d("EMAIL_VALIDATION", "Email found in original structure: " + email);
                                            showEmailExistsError();
                                        } else {
                                            // Email not found anywhere, proceed with account creation
                                            Log.d("EMAIL_VALIDATION", "Email not found, proceeding with account creation: " + email);
                                            createTeacherAccount(email, firstName, password, submitButton);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("EMAIL_VALIDATION", "Error checking original structure: " + e.getMessage());
                                        // On error, proceed with creation (fail-safe)
                                        createTeacherAccount(email, firstName, password, submitButton);
                                    });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EMAIL_VALIDATION", "Error checking TeacherEmailList: " + e.getMessage());
                            // On error, proceed with creation (fail-safe)
                            createTeacherAccount(email, firstName, password, submitButton);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("EMAIL_VALIDATION", "Error checking TeacherProfiles: " + e.getMessage());
                // On error, proceed with creation (fail-safe)
                createTeacherAccount(email, firstName, password, submitButton);
            });
    }

    // Show error message when email already exists
    private void showEmailExistsError() {
        TextInputLayout emailLayout = findViewById(R.id.email_address_layout);
        emailLayout.setError("This email address is already registered. Please use a different email.");
        animateShakeRotateEditTextErrorAnimation(emailLayout);
        Toast.makeText(this, "Email address already exists. Please use a different email.", Toast.LENGTH_LONG).show();
    }

    // Create teacher account after email validation passes
    private void createTeacherAccount(String email, String firstName, String password, AppCompatButton submitButton) {
        // Generate a unique document ID
        String teacherId = UUID.randomUUID().toString();

        // Prepare data to save
        HashMap<String, Object> teacherData = new HashMap<>();
        teacherData.put("firstName", firstName);
        teacherData.put("email", email);
        teacherData.put("password", password); // In a real app, password should be hashed
        animateButtonClick(submitButton);

        Log.d("TEACHER_CREATION", "Creating teacher account for: " + email);

        // Save teacher data to the original location
        db.collection("Accounts")
                .document("Teachers")
                .collection(email)
                .document("MyProfile")
                .set(teacherData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TEACHER_CREATION", "Teacher account created successfully in original location: " + email);
                    // Also save to a queryable collection for user management
                    saveToTeacherProfiles(teacherData, email);
                })
                .addOnFailureListener(e -> {
                    Log.e("TEACHER_CREATION", "Error creating teacher account: " + e.getMessage());
                    Toast.makeText(teacherSignUp.this, "Error creating teacher account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Save teacher data to a queryable collection for user management
    private void saveToTeacherProfiles(HashMap<String, Object> teacherData, String email) {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Generate a unique document ID for the teacher profile
        String teacherId = java.util.UUID.randomUUID().toString();

        // Create a copy of teacher data for the profiles collection
        HashMap<String, Object> profileData = new HashMap<>(teacherData);
        profileData.put("originalEmail", email); // Store original email for reference
        profileData.put("accountType", "Teacher");
        profileData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("TeacherProfiles")
                .document(teacherId)
                .set(profileData)
                .addOnSuccessListener(aVoid -> {
                    // Also add email to teacher list for discovery
                    addToTeacherEmailList(email);

                    // Teacher profile saved successfully
                    Toast.makeText(teacherSignUp.this, "Teacher account created successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Profile save failed, but main account was created
                    Toast.makeText(teacherSignUp.this, "Teacher account created, but profile sync failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish(); // Still finish since main account was created
                });
    }

    // Add teacher email to a discoverable list
    private void addToTeacherEmailList(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add email to a list in a special document for discovery
        HashMap<String, Object> emailData = new HashMap<>();
        emailData.put("email", email);
        emailData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("TeacherEmailList")
                .document(email.replace(".", "_").replace("@", "_at_")) // Safe document ID
                .set(emailData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TEACHER_SIGNUP", "Teacher email added to discovery list: " + email);
                })
                .addOnFailureListener(e -> {
                    Log.e("TEACHER_SIGNUP", "Failed to add teacher email to discovery list: " + e.getMessage());
                });
    }

        // Shake and rotate animation for error fields
    private void animateShakeRotateEditTextErrorAnimation(View view) {
        AnimatorSet animatorSet = new AnimatorSet();

        // Shake animation
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, 20f, -20f, 20f, -20f, 0);
        shakeAnimator.setDuration(350);

        // Rotate animation
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0, 5f, -5f, 5f, -5f, 0);
        rotateAnimator.setDuration(350);

        // Play both animations together
        animatorSet.playTogether(shakeAnimator, rotateAnimator);
        animatorSet.start();
    }

private void animateButtonClick(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.6f, 1.1f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.6f, 1.1f, 1f);

    // Set duration for the animations
    scaleX.setDuration(3000);
    scaleY.setDuration(3000);

    // OvershootInterpolator for game-like snappy effect
    OvershootInterpolator overshootInterpolator = new OvershootInterpolator(2f);
    scaleX.setInterpolator(overshootInterpolator);
    scaleY.setInterpolator(overshootInterpolator);

    // Combine animations into a set
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);
    animatorSet.start();
}


// Function to animate button focus with a smooth pulsing bounce effect
private void animateButtonFocus(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.06f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.06f, 1f);

    // Set duration for a slower, smoother pulsing bounce effect
    scaleX.setDuration(4000);
    scaleY.setDuration(4000);

    // AccelerateDecelerateInterpolator for smooth pulsing
    AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    scaleX.setInterpolator(interpolator);
    scaleY.setInterpolator(interpolator);

    // Set repeat count and mode on each ObjectAnimator
    scaleX.setRepeatCount(ObjectAnimator.INFINITE);  // Infinite repeat
    scaleX.setRepeatMode(ObjectAnimator.REVERSE);    // Reverse animation on repeat
    scaleY.setRepeatCount(ObjectAnimator.INFINITE);  // Infinite repeat
    scaleY.setRepeatMode(ObjectAnimator.REVERSE);    // Reverse animation on repeat

    // Combine the animations into an AnimatorSet
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);
    animatorSet.start();
}

    private void animateButtonPushDowm(View button) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);  // Scale down slightly
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);  // Scale down slightly

    // Set shorter duration for a quick push effect
    scaleX.setDuration(200);
    scaleY.setDuration(200);

    // Use a smooth interpolator
    AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    scaleX.setInterpolator(interpolator);
    scaleY.setInterpolator(interpolator);

    // Combine the animations into an AnimatorSet
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);

    // Start the animation
    animatorSet.start();
}

// Stop Focus Animation
private void stopButtonFocusAnimation(View button) {
    AnimatorSet animatorSet = (AnimatorSet) button.getTag();
    if (animatorSet != null) {
        animatorSet.cancel();  // Stop the animation when focus is lost
    }
}

}
