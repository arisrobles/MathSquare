package com.happym.mathsquare;

import android.text.InputFilter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.happym.mathsquare.sharedPreferences;
import androidx.core.view.WindowCompat;
public class teacherLogIn extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        FirebaseApp.initializeApp(this);

        setContentView(R.layout.layoutteacher_log_in);

        // Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        TextInputLayout emailLayout = findViewById(R.id.email_address_layout);
TextInputLayout passwordLayout = findViewById(R.id.password);
AppCompatButton submitButton = findViewById(R.id.btn_sign_in);


       TextInputEditText emailEditText = (TextInputEditText) emailLayout.getEditText();

InputFilter noSpacesFilter = (source, start, end, dest, dstart, dend) -> {
    if (source.toString().contains(" ")) {
        return "";
    }
    return source;
};

if (emailEditText != null) {
    emailEditText.setFilters(new InputFilter[]{noSpacesFilter});
}

       TextInputEditText passwordEditText = (TextInputEditText) passwordLayout.getEditText();



if (passwordEditText != null) {
    passwordEditText.setFilters(new InputFilter[]{noSpacesFilter});
}



submitButton.setOnClickListener(v -> {
    boolean hasError = false;
    emailLayout.setError(null);
    passwordLayout.setError(null);

    String email = ((TextInputEditText) emailLayout.getEditText()).getText().toString().trim();
    if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        emailLayout.setError("Valid email address is required");
        animateShakeRotateEditTextErrorAnimation(emailLayout);
        hasError = true;
    }

    String password = ((TextInputEditText) passwordLayout.getEditText()).getText().toString();
    if (TextUtils.isEmpty(password)) {
        passwordLayout.setError("Password is required");
        animateShakeRotateEditTextErrorAnimation(passwordLayout);
        hasError = true;
    }

    if (!hasError) {
                    animateButtonClick(submitButton);
        // Access the Firestore database and check if email and password match
        db.collection("Accounts")
            .document("Teachers")
            .collection(email)
            .document("MyProfile")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String storedPassword = documentSnapshot.getString("password");
                    if (storedPassword != null && storedPassword.equals(password)) {
                                    sharedPreferences.setLoggedIn(teacherLogIn.this, true);
                            sharedPreferences.saveEmail(teacherLogIn.this, email);

                            Intent intent = new Intent(teacherLogIn.this, Dashboard.class);



                        // Login success
                        Toast.makeText(this, "Welcome back Teacher!", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                            finish();
                        // Navigate to the next screen or perform the next action
                    } else {
                        // Incorrect password
                        passwordLayout.setError("Incorrect password");
                        animateShakeRotateEditTextErrorAnimation(passwordLayout);
                    }
                } else {

                    db.collection("Admin")
                            .whereEqualTo("email", email).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Admin account
                            String storedPassword = queryDocumentSnapshots.getDocuments().get(0).getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Save admin email to SharedPreferences for deletion validation
                                sharedPreferences.saveEmail(teacherLogIn.this, email);
                                Intent intent = new Intent(teacherLogIn.this, AdminActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Incorrect password
                                passwordLayout.setError("Incorrect password");
                                animateShakeRotateEditTextErrorAnimation(passwordLayout);
                            }
                        } else {
                            // Account does not exist
                            emailLayout.setError("Account does not exist");
                            animateShakeRotateEditTextErrorAnimation(emailLayout);
                        }
                            });
                }
            })
            .addOnFailureListener(e -> {
                // Handle any errors with the Firestore request
                Toast.makeText(this, "Error accessing account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
});



    }

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


}
