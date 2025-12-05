package com.happym.mathsquare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.WindowCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

public class AddAdminActivity extends AppCompatActivity {

    // Firestore instance as class field
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_add_admin);

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
                firstNameLayout.setError("Admin's First Name is required");
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

    // Check if email already exists in admin collection
    private void checkEmailExists(String email, String firstName, String password, AppCompatButton submitButton) {
        Log.d("ADMIN_EMAIL_VALIDATION", "Checking if admin email exists: " + email);

        // Check Admin collection for existing email
        db.collection("Admin")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Email found in Admin collection
                    Log.d("ADMIN_EMAIL_VALIDATION", "Email found in Admin collection: " + email);
                    showEmailExistsError();
                } else {
                    // Email not found, proceed with account creation
                    Log.d("ADMIN_EMAIL_VALIDATION", "Email not found, proceeding with admin account creation: " + email);
                    createAdminAccount(email, firstName, password, submitButton);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("ADMIN_EMAIL_VALIDATION", "Error checking Admin collection: " + e.getMessage());
                // On error, proceed with creation (fail-safe)
                createAdminAccount(email, firstName, password, submitButton);
            });
    }

    // Show error message when email already exists
    private void showEmailExistsError() {
        TextInputLayout emailLayout = findViewById(R.id.email_address_layout);
        emailLayout.setError("This email address is already registered. Please use a different email.");
        animateShakeRotateEditTextErrorAnimation(emailLayout);
        Toast.makeText(this, "Email address already exists. Please use a different email.", Toast.LENGTH_LONG).show();
    }

    // Create admin account after email validation passes
    private void createAdminAccount(String email, String firstName, String password, AppCompatButton submitButton) {
        // Generate a unique document ID
        String adminId = UUID.randomUUID().toString();

        // Prepare data to save
        HashMap<String, Object> adminData = new HashMap<>();
        adminData.put("firstName", firstName);
        adminData.put("email", email);
        adminData.put("password", password); // In a real app, password should be hashed
        animateButtonClick(submitButton);

        Log.d("ADMIN_CREATION", "Creating admin account for: " + email);

        // Save admin data to Firestore
        db.collection("Admin")
                .document(adminId)
                .set(adminData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ADMIN_CREATION", "Admin account created successfully: " + email);
                    // Account created, navigate to Dashboard
                    Toast.makeText(AddAdminActivity.this, "Admin account created successfully", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("ADMIN_CREATION", "Error creating admin account: " + e.getMessage());
                    Toast.makeText(AddAdminActivity.this, "Error creating Admin account: " + e.getMessage(), Toast.LENGTH_LONG).show();
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